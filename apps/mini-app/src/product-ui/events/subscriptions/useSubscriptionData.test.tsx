// @vitest-environment jsdom

import { act, renderHook, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { deleteSubscription, getSubscriptions } from "@/shared/api/productApi"
import { useSubscriptionData } from "./useSubscriptionData"

vi.mock("@/shared/api/productApi", () => ({
  deleteSubscription: vi.fn(),
  getSubscriptions: vi.fn(),
}))

type Deferred<T> = {
  promise: Promise<T>
  resolve(value: T): void
}

function createDeferred<T>(): Deferred<T> {
  let resolve!: (value: T) => void
  const promise = new Promise<T>((promiseResolve) => {
    resolve = promiseResolve
  })

  return { promise, resolve }
}

function subscriptionPage(id: string) {
  return {
    items: [
      {
        id,
        type: "DUNGEON" as const,
        createdAt: "2026-08-20T00:00:00Z",
        details: { dungeonType: "EASY" as const },
      },
    ],
    page: 0,
    size: 100,
    totalItems: 1,
    totalPages: 1,
    hasNextPage: false,
  }
}

describe("useSubscriptionData", () => {
  beforeEach(() => {
    vi.mocked(getSubscriptions).mockReset()
    vi.mocked(deleteSubscription).mockReset()
    vi.stubGlobal("requestAnimationFrame", (callback: FrameRequestCallback) => {
      callback(0)
      return 1
    })
    vi.stubGlobal("cancelAnimationFrame", vi.fn())
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  it("ignores a GET started before a successful mutation", async () => {
    const staleRefresh = createDeferred<ReturnType<typeof subscriptionPage>>()
    const reconciledRefresh = createDeferred<ReturnType<typeof subscriptionPage>>()

    vi.mocked(getSubscriptions)
      .mockReturnValueOnce(staleRefresh.promise)
      .mockReturnValueOnce(reconciledRefresh.promise)
    vi.mocked(deleteSubscription).mockResolvedValue()

    const { result } = renderHook(() => useSubscriptionData())

    let mutation!: Promise<boolean>
    act(() => {
      mutation = result.current.removeSubscription("removed-subscription")
    })

    await waitFor(() => expect(getSubscriptions).toHaveBeenCalledTimes(2))

    await act(async () => {
      reconciledRefresh.resolve(subscriptionPage("current-subscription"))
      await expect(mutation).resolves.toBe(true)
    })

    expect(result.current.subscriptions.map(({ id }) => id)).toEqual([
      "current-subscription",
    ])

    await act(async () => {
      staleRefresh.resolve(subscriptionPage("stale-subscription"))
      await staleRefresh.promise
    })

    expect(result.current.subscriptions.map(({ id }) => id)).toEqual([
      "current-subscription",
    ])
    expect(result.current.loadingSubscriptions).toBe(false)
  })
})
