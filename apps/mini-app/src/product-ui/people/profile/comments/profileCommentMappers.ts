import {
  getPublicProfile,
  type CommentResponse,
  type PublicProfileResponse,
} from "@/shared/api/profileSocialApi"
import type { Account } from "../../../model"
import { formatOccurredAt } from "../../../timePresentation"
import type { ProfileCommentView } from "../model"
import { shortAccountId } from "../profilePresentation"

export async function resolveProfileCommentAuthorNames(
  comments: CommentResponse[],
  activeAccount: Account,
  targetProfile: PublicProfileResponse,
): Promise<Map<string, string>> {
  const result = new Map<string, string>([
    [activeAccount.id, activeAccount.nickname],
    [targetProfile.accountId, targetProfile.cristalixNickname],
  ])
  const unknownIds = Array.from(
    new Set(
      comments
        .flatMap((comment) => [comment.authorAccountId, comment.replyToAuthorAccountId])
        .filter((value): value is string => Boolean(value))
        .filter((id) => !result.has(id)),
    ),
  )
  const profiles = await Promise.allSettled(unknownIds.map((id) => getPublicProfile(id)))

  profiles.forEach((resolved, index) => {
    const id = unknownIds[index]

    if (resolved.status === "fulfilled") {
      result.set(resolved.value.accountId, resolved.value.cristalixNickname)
    } else {
      result.set(id, shortAccountId(id))
    }
  })

  return result
}

export function toProfileCommentView(
  comment: CommentResponse,
  authorNames: Map<string, string>,
  locale: string,
): ProfileCommentView {
  return {
    id: comment.id,
    authorAccountId: comment.authorAccountId,
    author: authorNames.get(comment.authorAccountId) ?? shortAccountId(comment.authorAccountId),
    replyToAuthor: comment.replyToAuthorAccountId
      ? (authorNames.get(comment.replyToAuthorAccountId) ??
        shortAccountId(comment.replyToAuthorAccountId))
      : null,
    content: comment.deleted ? "" : (comment.content ?? ""),
    deleted: comment.deleted,
    canDelete: comment.canDelete,
    likes: comment.likes,
    replies: comment.replies,
    createdAt: formatOccurredAt(comment.createdAt, locale),
    liked: false,
  }
}

export function mergeProfileComments(
  current: ProfileCommentView[],
  incoming: ProfileCommentView[],
): ProfileCommentView[] {
  const next = [...current]
  const indexes = new Map(next.map((comment, index) => [comment.id, index]))

  incoming.forEach((comment) => {
    const index = indexes.get(comment.id)

    if (index === undefined) {
      indexes.set(comment.id, next.length)
      next.push(comment)
      return
    }

    next[index] = comment
  })

  return next
}
