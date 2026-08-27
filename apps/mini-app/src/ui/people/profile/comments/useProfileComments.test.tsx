// @vitest-environment jsdom

import { act, renderHook, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import {
  editComment,
  getAccountComments,
  getPublicProfile,
  type PublicProfileResponse,
} from "@/shared/api/profileSocialApi"
import { useProfileComments } from "./useProfileComments"

vi.mock("@/shared/api/profileSocialApi", () => ({
  createComment: vi.fn(),
  deleteComment: vi.fn(),
  editComment: vi.fn(),
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
  presence: {
    status: "UNKNOWN",
    online: null,
    realm: null,
    lastSeenOnline: null,
    observedAt: null,
  },
  createdAt: "2026-08-20T00:00:00Z",
}

const otherProfile: PublicProfileResponse = {
  ...profile,
  accountId: "other-profile-account",
  cristalixUuid: "00000000-0000-0000-0000-000000000002",
  cristalixNickname: "OtherProfileOwner",
}

const commentResponse = {
  id: "comment",
  targetAccountId: profile.accountId,
  authorAccountId: "active-account",
  parentCommentId: null,
  replyToAuthorAccountId: null,
  content: "Stable comment",
  deleted: false,
  canEdit: true,
  canDelete: true,
  likes: 0,
  replies: 0,
  createdAt: "2026-08-20T00:00:00Z",
  updatedAt: "2026-08-20T00:00:00Z",
}

const commentsPage = {
  content: [commentResponse],
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
    vi.mocked(getAccountComments).mockReset()
    vi.mocked(getAccountComments).mockResolvedValue(commentsPage)
    vi.mocked(getPublicProfile).mockReset()
    vi.mocked(editComment).mockReset()
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

  it("updates an editable comment without refetching the profile thread", async () => {
    vi.mocked(editComment).mockResolvedValue({
      ...commentResponse,
      content: "Edited comment",
      updatedAt: "2026-08-20T00:01:00Z",
    })

    const refreshProfileSummary = vi.fn().mockResolvedValue(undefined)
    const { result } = renderHook(() =>
      useProfileComments({
        activeAccountId: "active-account",
        activeAccountNickname: "Viewer",
        profile,
        locale: "en-US",
        refreshProfileSummary,
      }),
    )

    await waitFor(() => expect(result.current.comments).toHaveLength(1))

    await act(async () => {
      await result.current.editProfileComment(result.current.comments[0], " Edited comment ")
    })

    expect(editComment).toHaveBeenCalledWith("comment", "Edited comment")
    expect(result.current.comments[0]?.content).toBe("Edited comment")
    expect(getAccountComments).toHaveBeenCalledTimes(1)
  })

  it("does not expose the previous profile comments when the next profile load fails", async () => {
    vi.mocked(getAccountComments)
      .mockResolvedValueOnce(commentsPage)
      .mockRejectedValueOnce(new Error("comments unavailable"))

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

    rerender({ currentProfile: otherProfile })

    await waitFor(() => {
      expect(result.current.commentsLoading).toBe(false)
      expect(result.current.commentsError).toBe(true)
    })

    expect(result.current.comments).toEqual([])
    expect(result.current.replies).toEqual({})
    expect(result.current.expandedReplies.size).toBe(0)
    expect(result.current.busyIds.size).toBe(0)
    expect(result.current.submitting).toBe(false)
  })
})
