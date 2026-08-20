import { useCallback, useRef, useState } from "react"
import { getEventFeed, type EventFeedQuery } from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { FeedEvent } from "../model"
import { toProductFeedEvent } from "./productMappers"

type EventFeedData = {
  feed: FeedEvent[]
  eventCursor: string | null
  eventsHasMore: boolean
  loadingEvents: boolean
  refreshEvents(query?: EventFeedQuery): Promise<void>
  loadMoreEvents(query?: EventFeedQuery): Promise<void>
}

export function useEventFeedData(): EventFeedData {
  const [feed, setFeed] = useState<FeedEvent[]>([])
  const [eventCursor, setEventCursor] = useState<string | null>(null)
  const [eventsHasMore, setEventsHasMore] = useState(true)
  const [loadingEvents, setLoadingEvents] = useState(false)
  const requestRevision = useRef(0)

  const refreshEvents = useCallback(async (query: EventFeedQuery = {}) => {
    const revision = requestRevision.current + 1
    requestRevision.current = revision
    setLoadingEvents(true)

    try {
      const limit = query.limit ?? 50
      const response = await getEventFeed({
        ...query,
        after: null,
      })

      if (revision !== requestRevision.current) {
        return
      }

      setFeed(response.items.map(toProductFeedEvent))
      setEventCursor(response.cursor)
      setEventsHasMore(response.items.length >= limit && response.cursor !== null)
    } catch (error) {
      if (revision === requestRevision.current) {
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current) {
        setLoadingEvents(false)
      }
    }
  }, [])

  const loadMoreEvents = useCallback(
    async (query: EventFeedQuery = {}) => {
      if (loadingEvents || !eventsHasMore || !eventCursor) {
        return
      }

      const revision = requestRevision.current + 1
      requestRevision.current = revision
      setLoadingEvents(true)

      try {
        const limit = query.limit ?? 50
        const response = await getEventFeed({
          ...query,
          after: eventCursor,
        })

        if (revision !== requestRevision.current) {
          return
        }

        const nextItems = response.items.map(toProductFeedEvent)

        setFeed((current) => {
          const knownIds = new Set(current.map((item) => item.id))
          const uniqueItems = nextItems.filter((item) => !knownIds.has(item.id))

          return [...current, ...uniqueItems]
        })
        setEventCursor(response.cursor)
        setEventsHasMore(nextItems.length >= limit && response.cursor !== null)
      } catch (error) {
        if (revision === requestRevision.current) {
          handleError(error)
        }
      } finally {
        if (revision === requestRevision.current) {
          setLoadingEvents(false)
        }
      }
    },
    [eventCursor, eventsHasMore, loadingEvents],
  )

  return {
    feed,
    eventCursor,
    eventsHasMore,
    loadingEvents,
    refreshEvents,
    loadMoreEvents,
  }
}
