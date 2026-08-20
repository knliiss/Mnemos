import { useEffect, useState } from "react"
import { getEventFeed } from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { toProductFeedEvent } from "../../data/productMappers"
import type { FeedEvent } from "../../model"

export function useLatestAccountDrops(accountNickname: string | null, enabled: boolean) {
  const [drops, setDrops] = useState<FeedEvent[]>([])
  const [loadedNickname, setLoadedNickname] = useState<string | null>(null)

  useEffect(() => {
    if (!enabled || !accountNickname) {
      return
    }

    let cancelled = false
    void getEventFeed({
      types: ["ITEM_DROP"],
      droppedFor: accountNickname,
      sort: "NEWEST",
      limit: 3,
      after: null,
    })
      .then((response) => {
        if (cancelled) {
          return
        }

        setDrops(response.items.map(toProductFeedEvent))
        setLoadedNickname(accountNickname)
      })
      .catch((error) => {
        if (cancelled) {
          return
        }

        setDrops([])
        setLoadedNickname(accountNickname)
        handleError(error)
      })

    return () => {
      cancelled = true
    }
  }, [accountNickname, enabled])

  const current = enabled && accountNickname === loadedNickname

  return {
    drops: current ? drops : [],
    loading: Boolean(enabled && accountNickname && !current),
  }
}
