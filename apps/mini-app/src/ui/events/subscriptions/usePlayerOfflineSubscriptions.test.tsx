// @vitest-environment jsdom

import { act, renderHook, waitFor } from "@testing-library/react"
import { beforeEach, describe, expect, it, vi } from "vitest"
import {
  createPresenceSubscription,
  deletePresenceSubscription,
  listPresenceSubscriptions,
  resolvePlayerByNickname,
} from "@/shared/api/playerDirectoryApi"
import { handleError } from "@/shared/errors/errorHandler"
import { usePlayerOfflineSubscriptions } from "./usePlayerOfflineSubscriptions"

vi.mock("@/shared/api/playerDirectoryApi", () => ({
  createPresenceSubscription: vi.fn(),
  deletePresenceSubscription: vi.fn(),
  listPresenceSubscriptions: vi.fn(),
  resolvePlayerByNickname: vi.fn(),
}))

vi.mock("@/shared/errors/errorHandler", () => ({
  handleError: vi.fn(),
}))

const unknownPlayer = {
  playerId: "0198a322-692c-7d14-b7c2-f0d7af607bd1",
  nickname: "UnknownPlayer",
  presenceStatus: "UNKNOWN" as const,
  realm: null,
  realmType: null,
  privacyHidden: false,
  lastSeenOnline: null,
  observedAt: "2026-08-27T16:00:00Z",
}

const offlineSubscription = {
  id: "0198a323-692c-7d14-b7c2-f0d7af607bd2",
  playerId: "0198a324-692c-7d14-b7c2-f0d7af607bd3",
  nickname: "TrackedPlayer",
  presenceStatus: "OFFLINE" as const,
  realm: null,
  lastSeenOnline: "2026-08-27T15:58:00Z",
  createdAt: "2026-08-27T15:59:00Z",
}

describe("usePlayerOfflineSubscriptions", () => {
  beforeEach(() => {
    vi.mocked(createPresenceSubscription).mockReset()
    vi.mocked(deletePresenceSubscription).mockReset()
    vi.mocked(listPresenceSubscriptions).mockReset()
    vi.mocked(resolvePlayerByNickname).mockReset()
    vi.mocked(handleError).mockReset()
  })

  it("keeps rejected subscriptions out of local state and reconciles with the backend", async () => {
    const rejection = new Error("presence is not eligible")

    vi.mocked(listPresenceSubscriptions).mockResolvedValue([])
    vi.mocked(resolvePlayerByNickname).mockResolvedValue(unknownPlayer)
    vi.mocked(createPresenceSubscription).mockRejectedValue(rejection)

    const { result } = renderHook(() => usePlayerOfflineSubscriptions())

    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => {
      await result.current.resolveNickname("UnknownPlayer")
    })

    await act(async () => {
      await expect(result.current.subscribe(unknownPlayer.playerId)).resolves.toBe(false)
    })

    expect(createPresenceSubscription).toHaveBeenCalledWith(unknownPlayer.playerId)
    expect(listPresenceSubscriptions).toHaveBeenCalledTimes(2)
    expect(result.current.subscriptions).toEqual([])
    expect(handleError).toHaveBeenCalledWith(rejection)
  })

  it("replaces the local list with the authoritative list after a successful subscription", async () => {
    vi.mocked(listPresenceSubscriptions)
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([offlineSubscription])
    vi.mocked(createPresenceSubscription).mockResolvedValue(offlineSubscription)

    const { result } = renderHook(() => usePlayerOfflineSubscriptions())

    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => {
      await expect(result.current.subscribe(offlineSubscription.playerId)).resolves.toBe(true)
    })

    expect(result.current.subscriptions).toEqual([offlineSubscription])
  })
})
