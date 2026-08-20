import { useCallback, useEffect, useLayoutEffect, useRef, useState } from "react"
import { ApiError } from "@/shared/api/httpClient"
import {
  getAccountReaction,
  getAccountReactionSummary,
  getPublicProfile,
  removeAccountReaction,
  setAccountReaction,
  type AccountReactionType,
  type PublicProfileResponse,
} from "@/shared/api/profileSocialApi"
import { getEventFeed } from "@/shared/api/eventApi"
import { handleError } from "@/shared/errors/errorHandler"
import { toFeedEvent } from "../../../data/dataMappers"
import type { FeedEvent } from "../../../model"

type PublicProfileData = {
  profile: PublicProfileResponse | null
  reaction: AccountReactionType | null
  drops: FeedEvent[]
  loading: boolean
  error: boolean
  reactionBusy: boolean
  refresh(): Promise<void>
  refreshProfileSummary(): Promise<void>
  toggleReaction(reaction: AccountReactionType): Promise<void>
}

export function usePublicProfileData(
  accountId: string,
  viewerAccountId: string,
): PublicProfileData {
  const [profile, setProfile] = useState<PublicProfileResponse | null>(null)
  const [reaction, setReaction] = useState<AccountReactionType | null>(null)
  const [drops, setDrops] = useState<FeedEvent[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [reactionBusyScopes, setReactionBusyScopes] = useState<Set<string>>(() => new Set())
  const [loadedScopeKey, setLoadedScopeKey] = useState<string | null>(null)
  const loadRevision = useRef(0)
  const summaryRevision = useRef(0)
  const reactionBusyScopesRef = useRef<Set<string>>(new Set())
  const mountedRef = useRef(true)
  const scopeKey = `${viewerAccountId}:${accountId}`
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

  const setReactionScopeBusy = useCallback((key: string, busy: boolean) => {
    const next = new Set(reactionBusyScopesRef.current)

    if (busy) {
      next.add(key)
    } else {
      next.delete(key)
    }

    reactionBusyScopesRef.current = next
    setReactionBusyScopes(next)
  }, [])

  const refreshProfileSummary = useCallback(async () => {
    if (!accountId) {
      return
    }

    const requestedScopeKey = scopeKey
    const revision = summaryRevision.current + 1

    summaryRevision.current = revision

    try {
      const nextProfile = await getPublicProfile(accountId)

      if (revision !== summaryRevision.current || scopeKeyRef.current !== requestedScopeKey) {
        return
      }

      setProfile(nextProfile)
    } catch (error) {
      if (revision === summaryRevision.current && scopeKeyRef.current === requestedScopeKey) {
        handleError(error)
      }
    }
  }, [accountId, scopeKey])

  const refresh = useCallback(async () => {
    const requestedScopeKey = scopeKey
    const revision = loadRevision.current + 1

    loadRevision.current = revision
    setLoading(true)
    setError(false)

    if (!accountId || !viewerAccountId) {
      setProfile(null)
      setReaction(null)
      setDrops([])
      setLoadedScopeKey(requestedScopeKey)
      setLoading(false)
      return
    }

    try {
      const nextProfile = await getPublicProfile(accountId)

      if (revision !== loadRevision.current || scopeKeyRef.current !== requestedScopeKey) {
        return
      }

      const [currentReaction, feedResponse] = await Promise.all([
        getAccountReaction(accountId),
        getEventFeed({
          types: ["ITEM_DROP"],
          droppedFor: nextProfile.cristalixNickname,
          sort: "NEWEST",
          limit: 3,
        }),
      ])

      if (revision !== loadRevision.current || scopeKeyRef.current !== requestedScopeKey) {
        return
      }

      setProfile(nextProfile)
      setReaction(currentReaction)
      setDrops(feedResponse.items.map(toFeedEvent).slice(0, 3))
      setLoadedScopeKey(requestedScopeKey)
    } catch (caught) {
      if (revision !== loadRevision.current || scopeKeyRef.current !== requestedScopeKey) {
        return
      }

      setProfile(null)
      setReaction(null)
      setDrops([])
      setLoadedScopeKey(requestedScopeKey)

      if (!(caught instanceof ApiError && caught.status === 404)) {
        setError(true)
        handleError(caught)
      }
    } finally {
      if (revision === loadRevision.current && scopeKeyRef.current === requestedScopeKey) {
        setLoading(false)
      }
    }
  }, [accountId, scopeKey, viewerAccountId])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => {
      loadRevision.current += 1
      summaryRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [refresh])

  const toggleReaction = useCallback(
    async (nextReaction: AccountReactionType) => {
      if (!profile || reactionBusyScopesRef.current.has(scopeKey)) {
        return
      }

      const requestedScopeKey = scopeKey
      const profileAccountId = profile.accountId

      setReactionScopeBusy(requestedScopeKey, true)

      try {
        if (reaction === nextReaction) {
          await removeAccountReaction(profileAccountId)
        } else {
          await setAccountReaction(profileAccountId, nextReaction)
        }

        const summary = await getAccountReactionSummary(profileAccountId)

        if (!mountedRef.current || scopeKeyRef.current !== requestedScopeKey) {
          return
        }

        setReaction(reaction === nextReaction ? null : nextReaction)
        setProfile((current) =>
          current?.accountId === profileAccountId ? { ...current, ...summary } : current,
        )
      } catch (error) {
        if (mountedRef.current && scopeKeyRef.current === requestedScopeKey) {
          handleError(error)
        }
      } finally {
        if (mountedRef.current) {
          setReactionScopeBusy(requestedScopeKey, false)
        }
      }
    },
    [profile, reaction, scopeKey, setReactionScopeBusy],
  )

  const current = loadedScopeKey === scopeKey

  return {
    profile: current ? profile : null,
    reaction: current ? reaction : null,
    drops: current ? drops : [],
    loading: loading || !current,
    error: current && error,
    reactionBusy: current && reactionBusyScopes.has(scopeKey),
    refresh,
    refreshProfileSummary,
    toggleReaction,
  }
}
