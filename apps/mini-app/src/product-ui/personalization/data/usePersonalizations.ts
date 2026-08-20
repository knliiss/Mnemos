import { useCallback, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  activatePersonalization,
  deactivatePersonalization,
  getPersonalization,
  getPurchaseOperation,
  listOwnedPersonalizations,
  listPersonalizations,
  purchasePersonalization,
  type PersonalizationResponse,
} from "@/shared/api/personalizationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import { useErrorNotifications } from "../../components/errors/ErrorNotifications"
import { PERSONALIZATION_TYPES } from "../../contracts"
import { useProductData } from "../../data/ProductDataProvider"
import type { Personalization } from "../../model"
import { toProductPersonalization } from "./personalizationMappers"

type PersonalizationData = {
  items: Personalization[]
  loading: boolean
  busyId: string | null
  pendingPurchases: Record<string, string>
  purchase(item: Personalization): Promise<void>
  toggleActive(item: Personalization): Promise<void>
}

type PurchaseStatus = "CONFIRMED" | "FAILED"

const purchasePollAttempts = 15
const purchasePollIntervalMs = 900

export function usePersonalizations(): PersonalizationData {
  const { t } = useTranslation()
  const product = useProductData()
  const errors = useErrorNotifications()
  const [items, setItems] = useState<Personalization[]>([])
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState<string | null>(null)
  const [pendingPurchases, setPendingPurchases] = useState<Record<string, string>>({})

  const userId = product.user.id
  const username = product.user.username
  const refreshAccount = product.refreshAccount

  const refresh = useCallback(async () => {
    setLoading(true)

    try {
      const [ownedPage, ...catalogPages] = await Promise.all([
        listOwnedPersonalizations(undefined, 0, 100),
        ...PERSONALIZATION_TYPES.map((type) => listPersonalizations(type, undefined, 0, 100)),
      ])
      const ownedById = new Map(ownedPage.items.map((owned) => [owned.personalizationId, owned]))
      const responsesById = new Map<string, PersonalizationResponse>()

      for (const page of catalogPages) {
        for (const item of page.items) {
          responsesById.set(item.id, item)
        }
      }

      const missingOwnedIds = ownedPage.items
        .map((owned) => owned.personalizationId)
        .filter((id) => !responsesById.has(id))

      if (missingOwnedIds.length > 0) {
        const missingResponses = await Promise.all(
          missingOwnedIds.map((id) => getPersonalization(id)),
        )

        for (const response of missingResponses) {
          responsesById.set(response.id, response)
        }
      }

      const mappedItems = [...responsesById.values()].map((response) =>
        toProductPersonalization(
          response,
          ownedById.get(response.id) ?? null,
          userId,
          username,
        ),
      )

      setItems(mappedItems)
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [userId, username])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refresh])

  const finishPurchase = useCallback(
    async (
      personalizationId: string,
      status: PurchaseStatus,
      failureReason?: string | null,
    ) => {
      setPendingPurchases((current) => {
        const next = { ...current }

        delete next[personalizationId]

        return next
      })

      if (status === "CONFIRMED") {
        await Promise.all([refresh(), refreshAccount()])
        return
      }

      errors.showBackendError({
        code: "PERSONALIZATION_PURCHASE_FAILED",
        statusCode: 409,
        message: failureReason ?? t("personalization.purchaseFailed"),
      })
    },
    [errors, refresh, refreshAccount, t],
  )

  useEffect(() => {
    return realtimeEvents.subscribe((event) => {
      if (event.type !== "personalization.purchase.completed") {
        return
      }

      void finishPurchase(
        event.payload.personalizationId,
        event.payload.status,
        event.payload.failureReason,
      )
    })
  }, [finishPurchase])

  const pollPurchase = useCallback(
    async (personalizationId: string, operationId: string) => {
      for (let attempt = 0; attempt < purchasePollAttempts; attempt += 1) {
        await new Promise((resolve) => window.setTimeout(resolve, purchasePollIntervalMs))

        try {
          const operation = await getPurchaseOperation(operationId)

          if (operation.status === "PENDING") {
            continue
          }

          await finishPurchase(personalizationId, operation.status, operation.failureReason)
          return
        } catch {
          // Realtime delivery is primary. Polling only covers reconnect races.
        }
      }
    },
    [finishPurchase],
  )

  const purchase = useCallback(
    async (item: Personalization) => {
      if (item.owned || pendingPurchases[item.id] || busyId) {
        return
      }

      setBusyId(item.id)

      try {
        const operation = await purchasePersonalization(item.id)

        setPendingPurchases((current) => ({
          ...current,
          [item.id]: operation.operationId,
        }))

        void pollPurchase(item.id, operation.operationId)
      } catch (error) {
        handleError(error)
      } finally {
        setBusyId(null)
      }
    },
    [busyId, pendingPurchases, pollPurchase],
  )

  const toggleActive = useCallback(
    async (item: Personalization) => {
      if (!item.owned || busyId) {
        return
      }

      setBusyId(item.id)

      try {
        if (item.active) {
          await deactivatePersonalization(item.id)
        } else {
          await activatePersonalization(item.id)
        }

        await refresh()
      } catch (error) {
        handleError(error)
      } finally {
        setBusyId(null)
      }
    },
    [busyId, refresh],
  )

  return {
    items,
    loading,
    busyId,
    pendingPurchases,
    purchase,
    toggleActive,
  }
}
