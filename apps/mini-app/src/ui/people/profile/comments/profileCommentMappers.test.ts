import { describe, expect, it } from "vitest"
import type { CommentResponse } from "@/shared/api/profileSocialApi"
import { toProfileCommentView } from "./profileCommentMappers"

describe("profileCommentMappers", () => {
  it("preserves backend moderation capabilities", () => {
    const comment: CommentResponse = {
      id: "comment-1",
      targetAccountId: "target-account",
      authorAccountId: "author-account",
      parentCommentId: null,
      replyToAuthorAccountId: null,
      content: "Comment",
      deleted: false,
      canEdit: false,
      canDelete: true,
      likes: 4,
      replies: 2,
      createdAt: "2026-08-20T12:00:00Z",
      updatedAt: "2026-08-20T12:00:00Z",
    }

    const view = toProfileCommentView(
      comment,
      new Map([["author-account", "Author"]]),
      "en",
    )

    expect(view.canEdit).toBe(false)
    expect(view.canDelete).toBe(true)
  })
})
