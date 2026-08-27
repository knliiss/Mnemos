// @vitest-environment jsdom

import { act, renderHook } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import {
  searchPublicProfiles,
  type PublicProfileResponse,
  type SpringPageResponse,
} from "@/shared/api/profileSocialApi"
import { useProfileSearch } from "./useProfileSearch"

vi.mock("@/shared/api/profileSocialApi", () => ({
  searchPublicProfiles: vi.fn(),
}))

vi.mock("@/shared/errors/errorHandler", () => ({
  handleError: vi.fn(),
}))

function profile(accountId: string, nickname: string): PublicProfileResponse {
  return {
    accountId,
    cristalixUuid: "00000000-0000-0000-0000-000000000001",
    cristalixNickname: nickname,
    likes: 0,
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
    createdAt: "2026-08-23T00:00:00Z",
  }
}

function page(items: PublicProfileResponse[]): SpringPageResponse<PublicProfileResponse> {
  return {
    content: items,
    totalElements: items.length,
    totalPages: 1,
    number: 0,
    size: 20,
    numberOfElements: items.length,
    first: true,
    last: true,
    empty: items.length === 0,
  }
}

describe("useProfileSearch", () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.mocked(searchPublicProfiles).mockReset()
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.restoreAllMocks()
  })

  it("clears previous results immediately when the normalized query changes", async () => {
    vi.mocked(searchPublicProfiles).mockResolvedValueOnce(page([profile("account-a", "Knalis")]))

    const { result } = renderHook(() => useProfileSearch())

    act(() => {
      result.current.changeQuery("kn")
    })

    await act(async () => {
      await vi.advanceTimersByTimeAsync(260)
    })

    expect(result.current.results).toHaveLength(1)

    act(() => {
      result.current.changeQuery("kna")
    })

    expect(result.current.results).toEqual([])
    expect(result.current.searching).toBe(true)
  })

  it("keeps valid results when only surrounding whitespace changes", async () => {
    vi.mocked(searchPublicProfiles).mockResolvedValueOnce(page([profile("account-a", "Knalis")]))

    const { result } = renderHook(() => useProfileSearch())

    act(() => {
      result.current.changeQuery("kn")
    })

    await act(async () => {
      await vi.advanceTimersByTimeAsync(260)
    })

    expect(result.current.results).toHaveLength(1)

    act(() => {
      result.current.changeQuery(" kn ")
    })

    expect(result.current.normalizedQuery).toBe("kn")
    expect(result.current.results).toHaveLength(1)
    expect(result.current.searching).toBe(false)
    expect(searchPublicProfiles).toHaveBeenCalledTimes(1)
  })
})
