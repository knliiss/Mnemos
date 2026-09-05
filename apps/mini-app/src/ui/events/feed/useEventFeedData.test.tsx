// @vitest-environment jsdom

import { act, renderHook, waitFor } from "@testing-library/react"
import { beforeEach, describe, expect, it, vi } from "vitest"
import { getEventFeed } from "@/shared/api/eventApi"
import { useEventFeedData } from "./useEventFeedData"

vi.mock("@/shared/api/eventApi", () => ({
  getEventFeed: vi.fn(),
}))

vi.mock("../../data/dataMappers", () => ({
  toFeedEvent: (event: unknown) => event,
}))

vi.mock("@/shared/errors/errorHandler", () => ({
  handleError: vi.fn(),
}))

vi.mock("@/shared/realtime/realtimeEventBus", () => ({
  realtimeBroadcasts: {
    subscribe: () => () => undefined,
  },
}))

type TestEvent = {
  id: string
}

function page(ids: string[], cursor: string | null) {
  return {
    items: ids.map((id) => ({ id }) as never),
    cursor,
  }
}

beforeEach(() => {
  vi.mocked(getEventFeed).mockReset()
})

describe("useEventFeedData", () => {
  it("rebuilds the already loaded depth before replacing the feed on refresh", async () => {
    vi.mocked(getEventFeed)
      .mockResolvedValueOnce(page(["1", "2"], "cursor-1"))
      .mockResolvedValueOnce(page(["3", "4"], "cursor-2"))
      .mockResolvedValueOnce(page(["new", "1"], "cursor-refresh-1"))
      .mockResolvedValueOnce(page(["2", "3"], "cursor-refresh-2"))

    const { result } = renderHook(() => useEventFeedData())
    const query = { limit: 2 }

    await act(async () => {
      await result.current.refreshEvents(query)
    })

    await waitFor(() => {
      expect(result.current.feed.map((event) => (event as TestEvent).id)).toEqual(["1", "2"])
    })

    await act(async () => {
      await result.current.loadMoreEvents(query)
    })

    expect(result.current.feed.map((event) => (event as TestEvent).id)).toEqual([
      "1",
      "2",
      "3",
      "4",
    ])

    await act(async () => {
      await result.current.refreshEvents(query)
    })

    expect(result.current.feed.map((event) => (event as TestEvent).id)).toEqual([
      "new",
      "1",
      "2",
      "3",
    ])
    expect(getEventFeed).toHaveBeenNthCalledWith(3, { limit: 2, after: null })
    expect(getEventFeed).toHaveBeenNthCalledWith(4, {
      limit: 2,
      after: "cursor-refresh-1",
    })
  })

  it("keeps the existing feed mounted while a changed query is loading", async () => {
    let resolveChangedQuery!: (value: ReturnType<typeof page>) => void
    const changedQuery = new Promise<ReturnType<typeof page>>((resolve) => {
      resolveChangedQuery = resolve
    })

    vi.mocked(getEventFeed)
      .mockResolvedValueOnce(page(["1", "2"], "cursor-1"))
      .mockReturnValueOnce(changedQuery)

    const { result } = renderHook(() => useEventFeedData())

    await act(async () => {
      await result.current.refreshEvents({ limit: 2 })
    })

    let refreshPromise: Promise<void>
    act(() => {
      refreshPromise = result.current.refreshEvents({ limit: 2, search: "moon" })
    })

    expect(result.current.feed.map((event) => (event as TestEvent).id)).toEqual(["1", "2"])

    await act(async () => {
      resolveChangedQuery(page(["5"], null))
      await refreshPromise!
    })

    expect(result.current.feed.map((event) => (event as TestEvent).id)).toEqual(["5"])
  })
})
