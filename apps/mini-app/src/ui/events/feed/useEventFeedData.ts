import { useCallback, useEffect, useRef, useState } from "react"
import { getEventFeed, type EventFeedQuery } from "@/shared/api/eventApi"
import { handleError } from "@/shared/errors/errorHandler"
import { realtimeBroadcasts } from "@/shared/realtime/realtimeEventBus"
import { toFeedEvent } from "../../data/dataMappers"
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

function queryKey(query: EventFeedQuery): string {
  return JSON.stringify({
    types: query.types ?? null,
    search: query.search?.trim() ?? "",
    droppedFor: query.droppedFor?.trim() ?? "",
    itemRarity: query.itemRarity ?? null,
    itemType: query.itemType ?? null,
    boosterType: query.boosterType ?? null,
    globalType: query.globalType ?? null,
    raidLocation: query.raidLocation ?? null,
    wipeId: query.wipeId ?? null,
    sort: query.sort ?? "NEWEST",
    limit: query.limit ?? 50,
  })
}

function appendUniqueEvents(current: FeedEvent[], next: FeedEvent[]): FeedEvent[] {
  const knownIds = new Set(current.map((item) => item.id))
  const uniqueItems = next.filter((item) => !knownIds.has(item.id))

  return [...current, ...uniqueItems]
}

export function useEventFeedData(): EventFeedData {
  const [feed, setFeed] = useState<FeedEvent[]>([])
  const [eventCursor, setEventCursor] = useState<string | null>(null)
  const [eventsHasMore, setEventsHasMore] = useState(true)
  const [loadingEvents, setLoadingEvents] = useState(true)
  const [eventsError, setEventsError] = useState(false)
  const requestRevision = useRef(0)
  const queryKeyRef = useRef<string | null>(null)
  const activeQueryRef = useRef<EventFeedQuery>({})
  const feedRef = useRef<FeedEvent[]>([])
  const loadingRef = useRef(true)

  const refreshEvents = useCallback(async (query: EventFeedQuery = {}) => {
    const revision = requestRevision.current + 1
    const nextQueryKey = queryKey(query)
    const limit = query.limit ?? 50
    const targetItemCount = Math.max(feedRef.current.length, limit)

    requestRevision.current = revision
    activeQueryRef.current = query
    loadingRef.current = true
    setLoadingEvents(true)
    setEventsError(false)

    try {
      let after: string | null = null
      let nextFeed: FeedEvent[] = []
      let nextCursor: string | null = null
      let nextHasMore = true
      const seenCursors = new Set<string>()

      while (nextFeed.length < targetItemCount && nextHasMore) {
        const response = await getEventFeed({
          ...query,
          after,
        })

        if (revision !== requestRevision.current) {
          return
        }

        const pageItems = response.items.map(toFeedEvent)
        nextFeed = appendUniqueEvents(nextFeed, pageItems)
        nextCursor = response.cursor
        nextHasMore = pageItems.length >= limit && response.cursor !== null

        if (response.cursor === null || seenCursors.has(response.cursor)) {
          nextHasMore = false
          break
        }

        seenCursors.add(response.cursor)
        after = response.cursor
      }

      if (revision !== requestRevision.current) {
        return
      }

      queryKeyRef.current = nextQueryKey
      feedRef.current = nextFeed
      setFeed(nextFeed)
      setEventCursor(nextCursor)
      setEventsHasMore(nextHasMore)
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
      activeQueryRef.current = query
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

        const nextItems = response.items.map(toFeedEvent)

        setFeed((current) => {
          const merged = appendUniqueEvents(current, nextItems)
          feedRef.current = merged

          return merged
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

  useEffect(() => {
    return realtimeBroadcasts.subscribe((event) => {
      if (event.type !== "game-event" || queryKeyRef.current === null) {
        return
      }

      void refreshEvents(activeQueryRef.current)
    })
  }, [refreshEvents])

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
