import { useCallback, useEffect, useState } from "react"
import {
  deleteSubscription as deleteSubscriptionRequest,
  getSubscriptions,
} from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { Subscription } from "../model"
import { toProductSubscription } from "./productMappers"

type SubscriptionData = {
  subscriptions: Subscription[]
  refreshSubscriptions(): Promise<void>
  removeSubscription(subscriptionId: string): Promise<void>
}

export function useSubscriptionData(): SubscriptionData {
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([])

  const refreshSubscriptions = useCallback(async () => {
    try {
      const response = await getSubscriptions()

      setSubscriptions(response.items.map(toProductSubscription))
    } catch (error) {
      handleError(error)
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refreshSubscriptions()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refreshSubscriptions])

  const removeSubscription = useCallback(async (subscriptionId: string) => {
    try {
      await deleteSubscriptionRequest(subscriptionId)

      setSubscriptions((current) =>
        current.filter((subscription) => subscription.id !== subscriptionId),
      )
    } catch (error) {
      handleError(error)
      throw error
    }
  }, [])

  return {
    subscriptions,
    refreshSubscriptions,
    removeSubscription,
  }
}
