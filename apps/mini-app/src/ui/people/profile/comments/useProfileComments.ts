import { useMemo } from "react"
import type { PublicProfileResponse } from "@/shared/api/profileSocialApi"
import type { ProfileCommentView, ReplyTarget, ReplyThreadMap } from "../model"
import { useProfileCommentFeed } from "./useProfileCommentFeed"
import { useProfileCommentMutations } from "./useProfileCommentMutations"

type ProfileCommentsController = {
  comments: ProfileCommentView[]
  commentsHasMore: boolean
  commentsLoading: boolean
  commentsError: boolean
  replies: ReplyThreadMap
  expandedReplies: Set<string>
  busyIds: Set<string>
  submitting: boolean
  latestComment: ProfileCommentView | null
  retryComments(): Promise<void>
  loadMoreComments(): Promise<void>
  toggleReplies(comment: ProfileCommentView): Promise<void>
  loadMoreReplies(comment: ProfileCommentView): Promise<void>
  toggleCommentLike(comment: ProfileCommentView, rootCommentId?: string): Promise<void>
  editProfileComment(
    comment: ProfileCommentView,
    content: string,
    rootCommentId?: string,
  ): Promise<void>
  removeComment(comment: ProfileCommentView, rootCommentId?: string): Promise<void>
  submitComment(content: string, replyTarget: ReplyTarget | null): Promise<void>
}

type Options = {
  activeAccountId: string
  activeAccountNickname: string
  profile: PublicProfileResponse | null
  locale: string
  refreshProfileSummary(): Promise<void>
}

export function useProfileComments({
  activeAccountId,
  activeAccountNickname,
  profile,
  locale,
  refreshProfileSummary,
}: Options): ProfileCommentsController {
  const activeAccount = useMemo(
    () => ({ id: activeAccountId, nickname: activeAccountNickname }),
    [activeAccountId, activeAccountNickname],
  )
  const profileAccountId = profile?.accountId ?? null
  const profileNickname = profile?.cristalixNickname ?? null
  const targetProfile = useMemo(
    () =>
      profileAccountId && profileNickname
        ? {
            accountId: profileAccountId,
            cristalixNickname: profileNickname,
          }
        : null,
    [profileAccountId, profileNickname],
  )
  const commentScope = targetProfile
    ? `${activeAccountId}:${activeAccountNickname}:${targetProfile.accountId}:${targetProfile.cristalixNickname}:${locale}`
    : null

  const feed = useProfileCommentFeed({
    activeAccount,
    targetProfile,
    locale,
    commentScope,
  })
  const mutations = useProfileCommentMutations({
    activeAccountId,
    activeAccountNickname,
    targetProfile,
    locale,
    commentScope,
    scopeRevision: feed.scopeRevision,
    setComments: feed.setComments,
    setReplies: feed.setReplies,
    setExpandedReplies: feed.setExpandedReplies,
    refreshProfileSummary,
  })

  const hasCommentScope = commentScope !== null
  const current = hasCommentScope && feed.loadedCommentScope === commentScope
  const latestComment = useMemo(
    () => (current ? (feed.comments.find((comment) => !comment.deleted) ?? null) : null),
    [current, feed.comments],
  )

  return {
    comments: current ? feed.comments : [],
    commentsHasMore: current && feed.commentsHasMore,
    commentsLoading: hasCommentScope && (feed.commentsLoading || !current),
    commentsError: current && feed.commentsError,
    replies: current ? feed.replies : {},
    expandedReplies: current ? feed.expandedReplies : new Set<string>(),
    busyIds: current ? mutations.busyIds : new Set<string>(),
    submitting: current && mutations.submitting,
    latestComment,
    retryComments: feed.retryComments,
    loadMoreComments: feed.loadMoreComments,
    toggleReplies: feed.toggleReplies,
    loadMoreReplies: feed.loadMoreReplies,
    toggleCommentLike: mutations.toggleCommentLike,
    editProfileComment: mutations.editProfileComment,
    removeComment: mutations.removeComment,
    submitComment: mutations.submitComment,
  }
}
