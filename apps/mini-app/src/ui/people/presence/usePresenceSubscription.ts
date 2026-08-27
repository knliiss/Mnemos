import { useCallback, useEffect, useState } from "react"
import {
  createPresenceSubscription,
  deletePresenceSubscription,
  listPresenceSubscriptions,
} from "@/shared/api/playerDirectoryApi"
import { handleError } from "@/shared/errors/errorHandler"

export function usePresenceSubscription(playerId: string) {
  const [subscribed, setSubscribed] = useState(false)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)

  const refresh = useCallback(async () => {
    setLoading(true)

    try {
      const subscriptions = await listPresenceSubscriptions()
      setSubscribed(subscriptions.some((subscription) => subscription.playerId === playerId))
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [playerId])

  useEffect(() => {
    let active = true

    void listPresenceSubscriptions()
      .then((subscriptions) => {
        if (!active) {
          return
        }

        setSubscribed(subscriptions.some((subscription) => subscription.playerId === playerId))
      })
      .catch((error) => {
        if (active) {
          handleError(error)
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [playerId])

  const toggle = useCallback(async () => {
    if (busy) {
      return
    }

    setBusy(true)

    try {
      if (subscribed) {
        await deletePresenceSubscription(playerId)
        setSubscribed(false)
      } else {
        await createPresenceSubscription(playerId)
        setSubscribed(true)
      }
    } catch (error) {
      handleError(error)
      await refresh()
    } finally {
      setBusy(false)
    }
  }, [busy, playerId, refresh, subscribed])

  return {
    subscribed,
    loading,
    busy,
    toggle,
  }
}
