import { useCallback, useEffect, useMemo, useState } from "react"
import {
  createComment,
  deleteComment,
  getAccountComments,
  getCommentLikeCount,
  getCommentLikeState,
  getCommentReplies,
  likeComment,
  unlikeComment,
  type PublicProfileResponse,
} from "@/shared/api/profileSocialApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { Account } from "../../../model"
import type { ProfileCommentView, ReplyTarget, ReplyThreadMap } from "../model"
import {
  mergeProfileComments,
  resolveProfileCommentAuthorNames,
  toProfileCommentView,
} from "./profileCommentMappers"

const ROOT_COMMENT_PAGE_SIZE = 20
const REPLY_PAGE_SIZE = 3

type ProfileCommentsController = {
  comments: ProfileCommentView[]
  commentsHasMore: boolean
  commentsLoading: boolean
  replies: ReplyThreadMap
  expandedReplies: Set<string>
  busyIds: Set<string>
  submitting: boolean
  latestComment: ProfileCommentView | null
  loadMoreComments(): Promise<void>
  toggleReplies(comment: ProfileCommentView): Promise<void>
  loadMoreReplies(comment: ProfileCommentView): Promise<void>
  toggleCommentLike(comment: ProfileCommentView, rootCommentId?: string): Promise<void>
  removeComment(comment: ProfileCommentView, rootCommentId?: string): Promise<void>
  submitComment(content: string, replyTarget: ReplyTarget | null): Promise<void>
}

type Options = {
  activeAccount: Account
  profile: PublicProfileResponse | null
  locale: string
  refreshProfileSummary(): Promise<void>
}

