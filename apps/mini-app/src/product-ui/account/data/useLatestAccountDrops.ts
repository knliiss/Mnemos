import { useEffect, useState } from "react"
import { getEventFeed } from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { toProductFeedEvent } from "../../data/productMappers"
import type { FeedEvent } from "../../model"

export function useLatestAccountDrops(accountNickname: string | null, enabled: boolean) {
  const [drops, setDrops] = useState<FeedEvent[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!enabled || !accountNickname) {
      setDrops([])
      return
    }

    let cancelled = false
    setLoading(true)

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
      })
      .catch(handleError)
      .finally(() => {
        if (!cancelled) {
          setLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [accountNickname, enabled])

  return {
    drops,
    loading,
  }
}
