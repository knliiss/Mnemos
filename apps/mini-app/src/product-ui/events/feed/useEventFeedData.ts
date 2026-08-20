import { useCallback, useEffect, useRef, useState } from "react"
import { getEventFeed, type EventFeedQuery } from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { toProductFeedEvent } from "../../data/productMappers"
import type { FeedEvent } from "../../model"

type EventFeedData = {
  feed: FeedEvent[]
  eventCursor: string | null
  eventsHasMore: boolean
  loadingEvents: boolean
  eventsError: boolean
  refreshEvents(query?: EventFeedQuery): Promise<void>
  loadMoreEvents(query?: EventFeedQuery): Promise<void>
}

export function useEventFeedData(): EventFeedData {
  const [feed, setFeed] = useState<FeedEvent[]>([])
  const [eventCursor, setEventCursor] = useState<string | null>(null)
  const [eventsHasMore, setEventsHasMore] = useState(true)
  const [loadingEvents, setLoadingEvents] = useState(true)
  const [eventsError, setEventsError] = useState(false)
  const requestRevision = useRef(0)
  const queryKeyRef = useRef<string | null>(null)
  const loadingRef = useRef(true)

  const refreshEvents = useCallback(async (query: EventFeedQuery = {}) => {
    const revision = requestRevision.current + 1
    const queryKey = JSON.stringify({
      types: query.types ?? null,
      search: query.search?.trim() ?? "",
      droppedFor: query.droppedFor?.trim() ?? "",
      itemRarity: query.itemRarity ?? null,
      sort: query.sort ?? "NEWEST",
      limit: query.limit ?? 50,
    })

    requestRevision.current = revision
    loadingRef.current = true
    setLoadingEvents(true)
    setEventsError(false)

    if (queryKeyRef.current !== queryKey) {
      queryKeyRef.current = queryKey
      setFeed([])
      setEventCursor(null)
      setEventsHasMore(true)
    }

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
        setEventsError(true)
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current) {
        loadingRef.current = false
        setLoadingEvents(false)
      }
    }
  }, [])

  const loadMoreEvents = useCallback(
    async (query: EventFeedQuery = {}) => {
      if (loadingRef.current || !eventsHasMore || !eventCursor) {
        return
      }

      const revision = requestRevision.current + 1
      requestRevision.current = revision
      loadingRef.current = true
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
          loadingRef.current = false
          setLoadingEvents(false)
        }
      }
    },
    [eventCursor, eventsHasMore],
  )

  useEffect(
    () => () => {
      requestRevision.current += 1
      loadingRef.current = false
    },
    [],
  )

  return {
    feed,
    eventCursor,
    eventsHasMore,
    loadingEvents,
    eventsError,
    refreshEvents,
    loadMoreEvents,
  }
}