export function useProfileComments({
  activeAccount,
  profile,
  locale,
  refreshProfileSummary,
}: Options): ProfileCommentsController {
  const [comments, setComments] = useState<ProfileCommentView[]>([])
  const [commentsPage, setCommentsPage] = useState(0)
  const [commentsHasMore, setCommentsHasMore] = useState(false)
  const [commentsLoading, setCommentsLoading] = useState(false)
  const [replies, setReplies] = useState<ReplyThreadMap>({})
  const [expandedReplies, setExpandedReplies] = useState<Set<string>>(() => new Set())
  const [busyIds, setBusyIds] = useState<Set<string>>(() => new Set())
  const [submitting, setSubmitting] = useState(false)
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

  const setBusy = useCallback((id: string, busy: boolean) => {
    setBusyIds((current) => {
      const next = new Set(current)

      if (busy) {
        next.add(id)
      } else {
        next.delete(id)
      }

      return next
    })
  }, [])

  const loadInitialComments = useCallback(async () => {
    if (!targetProfile) {
      setComments([])
      setCommentsPage(0)
      setCommentsHasMore(false)
      setReplies({})
      setExpandedReplies(new Set())
      return
    }

    setCommentsLoading(true)

    try {
      const page = await getAccountComments(targetProfile.accountId, 0, ROOT_COMMENT_PAGE_SIZE)
      const authorNames = await resolveProfileCommentAuthorNames(
        page.content,
        activeAccount,
        targetProfile,
      )

      setComments(page.content.map((comment) => toProfileCommentView(comment, authorNames, locale)))
      setCommentsPage(page.number)
      setCommentsHasMore(!page.last)
      setReplies({})
      setExpandedReplies(new Set())
    } catch (error) {
      handleError(error)
    } finally {
      setCommentsLoading(false)
    }
  }, [activeAccount, locale, targetProfile])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void loadInitialComments()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [loadInitialComments])

  const loadMoreComments = useCallback(async () => {
    if (!targetProfile || commentsLoading || !commentsHasMore) {
      return
    }

    const nextPage = commentsPage + 1
    setCommentsLoading(true)

    try {
      const page = await getAccountComments(
        targetProfile.accountId,
        nextPage,
        ROOT_COMMENT_PAGE_SIZE,
      )
      const authorNames = await resolveProfileCommentAuthorNames(
        page.content,
        activeAccount,
        targetProfile,
      )
      const nextItems = page.content.map((comment) =>
        toProfileCommentView(comment, authorNames, locale),
      )

      setComments((current) => mergeProfileComments(current, nextItems))
      setCommentsPage(page.number)
      setCommentsHasMore(!page.last)
    } catch (error) {
      handleError(error)
    } finally {
      setCommentsLoading(false)
    }
  }, [activeAccount, commentsHasMore, commentsLoading, commentsPage, locale, targetProfile])

  const fetchReplyPage = useCallback(
    async (root: ProfileCommentView, pageNumber: number, append: boolean) => {
      if (!targetProfile) {
        return
      }

      setReplies((current) => ({
        ...current,
        [root.id]: {
          items: current[root.id]?.items ?? [],
          page: current[root.id]?.page ?? 0,
          hasMore: current[root.id]?.hasMore ?? true,
          loading: true,
        },
      }))

      try {
        const page = await getCommentReplies(root.id, pageNumber, REPLY_PAGE_SIZE)
        const authorNames = await resolveProfileCommentAuthorNames(
          page.content,
          activeAccount,
          targetProfile,
        )
        const nextItems = page.content.map((comment) =>
          toProfileCommentView(comment, authorNames, locale),
        )

        setReplies((current) => ({
          ...current,
          [root.id]: {
            items: append
              ? mergeProfileComments(current[root.id]?.items ?? [], nextItems)
              : nextItems,
            page: page.number,
            hasMore: !page.last,
            loading: false,
          },
        }))
      } catch (error) {
        handleError(error)
        setReplies((current) => ({
          ...current,
          [root.id]: {
            items: current[root.id]?.items ?? [],
            page: current[root.id]?.page ?? 0,
            hasMore: current[root.id]?.hasMore ?? false,
            loading: false,
          },
        }))
      }
    },
    [activeAccount, locale, targetProfile],
  )

  const toggleReplies = useCallback(
    async (comment: ProfileCommentView) => {
      const isExpanded = expandedReplies.has(comment.id)

      setExpandedReplies((current) => {
        const next = new Set(current)

        if (next.has(comment.id)) {
          next.delete(comment.id)
        } else {
          next.add(comment.id)
        }

        return next
      })

      if (!isExpanded && !replies[comment.id]) {
        await fetchReplyPage(comment, 0, false)
      }
    },
    [expandedReplies, fetchReplyPage, replies],
  )

  const loadMoreReplies = useCallback(
    async (comment: ProfileCommentView) => {
      const thread = replies[comment.id]

      if (!thread || thread.loading || !thread.hasMore) {
        return
      }

      await fetchReplyPage(comment, thread.page + 1, true)
    },
    [fetchReplyPage, replies],
  )

  const toggleCommentLike = useCallback(
    async (comment: ProfileCommentView, rootCommentId?: string) => {
      if (busyIds.has(comment.id)) {
        return
      }

      setBusy(comment.id, true)

      try {
        const liked = await getCommentLikeState(comment.id)

        if (liked) {
          await unlikeComment(comment.id)
        } else {
          await likeComment(comment.id)
        }

        const likes = await getCommentLikeCount(comment.id)
        const update = (value: ProfileCommentView) =>
          value.id === comment.id ? { ...value, liked: !liked, likes } : value

        if (rootCommentId) {
          setReplies((current) => {
            const thread = current[rootCommentId]

            if (!thread) {
              return current
            }

            return {
              ...current,
              [rootCommentId]: {
                ...thread,
                items: thread.items.map(update),
              },
            }
          })
        } else {
          setComments((current) => current.map(update))
        }
      } catch (error) {
        handleError(error)
      } finally {
        setBusy(comment.id, false)
      }
    },
    [busyIds, setBusy],
  )

  const removeComment = useCallback(
    async (comment: ProfileCommentView, rootCommentId?: string) => {
      if (!comment.canDelete || busyIds.has(comment.id)) {
        return
      }

      setBusy(comment.id, true)

      try {
        await deleteComment(comment.id)

        if (rootCommentId) {
          setReplies((current) => {
            const thread = current[rootCommentId]

            if (!thread) {
              return current
            }

            return {
              ...current,
              [rootCommentId]: {
                ...thread,
                items: thread.items.filter((item) => item.id !== comment.id),
              },
            }
          })
          setComments((current) =>
            current.map((root) =>
              root.id === rootCommentId
                ? { ...root, replies: Math.max(0, root.replies - 1) }
                : root,
            ),
          )
        } else {
          setComments((current) => current.filter((item) => item.id !== comment.id))
          setReplies((current) => {
            const next = { ...current }

            delete next[comment.id]

            return next
          })
          setExpandedReplies((current) => {
            const next = new Set(current)

            next.delete(comment.id)

            return next
          })
        }

        await refreshProfileSummary()
      } catch (error) {
        handleError(error)
      } finally {
        setBusy(comment.id, false)
      }
    },
    [busyIds, refreshProfileSummary, setBusy],
  )

  const submitComment = useCallback(
    async (content: string, replyTarget: ReplyTarget | null) => {
      if (!targetProfile || submitting) {
        return
      }

      setSubmitting(true)

      try {
        const created = await createComment(
          targetProfile.accountId,
          content,
          replyTarget?.rootCommentId ?? null,
          replyTarget?.targetCommentId ?? null,
        )
        const authorNames = new Map<string, string>([
          [activeAccount.id, activeAccount.nickname],
          [targetProfile.accountId, targetProfile.cristalixNickname],
        ])

        if (created.replyToAuthorAccountId && replyTarget) {
          authorNames.set(created.replyToAuthorAccountId, replyTarget.author)
        }

        const view = toProfileCommentView(created, authorNames, locale)

        if (replyTarget) {
          setReplies((current) => {
            const thread = current[replyTarget.rootCommentId]

            return {
              ...current,
              [replyTarget.rootCommentId]: {
                items: mergeProfileComments(thread?.items ?? [], [view]),
                page: thread?.page ?? 0,
                hasMore: thread?.hasMore ?? false,
                loading: false,
              },
            }
          })
          setComments((current) =>
            current.map((comment) =>
              comment.id === replyTarget.rootCommentId
                ? { ...comment, replies: comment.replies + 1 }
                : comment,
            ),
          )
          setExpandedReplies((current) => new Set(current).add(replyTarget.rootCommentId))
        } else {
          setComments((current) => [view, ...current])
        }

        await refreshProfileSummary()
      } catch (error) {
        handleError(error)
        throw error
      } finally {
        setSubmitting(false)
      }
    },
    [activeAccount, locale, refreshProfileSummary, submitting, targetProfile],
  )

  const latestComment = useMemo(
    () => comments.find((comment) => !comment.deleted) ?? null,
    [comments],
  )

  return {
    comments,
    commentsHasMore,
    commentsLoading,
    replies,
    expandedReplies,
    busyIds,
    submitting,
    latestComment,
    loadMoreComments,
    toggleReplies,
    loadMoreReplies,
    toggleCommentLike,
    removeComment,
    submitComment,
  }
}
