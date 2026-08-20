import { useCallback, useEffect, useState } from "react"
import {
  getAccountReaction,
  getAccountReactionSummary,
  getPublicProfile,
  removeAccountReaction,
  setAccountReaction,
  type AccountReactionType,
  type PublicProfileResponse,
} from "@/shared/api/profileSocialApi"
import { getEventFeed } from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { toProductFeedEvent } from "../../../data/productMappers"
import type { FeedEvent } from "../../../model"

type PublicProfileData = {
  profile: PublicProfileResponse | null
  reaction: AccountReactionType | null
  drops: FeedEvent[]
  loading: boolean
  reactionBusy: boolean
  refreshProfileSummary(): Promise<void>
  toggleReaction(reaction: AccountReactionType): Promise<void>
}

export function usePublicProfileData(accountId: string): PublicProfileData {
  const [profile, setProfile] = useState<PublicProfileResponse | null>(null)
  const [reaction, setReaction] = useState<AccountReactionType | null>(null)
  const [drops, setDrops] = useState<FeedEvent[]>([])
  const [loading, setLoading] = useState(true)
  const [reactionBusy, setReactionBusy] = useState(false)

  const refreshProfileSummary = useCallback(async () => {
    if (!accountId) {
      return
    }

    try {
      setProfile(await getPublicProfile(accountId))
    } catch (error) {
      handleError(error)
    }
  }, [accountId])

  const load = useCallback(async () => {
    if (!accountId) {
      setProfile(null)
      setReaction(null)
      setDrops([])
      setLoading(false)
      return
    }

    setLoading(true)

    try {
      const nextProfile = await getPublicProfile(accountId)
      const [currentReaction, feedResponse] = await Promise.all([
        getAccountReaction(accountId),
        getEventFeed({
          types: ["ITEM_DROP"],
          droppedFor: nextProfile.cristalixNickname,
          sort: "NEWEST",
          limit: 3,
        }),
      ])

      setProfile(nextProfile)
      setReaction(currentReaction)
      setDrops(feedResponse.items.map(toProductFeedEvent).slice(0, 3))
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [accountId])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void load()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [load])

  const toggleReaction = useCallback(
    async (nextReaction: AccountReactionType) => {
      if (!profile || reactionBusy) {
        return
      }

      setReactionBusy(true)

      try {
        if (reaction === nextReaction) {
          await removeAccountReaction(profile.accountId)
          setReaction(null)
        } else {
          await setAccountReaction(profile.accountId, nextReaction)
          setReaction(nextReaction)
        }

        const summary = await getAccountReactionSummary(profile.accountId)
        setProfile((current) => (current ? { ...current, ...summary } : current))
      } catch (error) {
        handleError(error)
      } finally {
        setReactionBusy(false)
      }
    },
    [profile, reaction, reactionBusy],
  )

  return {
    profile,
    reaction,
    drops,
    loading,
    reactionBusy,
    refreshProfileSummary,
    toggleReaction,
  }
}
