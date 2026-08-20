// @vitest-environment jsdom

import { renderHook, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import {
  getAccountComments,
  getPublicProfile,
  type PublicProfileResponse,
} from "@/shared/api/profileSocialApi"
import { useProfileComments } from "./useProfileComments"

vi.mock("@/shared/api/profileSocialApi", () => ({
  createComment: vi.fn(),
  deleteComment: vi.fn(),
  getAccountComments: vi.fn(),
  getCommentLikeCount: vi.fn(),
  getCommentLikeState: vi.fn(),
  getCommentReplies: vi.fn(),
  getPublicProfile: vi.fn(),
  likeComment: vi.fn(),
  unlikeComment: vi.fn(),
}))

const profile: PublicProfileResponse = {
  accountId: "profile-account",
  cristalixUuid: "00000000-0000-0000-0000-000000000001",
  cristalixNickname: "ProfileOwner",
  likes: 1,
  dislikes: 0,
  comments: 1,
  appearance: {
    nickStyle: null,
    profileTheme: null,
    avatarFrame: null,
    profileEffect: null,
  },
  createdAt: "2026-08-20T00:00:00Z",
}

const commentsPage = {
  content: [
    {
      id: "comment",
      targetAccountId: profile.accountId,
      authorAccountId: "active-account",
      parentCommentId: null,
      replyToAuthorAccountId: null,
      content: "Stable comment",
      deleted: false,
      canDelete: true,
      likes: 0,
      replies: 0,
      createdAt: "2026-08-20T00:00:00Z",
      updatedAt: "2026-08-20T00:00:00Z",
    },
  ],
  totalElements: 1,
  totalPages: 1,
  number: 0,
  size: 20,
  numberOfElements: 1,
  first: true,
  last: true,
  empty: false,
}

describe("useProfileComments", () => {
  beforeEach(() => {
    vi.mocked(getAccountComments).mockResolvedValue(commentsPage)
    vi.mocked(getPublicProfile).mockReset()
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

  it("does not reset or refetch comments when an unrelated profile summary changes", async () => {
    const refreshProfileSummary = vi.fn().mockResolvedValue(undefined)
    const { result, rerender } = renderHook(
      ({ currentProfile }: { currentProfile: PublicProfileResponse }) =>
        useProfileComments({
          activeAccountId: "active-account",
          activeAccountNickname: "Viewer",
          profile: currentProfile,
          locale: "en-US",
          refreshProfileSummary,
        }),
      { initialProps: { currentProfile: profile } },
    )

    await waitFor(() => expect(result.current.comments).toHaveLength(1))

    rerender({ currentProfile: { ...profile, likes: profile.likes + 1 } })

    expect(result.current.comments[0]?.content).toBe("Stable comment")
    expect(result.current.commentsLoading).toBe(false)
    expect(getAccountComments).toHaveBeenCalledTimes(1)
  })
})
