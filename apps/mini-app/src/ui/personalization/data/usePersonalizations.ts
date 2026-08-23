import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from "react"
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
import { useMnemosData } from "../../data/MnemosDataProvider"
import type { Personalization } from "../../model"
import { toPersonalization } from "./personalizationMappers"

type PersonalizationData = {
  items: Personalization[]
  loading: boolean
  error: boolean
  busyId: string | null
  pendingPurchases: Record<string, string>
  refresh(): Promise<void>
  purchase(item: Personalization): Promise<void>
  toggleActive(item: Personalization): Promise<void>
}

type PurchaseStatus = "CONFIRMED" | "FAILED"

type PendingPurchase = {
  personalizationId: string
  operationId: string
  target: Personalization["target"]
  scopeKey: string
}

type PendingPurchaseState = Record<string, PendingPurchase>

const purchasePollAttempts = 15
const purchasePollIntervalMs = 900

export function usePersonalizations(): PersonalizationData {
  const { t } = useTranslation()
  const data = useMnemosData()
  const errors = useErrorNotifications()
  const [items, setItems] = useState<Personalization[]>([])
  const [itemsScopeKey, setItemsScopeKey] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [busyByScope, setBusyByScope] = useState<Record<string, string>>({})
  const [pendingPurchaseState, setPendingPurchaseState] = useState<PendingPurchaseState>({})
  const requestRevision = useRef(0)
  const busyByScopeRef = useRef<Record<string, string>>({})
  const pendingPurchaseStateRef = useRef<PendingPurchaseState>({})
  const latestRefreshRef = useRef<() => Promise<void>>(async () => undefined)
  const latestRefreshAccountRef = useRef<() => Promise<void>>(async () => undefined)
  const mountedRef = useRef(true)

  const userId = data.user.id
  const username = data.user.username
  const activeAccountId = data.activeAccount?.id ?? null
  const refreshAccount = data.refreshAccount
  const scopeKey = `${userId}:${activeAccountId ?? "none"}`
  const userBusyScopeKey = `user:${userId}`
  const scopeKeyRef = useRef(scopeKey)

  useLayoutEffect(() => {
    scopeKeyRef.current = scopeKey
  }, [scopeKey])

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

  const updateBusyByScope = useCallback(
    (update: (current: Record<string, string>) => Record<string, string>) => {
      const next = update(busyByScopeRef.current)

      busyByScopeRef.current = next
      setBusyByScope(next)
    },
    [],
  )

  const updatePendingPurchases = useCallback(
    (update: (current: PendingPurchaseState) => PendingPurchaseState) => {
      const next = update(pendingPurchaseStateRef.current)

      pendingPurchaseStateRef.current = next
      setPendingPurchaseState(next)
    },
    [],
  )

  const refresh = useCallback(async () => {
    const requestedScopeKey = scopeKey
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoading(true)
    setError(false)

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
        toPersonalization(response, ownedById.get(response.id) ?? null, userId, username),
      )

      setItems(mappedItems)
      setItemsScopeKey(requestedScopeKey)
    } catch (error) {
      if (revision === requestRevision.current && scopeKeyRef.current === requestedScopeKey) {
        setItems([])
        setItemsScopeKey(requestedScopeKey)
        setError(true)
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

    return () => {
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [refresh])

  useLayoutEffect(() => {
    latestRefreshRef.current = refresh
    latestRefreshAccountRef.current = refreshAccount
  }, [refresh, refreshAccount])

  const finishPurchase = useCallback(
    async (
      personalizationId: string,
      operationId: string,
      status: PurchaseStatus,
      failureReason?: string | null,
    ) => {
      if (!mountedRef.current) {
        return
      }

      const pendingEntry = Object.entries(pendingPurchaseStateRef.current).find(
        ([, candidate]) =>
          candidate.personalizationId === personalizationId &&
          candidate.operationId === operationId,
      )

      if (!pendingEntry) {
        return
      }

      const [pendingKey, pending] = pendingEntry

      updatePendingPurchases((current) => {
        if (current[pendingKey]?.operationId !== operationId) {
          return current
        }

        const next = { ...current }

        delete next[pendingKey]
        return next
      })

      if (status === "CONFIRMED") {
        if (pending.target === "USER" || pending.scopeKey === scopeKeyRef.current) {
          await Promise.all([latestRefreshRef.current(), latestRefreshAccountRef.current()])
        }

        return
      }

      if (pending.target === "USER" || pending.scopeKey === scopeKeyRef.current) {
        errors.showBackendError({
          code: "PERSONALIZATION_PURCHASE_FAILED",
          statusCode: 409,
          message: failureReason ?? t("personalization.purchaseFailed"),
        })
      }
    },
    [errors, t, updatePendingPurchases],
  )

  useEffect(() => {
    return realtimeEvents.subscribe((event) => {
      if (event.type !== "personalization.purchase.completed" || !event.operationId) {
        return
      }

      const pending = Object.values(pendingPurchaseStateRef.current).find(
        (candidate) => candidate.operationId === event.operationId,
      )

      if (!pending || pending.personalizationId !== event.payload.personalizationId) {
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
    async (pendingKey: string, operationId: string) => {
      let lastPollError: unknown = null

      for (let attempt = 0; attempt < purchasePollAttempts; attempt += 1) {
        if (!mountedRef.current) {
          return
        }

        const pending = pendingPurchaseStateRef.current[pendingKey]

        if (!pending || pending.operationId !== operationId) {
          return
        }

        await new Promise((resolve) => window.setTimeout(resolve, purchasePollIntervalMs))

        if (!mountedRef.current) {
          return
        }

        try {
          const operation = await getPurchaseOperation(operationId)
          lastPollError = null
          const current = pendingPurchaseStateRef.current[pendingKey]

          if (!current || current.operationId !== operationId) {
            return
          }

          if (operation.ownerType === "ACCOUNT") {
            const operationScopeKey = `${userId}:${operation.ownerId}`

            if (current.scopeKey !== operationScopeKey) {
              updatePendingPurchases((state) => ({
                ...state,
                [pendingKey]: {
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
            current.personalizationId,
            operationId,
            operation.status,
            operation.failureReason,
          )
          return
        } catch (error) {
          lastPollError = error
        }
      }

      const pending = pendingPurchaseStateRef.current[pendingKey]

      if (
        lastPollError &&
        pending?.operationId === operationId &&
        (pending.target === "USER" || pending.scopeKey === scopeKeyRef.current)
      ) {
        handleError(lastPollError)
      }
    },
    [finishPurchase, updatePendingPurchases, userId],
  )

  const purchase = useCallback(
    async (item: Personalization) => {
      const purchaseScopeKey = scopeKey
      const busyScopeKey = item.target === "USER" ? userBusyScopeKey : purchaseScopeKey
      const existingPurchase = Object.values(pendingPurchaseStateRef.current).some(
        (pending) =>
          pending.personalizationId === item.id &&
          (pending.target === "USER" || pending.scopeKey === purchaseScopeKey),
      )

      if (item.owned || existingPurchase || busyByScopeRef.current[busyScopeKey]) {
        return
      }

      updateBusyByScope((current) => ({ ...current, [busyScopeKey]: item.id }))

      try {
        const operation = await purchasePersonalization(item.id)

        if (!mountedRef.current) {
          return
        }

        const pendingKey =
          item.target === "USER"
            ? `user:${userId}:${item.id}`
            : `account:${purchaseScopeKey}:${item.id}`

        updatePendingPurchases((current) => ({
          ...current,
          [pendingKey]: {
            personalizationId: item.id,
            operationId: operation.operationId,
            target: item.target,
            scopeKey: purchaseScopeKey,
          },
        }))

        void pollPurchase(pendingKey, operation.operationId)
      } catch (error) {
        if (
          mountedRef.current &&
          (item.target === "USER" || purchaseScopeKey === scopeKeyRef.current)
        ) {
          handleError(error)
        }
      } finally {
        if (mountedRef.current) {
          updateBusyByScope((current) => {
            if (current[busyScopeKey] !== item.id) {
              return current
            }

            const next = { ...current }

            delete next[busyScopeKey]
            return next
          })
        }
      }
    },
    [pollPurchase, scopeKey, updateBusyByScope, updatePendingPurchases, userBusyScopeKey, userId],
  )

  const toggleActive = useCallback(
    async (item: Personalization) => {
      const actionScopeKey = scopeKey
      const busyScopeKey = item.target === "USER" ? userBusyScopeKey : actionScopeKey

      if (!item.owned || busyByScopeRef.current[busyScopeKey]) {
        return
      }

      updateBusyByScope((current) => ({ ...current, [busyScopeKey]: item.id }))

      try {
        if (item.active) {
          await deactivatePersonalization(item.id)
        } else {
          await activatePersonalization(item.id)
        }

        if (item.target === "USER") {
          await latestRefreshRef.current()
        } else if (actionScopeKey === scopeKeyRef.current) {
          await refresh()
        }
      } catch (error) {
        if (
          mountedRef.current &&
          (item.target === "USER" || actionScopeKey === scopeKeyRef.current)
        ) {
          handleError(error)
        }
      } finally {
        if (mountedRef.current) {
          updateBusyByScope((current) => {
            if (current[busyScopeKey] !== item.id) {
              return current
            }

            const next = { ...current }

            delete next[busyScopeKey]
            return next
          })
        }
      }
    },
    [refresh, scopeKey, updateBusyByScope, userBusyScopeKey],
  )

  const pendingPurchases = useMemo(
    () =>
      Object.fromEntries(
        Object.entries(pendingPurchaseState)
          .filter(([, pending]) => pending.target === "USER" || pending.scopeKey === scopeKey)
          .map(([, pending]) => [pending.personalizationId, pending.operationId]),
      ),
    [pendingPurchaseState, scopeKey],
  )

  return {
    items: itemsScopeKey === scopeKey ? items : [],
    loading: loading || itemsScopeKey !== scopeKey,
    error: itemsScopeKey === scopeKey && error,
    busyId: busyByScope[userBusyScopeKey] ?? busyByScope[scopeKey] ?? null,
    pendingPurchases,
    refresh,
    purchase,
    toggleActive,
  }
}
