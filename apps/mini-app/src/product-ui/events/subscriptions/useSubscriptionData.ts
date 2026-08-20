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
  refreshSubscriptions(): Promise<void>
  removeSubscription(subscriptionId: string): Promise<void>
}

export function useSubscriptionData(): SubscriptionData {
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([])
  const requestRevision = useRef(0)

  const refreshSubscriptions = useCallback(async () => {
    const revision = requestRevision.current + 1

    requestRevision.current = revision

    try {
      const response = await getSubscriptions()

      if (revision !== requestRevision.current) {
        return
      }

      setSubscriptions(response.items.map(toProductSubscription))
    } catch (error) {
      if (revision === requestRevision.current) {
        handleError(error)
      }
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refreshSubscriptions()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refreshSubscriptions])

  const removeSubscription = useCallback(
    async (subscriptionId: string) => {
      requestRevision.current += 1

      try {
        await deleteSubscriptionRequest(subscriptionId)
        await refreshSubscriptions()
      } catch (error) {
        handleError(error)
        throw error
      }
    },
    [refreshSubscriptions],
  )

  return {
    subscriptions,
    refreshSubscriptions,
    removeSubscription,
  }
}
