import { useCallback, useEffect, useMemo, useRef, useState } from "react"
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
  const [comments, setComments] = useState<ProfileCommentView[]>([])
  const [commentsPage, setCommentsPage] = useState(0)
  const [commentsHasMore, setCommentsHasMore] = useState(false)
  const [commentsLoading, setCommentsLoading] = useState(false)
  const [commentsError, setCommentsError] = useState(false)
  const [loadedCommentScope, setLoadedCommentScope] = useState<string | null>(null)
  const [replies, setReplies] = useState<ReplyThreadMap>({})
  const [expandedReplies, setExpandedReplies] = useState<Set<string>>(() => new Set())
  const [busyIds, setBusyIds] = useState<Set<string>>(() => new Set())
  const [submitting, setSubmitting] = useState(false)
  const scopeRevision = useRef(0)
  const rootRequestRevision = useRef(0)
  const replyRequestRevisions = useRef<Record<string, number>>({})
  const commentsLoadingRef = useRef(false)
  const busyIdsRef = useRef<Set<string>>(new Set())
  const submittingRef = useRef(false)
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

  const setBusy = useCallback((id: string, busy: boolean) => {
    const next = new Set(busyIdsRef.current)

    if (busy) {
      next.add(id)
    } else {
      next.delete(id)
    }

    busyIdsRef.current = next
    setBusyIds(next)
  }, [])

  const resetCommentScope = useCallback((loadedScope: string | null) => {
    setComments([])
    setCommentsPage(0)
    setCommentsHasMore(false)
    setLoadedCommentScope(loadedScope)
    setReplies({})
    setExpandedReplies(new Set())
    replyRequestRevisions.current = {}
    busyIdsRef.current = new Set()
    setBusyIds(new Set())
    submittingRef.current = false
    setSubmitting(false)
  }, [])

  const loadInitialComments = useCallback(async () => {
    const requestedScope = scopeRevision.current + 1
    const revision = rootRequestRevision.current + 1

    scopeRevision.current = requestedScope
    rootRequestRevision.current = revision

    if (!targetProfile) {
      resetCommentScope(null)
      setCommentsError(false)
      commentsLoadingRef.current = false
      setCommentsLoading(false)
      return
    }

    commentsLoadingRef.current = true
    setCommentsLoading(true)
    setCommentsError(false)

    try {
      const page = await getAccountComments(targetProfile.accountId, 0, ROOT_COMMENT_PAGE_SIZE)
      const authorNames = await resolveProfileCommentAuthorNames(
        page.content,
        activeAccount,
        targetProfile,
      )

      if (requestedScope !== scopeRevision.current || revision !== rootRequestRevision.current) {
        return
      }

      setComments(page.content.map((comment) => toProfileCommentView(comment, authorNames, locale)))
      setCommentsPage(page.number)
      setCommentsHasMore(!page.last)
      setLoadedCommentScope(commentScope)
      setReplies({})
      setExpandedReplies(new Set())
      replyRequestRevisions.current = {}
      busyIdsRef.current = new Set()
      setBusyIds(new Set())
      submittingRef.current = false
      setSubmitting(false)
    } catch (error) {
      if (requestedScope === scopeRevision.current && revision === rootRequestRevision.current) {
        resetCommentScope(commentScope)
        setCommentsError(true)
        handleError(error)
      }
    } finally {
      if (requestedScope === scopeRevision.current && revision === rootRequestRevision.current) {
        commentsLoadingRef.current = false
        setCommentsLoading(false)
      }
    }
  }, [activeAccount, commentScope, locale, resetCommentScope, targetProfile])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void loadInitialComments()
    })

    return () => {
      scopeRevision.current += 1
      rootRequestRevision.current += 1
      commentsLoadingRef.current = false
      busyIdsRef.current = new Set()
      submittingRef.current = false
      window.cancelAnimationFrame(frame)
    }
  }, [loadInitialComments])

  const loadMoreComments = useCallback(async () => {
    if (!targetProfile || commentsLoadingRef.current || !commentsHasMore) {
      return
    }

    const nextPage = commentsPage + 1
    const requestedScope = scopeRevision.current
    const revision = rootRequestRevision.current + 1

    rootRequestRevision.current = revision
    commentsLoadingRef.current = true
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

      if (requestedScope !== scopeRevision.current || revision !== rootRequestRevision.current) {
        return
      }

      setComments((current) => mergeProfileComments(current, nextItems))
      setCommentsPage(page.number)
      setCommentsHasMore(!page.last)
    } catch (error) {
      if (requestedScope === scopeRevision.current && revision === rootRequestRevision.current) {
        handleError(error)
      }
    } finally {
      if (requestedScope === scopeRevision.current && revision === rootRequestRevision.current) {
        commentsLoadingRef.current = false
        setCommentsLoading(false)
      }
    }
  }, [activeAccount, commentsHasMore, commentsPage, locale, targetProfile])

  const fetchReplyPage = useCallback(
    async (root: ProfileCommentView, pageNumber: number, append: boolean) => {
      if (!targetProfile) {
        return
      }

      const requestedScope = scopeRevision.current
      const requestRevision = (replyRequestRevisions.current[root.id] ?? 0) + 1

      replyRequestRevisions.current[root.id] = requestRevision

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

        if (
          requestedScope !== scopeRevision.current ||
          replyRequestRevisions.current[root.id] !== requestRevision
        ) {
          return
        }

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
        if (
          requestedScope !== scopeRevision.current ||
          replyRequestRevisions.current[root.id] !== requestRevision
        ) {
          return
        }

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
      if (busyIdsRef.current.has(comment.id)) {
        return
      }

      const requestedScope = scopeRevision.current
      setBusy(comment.id, true)

      try {
        const liked = await getCommentLikeState(comment.id)

        if (liked) {
          await unlikeComment(comment.id)
        } else {
          await likeComment(comment.id)
        }

        const likes = await getCommentLikeCount(comment.id)

        if (requestedScope !== scopeRevision.current) {
          return
        }

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
        if (requestedScope === scopeRevision.current) {
          handleError(error)
        }
      } finally {
        if (requestedScope === scopeRevision.current) {
          setBusy(comment.id, false)
        }
      }
    },
    [setBusy],
  )

  const removeComment = useCallback(
    async (comment: ProfileCommentView, rootCommentId?: string) => {
      if (!comment.canDelete || busyIdsRef.current.has(comment.id)) {
        return
      }

      const requestedScope = scopeRevision.current
      setBusy(comment.id, true)

      try {
        await deleteComment(comment.id)

        if (requestedScope !== scopeRevision.current) {
          return
        }

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
        if (requestedScope === scopeRevision.current) {
          handleError(error)
        }
      } finally {
        if (requestedScope === scopeRevision.current) {
          setBusy(comment.id, false)
        }
      }
    },
    [refreshProfileSummary, setBusy],
  )

  const submitComment = useCallback(
    async (content: string, replyTarget: ReplyTarget | null) => {
      if (!targetProfile || submittingRef.current) {
        return
      }

      const requestedScope = scopeRevision.current
      submittingRef.current = true
      setSubmitting(true)

      try {
        const created = await createComment(
          targetProfile.accountId,
          content,
          replyTarget?.rootCommentId ?? null,
          replyTarget?.targetCommentId ?? null,
        )

        if (requestedScope !== scopeRevision.current) {
          return
        }

        const authorNames = new Map<string, string>([
          [activeAccountId, activeAccountNickname],
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
        if (requestedScope === scopeRevision.current) {
          handleError(error)
        }
        throw error
      } finally {
        if (requestedScope === scopeRevision.current) {
          submittingRef.current = false
          setSubmitting(false)
        }
      }
    },
    [activeAccountId, activeAccountNickname, locale, refreshProfileSummary, targetProfile],
  )

  const hasCommentScope = commentScope !== null
  const current = hasCommentScope && loadedCommentScope === commentScope
  const currentComments = current ? comments : []
  const currentReplies = current ? replies : {}
  const currentExpandedReplies = current ? expandedReplies : new Set<string>()
  const currentBusyIds = current ? busyIds : new Set<string>()
  const latestComment = useMemo(
    () => (current ? (comments.find((comment) => !comment.deleted) ?? null) : null),
    [comments, current],
  )

  return {
    comments: currentComments,
    commentsHasMore: current && commentsHasMore,
    commentsLoading: hasCommentScope && (commentsLoading || !current),
    commentsError: current && commentsError,
    replies: currentReplies,
    expandedReplies: currentExpandedReplies,
    busyIds: currentBusyIds,
    submitting: current && submitting,
    latestComment,
    retryComments: loadInitialComments,
    loadMoreComments,
    toggleReplies,
    loadMoreReplies,
    toggleCommentLike,
    removeComment,
    submitComment,
  }
}
