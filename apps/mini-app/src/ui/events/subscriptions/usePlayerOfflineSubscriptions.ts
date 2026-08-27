import { useCallback, useEffect, useRef, useState } from "react"
import {
  createPresenceSubscription,
  deletePresenceSubscription,
  listPresenceSubscriptions,
  resolvePlayerByNickname,
  type PlayerResponse,
  type PresenceSubscriptionResponse,
} from "@/shared/api/playerDirectoryApi"
import { handleError } from "@/shared/errors/errorHandler"

export function usePlayerOfflineSubscriptions() {
  const [subscriptions, setSubscriptions] = useState<PresenceSubscriptionResponse[]>([])
  const [resolvedPlayer, setResolvedPlayer] = useState<PlayerResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadFailed, setLoadFailed] = useState(false)
  const [searching, setSearching] = useState(false)
  const [searchFailed, setSearchFailed] = useState(false)
  const [busyPlayerId, setBusyPlayerId] = useState<string | null>(null)
  const searchRevision = useRef(0)
  const mutationLocked = useRef(false)

  const refresh = useCallback(async () => {
    setLoading(true)

    try {
      const nextSubscriptions = await listPresenceSubscriptions()

      setSubscriptions(nextSubscriptions)
      setLoadFailed(false)
    } catch (error) {
      handleError(error)
      setLoadFailed(true)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    let active = true

    void listPresenceSubscriptions()
      .then((nextSubscriptions) => {
        if (!active) {
          return
        }

        setSubscriptions(nextSubscriptions)
        setLoadFailed(false)
      })
      .catch((error) => {
        if (!active) {
          return
        }

        handleError(error)
        setLoadFailed(true)
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [])

  const resolveNickname = useCallback(async (nickname: string) => {
    const normalizedNickname = nickname.trim()
    const revision = searchRevision.current + 1

    searchRevision.current = revision
    setResolvedPlayer(null)
    setSearchFailed(false)

    if (!normalizedNickname) {
      setSearching(false)
      return
    }

    setSearching(true)

    try {
      const player = await resolvePlayerByNickname(normalizedNickname)

      if (searchRevision.current !== revision) {
        return
      }

      setResolvedPlayer(player)
    } catch (error) {
      if (searchRevision.current !== revision) {
        return
      }

      handleError(error)
      setSearchFailed(true)
    } finally {
      if (searchRevision.current === revision) {
        setSearching(false)
      }
    }
  }, [])

  const clearResolvedPlayer = useCallback(() => {
    searchRevision.current += 1
    setResolvedPlayer(null)
    setSearchFailed(false)
    setSearching(false)
  }, [])

  const subscribe = useCallback(
    async (playerId: string) => {
      if (mutationLocked.current) {
        return false
      }

      mutationLocked.current = true
      setBusyPlayerId(playerId)

      let succeeded = false

      try {
        await createPresenceSubscription(playerId)
        succeeded = true
      } catch (error) {
        handleError(error)
      }

      await refresh()

      mutationLocked.current = false
      setBusyPlayerId(null)

      return succeeded
    },
    [refresh],
  )

  const unsubscribe = useCallback(
    async (playerId: string) => {
      if (mutationLocked.current) {
        return false
      }

      mutationLocked.current = true
      setBusyPlayerId(playerId)

      let succeeded = false

      try {
        await deletePresenceSubscription(playerId)
        succeeded = true
      } catch (error) {
        handleError(error)
      }

      await refresh()

      mutationLocked.current = false
      setBusyPlayerId(null)

      return succeeded
    },
    [refresh],
  )

  return {
    subscriptions,
    resolvedPlayer,
    loading,
    loadFailed,
    searching,
    searchFailed,
    busyPlayerId,
    refresh,
    resolveNickname,
    clearResolvedPlayer,
    subscribe,
    unsubscribe,
  }
}
