import { useCallback, useEffect, useRef, useState } from "react"
import {
  deleteSubscription as deleteSubscriptionRequest,
  getSubscriptions,
} from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { toProductSubscription } from "../../data/productMappers"
import type { Subscription } from "../../model"

type SubscriptionData = {
  subscriptions: Subscription[]
  loadingSubscriptions: boolean
  subscriptionsError: boolean
  refreshSubscriptions(): Promise<boolean>
  mutateSubscriptions(mutation: () => Promise<void>): Promise<boolean>
  removeSubscription(subscriptionId: string): Promise<boolean>
}

export function useSubscriptionData(): SubscriptionData {
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([])
  const [loadingSubscriptions, setLoadingSubscriptions] = useState(true)
  const [subscriptionsError, setSubscriptionsError] = useState(false)
  const requestRevision = useRef(0)

  const refreshSubscriptions = useCallback(async () => {
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoadingSubscriptions(true)
    setSubscriptionsError(false)

    try {
      const response = await getSubscriptions()

      if (revision !== requestRevision.current) {
        return false
      }

      setSubscriptions(response.items.map(toProductSubscription))
      return true
    } catch (error) {
      if (revision === requestRevision.current) {
        setSubscriptionsError(true)
        handleError(error)
      }

      return false
    } finally {
      if (revision === requestRevision.current) {
        setLoadingSubscriptions(false)
      }
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refreshSubscriptions()
    })

    return () => {
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [refreshSubscriptions])

  const mutateSubscriptions = useCallback(
    async (mutation: () => Promise<void>) => {
      requestRevision.current += 1

      try {
        await mutation()
      } catch (error) {
        handleError(error)
        await refreshSubscriptions()
        return false
      }

      return refreshSubscriptions()
    },
    [refreshSubscriptions],
  )

  const removeSubscription = useCallback(
    (subscriptionId: string) =>
      mutateSubscriptions(() => deleteSubscriptionRequest(subscriptionId)),
    [mutateSubscriptions],
  )

  return {
    subscriptions,
    loadingSubscriptions,
    subscriptionsError,
    refreshSubscriptions,
    mutateSubscriptions,
    removeSubscription,
  }
}
