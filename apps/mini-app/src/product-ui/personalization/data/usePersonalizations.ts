import { useCallback, useEffect, useMemo, useRef, useState } from "react"
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

type PendingPurchase = {
  operationId: string
  target: Personalization["target"]
  scopeKey: string
}

type PendingPurchaseState = Record<string, PendingPurchase>

const purchasePollAttempts = 15
const purchasePollIntervalMs = 900

export function usePersonalizations(): PersonalizationData {
  const { t } = useTranslation()
  const product = useProductData()
  const errors = useErrorNotifications()
  const [items, setItems] = useState<Personalization[]>([])
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState<string | null>(null)
  const [pendingPurchaseState, setPendingPurchaseState] = useState<PendingPurchaseState>({})
  const requestRevision = useRef(0)
  const pendingPurchaseStateRef = useRef<PendingPurchaseState>({})

  const userId = product.user.id
  const username = product.user.username
  const activeAccountId = product.activeAccount?.id ?? null
  const refreshAccount = product.refreshAccount
  const scopeKey = `${userId}:${activeAccountId ?? "none"}`
  const scopeKeyRef = useRef(scopeKey)

  scopeKeyRef.current = scopeKey

  const updatePendingPurchases = useCallback(
    (update: (current: PendingPurchaseState) => PendingPurchaseState) => {
      setPendingPurchaseState((current) => {
        const next = update(current)

        pendingPurchaseStateRef.current = next
        return next
      })
    },
    [],
  )

  const refresh = useCallback(async () => {
    const requestedScopeKey = scopeKey
    const revision = requestRevision.current + 1

    requestRevision.current = revision
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

      if (revision !== requestRevision.current || scopeKeyRef.current !== requestedScopeKey) {
        return
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
      if (revision === requestRevision.current && scopeKeyRef.current === requestedScopeKey) {
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current && scopeKeyRef.current === requestedScopeKey) {
        setLoading(false)
      }
    }
  }, [scopeKey, userId, username])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refresh])

  const finishPurchase = useCallback(
    async (
      personalizationId: string,
      operationId: string,
      status: PurchaseStatus,
      failureReason?: string | null,
    ) => {
      const pending = pendingPurchaseStateRef.current[personalizationId]

      if (!pending || pending.operationId !== operationId) {
        return
      }

      updatePendingPurchases((current) => {
        if (current[personalizationId]?.operationId !== operationId) {
          return current
        }

        const next = { ...current }

        delete next[personalizationId]
        return next
      })

      if (status === "CONFIRMED") {
        if (pending.target === "USER" || pending.scopeKey === scopeKeyRef.current) {
          await Promise.all([refresh(), refreshAccount()])
        }

        return
      }

      errors.showBackendError({
        code: "PERSONALIZATION_PURCHASE_FAILED",
        statusCode: 409,
        message: failureReason ?? t("personalization.purchaseFailed"),
      })
    },
    [errors, refresh, refreshAccount, t, updatePendingPurchases],
  )

  useEffect(() => {
    return realtimeEvents.subscribe((event) => {
      if (
        event.type !== "personalization.purchase.completed" ||
        !event.operationId
      ) {
        return
      }

      const pending = pendingPurchaseStateRef.current[event.payload.personalizationId]

      if (!pending || pending.operationId !== event.operationId) {
        return
      }

      void finishPurchase(
        event.payload.personalizationId,
        event.operationId,
        event.payload.status,
        event.payload.failureReason,
      )
    })
  }, [finishPurchase])

  const pollPurchase = useCallback(
    async (personalizationId: string, operationId: string) => {
      for (let attempt = 0; attempt < purchasePollAttempts; attempt += 1) {
        const pending = pendingPurchaseStateRef.current[personalizationId]

        if (!pending || pending.operationId !== operationId) {
          return
        }

        await new Promise((resolve) => window.setTimeout(resolve, purchasePollIntervalMs))

        try {
          const operation = await getPurchaseOperation(operationId)
          const current = pendingPurchaseStateRef.current[personalizationId]

          if (!current || current.operationId !== operationId) {
            return
          }

          if (operation.ownerType === "ACCOUNT") {
            const operationScopeKey = `${userId}:${operation.ownerId}`

            if (current.scopeKey !== operationScopeKey) {
              updatePendingPurchases((state) => ({
                ...state,
                [personalizationId]: {
                  ...current,
                  scopeKey: operationScopeKey,
                },
              }))
            }
          }

          if (operation.status === "PENDING") {
            continue
          }

          await finishPurchase(
            personalizationId,
            operationId,
            operation.status,
            operation.failureReason,
          )
          return
        } catch {
          // Realtime delivery is primary. Polling only covers reconnect races.
        }
      }
    },
    [finishPurchase, updatePendingPurchases, userId],
  )

  const purchase = useCallback(
    async (item: Personalization) => {
      if (item.owned || pendingPurchaseStateRef.current[item.id] || busyId) {
        return
      }

      const purchaseScopeKey = scopeKeyRef.current

      setBusyId(item.id)

      try {
        const operation = await purchasePersonalization(item.id)

        updatePendingPurchases((current) => ({
          ...current,
          [item.id]: {
            operationId: operation.operationId,
            target: item.target,
            scopeKey: purchaseScopeKey,
          },
        }))

        void pollPurchase(item.id, operation.operationId)
      } catch (error) {
        handleError(error)
      } finally {
        setBusyId(null)
      }
    },
    [busyId, pollPurchase, updatePendingPurchases],
  )

  const toggleActive = useCallback(
    async (item: Personalization) => {
      if (!item.owned || busyId) {
        return
      }

      const actionScopeKey = scopeKeyRef.current

      setBusyId(item.id)

      try {
        if (item.active) {
          await deactivatePersonalization(item.id)
        } else {
          await activatePersonalization(item.id)
        }

        if (item.target === "USER" || actionScopeKey === scopeKeyRef.current) {
          await refresh()
        }
      } catch (error) {
        handleError(error)
      } finally {
        setBusyId(null)
      }
    },
    [busyId, refresh],
  )

  const pendingPurchases = useMemo(
    () =>
      Object.fromEntries(
        Object.entries(pendingPurchaseState)
          .filter(
            ([, pending]) => pending.target === "USER" || pending.scopeKey === scopeKey,
          )
          .map(([personalizationId, pending]) => [personalizationId, pending.operationId]),
      ),
    [pendingPurchaseState, scopeKey],
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
