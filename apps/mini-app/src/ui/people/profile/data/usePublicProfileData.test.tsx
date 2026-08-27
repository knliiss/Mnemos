// @vitest-environment jsdom

import { act, renderHook, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import {
  getAccountReaction,
  getPublicProfile,
  type PublicProfileResponse,
} from "@/shared/api/profileSocialApi"
import { getEventFeed } from "@/shared/api/eventApi"
import { usePublicProfileData } from "./usePublicProfileData"

vi.mock("@/shared/api/profileSocialApi", () => ({
  getAccountReaction: vi.fn(),
  getAccountReactionSummary: vi.fn(),
  getPublicProfile: vi.fn(),
  removeAccountReaction: vi.fn(),
  setAccountReaction: vi.fn(),
}))

vi.mock("@/shared/api/eventApi", () => ({
  getEventFeed: vi.fn(),
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

function profile(likes: number): PublicProfileResponse {
  return {
    accountId: "profile-account",
    cristalixUuid: "00000000-0000-0000-0000-000000000001",
    cristalixNickname: "ProfileOwner",
    likes,
    dislikes: 0,
    comments: 0,
    appearance: {
      nickStyle: null,
      profileTheme: null,
      avatarFrame: null,
      profileEffect: null,
    },
    presence: {
      status: "UNKNOWN",
      online: null,
      realm: null,
      lastSeenOnline: null,
      observedAt: null,
    },
    createdAt: "2026-08-20T00:00:00Z",
  }
}

describe("usePublicProfileData", () => {
  beforeEach(() => {
    vi.mocked(getPublicProfile).mockReset()
    vi.mocked(getAccountReaction).mockResolvedValue("LIKE")
    vi.mocked(getEventFeed).mockResolvedValue({ items: [], cursor: null })
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

  it("ignores the previous viewer scope before starting viewer-specific requests", async () => {
    const viewerAProfile = createDeferred<PublicProfileResponse>()
    const viewerBProfile = createDeferred<PublicProfileResponse>()

    vi.mocked(getPublicProfile)
      .mockReturnValueOnce(viewerAProfile.promise)
      .mockReturnValueOnce(viewerBProfile.promise)

    const { result, rerender } = renderHook(
      ({ viewerAccountId }: { viewerAccountId: string }) =>
        usePublicProfileData("profile-account", viewerAccountId),
      { initialProps: { viewerAccountId: "viewer-a" } },
    )

    rerender({ viewerAccountId: "viewer-b" })
    expect(result.current.profile).toBeNull()

    await act(async () => {
      viewerBProfile.resolve(profile(2))
      await viewerBProfile.promise
    })

    await waitFor(() => expect(result.current.profile?.likes).toBe(2))

    await act(async () => {
      viewerAProfile.resolve(profile(1))
      await viewerAProfile.promise
    })

    expect(result.current.profile?.likes).toBe(2)
    expect(getAccountReaction).toHaveBeenCalledTimes(1)
    expect(getEventFeed).toHaveBeenCalledTimes(1)
  })
})
