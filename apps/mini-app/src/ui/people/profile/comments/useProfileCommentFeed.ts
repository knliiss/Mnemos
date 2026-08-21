import { useCallback, useEffect, useRef, useState } from "react"
import {
  getAccountComments,
  getCommentReplies,
} from "@/shared/api/profileSocialApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { ProfileCommentView, ReplyThreadMap } from "../model"
import {
  mergeProfileComments,
  resolveProfileCommentAuthorNames,
  toProfileCommentView,
} from "./profileCommentMappers"
import type {
  ProfileCommentAccountIdentity,
  ProfileCommentTarget,
} from "./profileCommentContext"

const ROOT_COMMENT_PAGE_SIZE = 20
const REPLY_PAGE_SIZE = 3

type Options = {
  activeAccount: ProfileCommentAccountIdentity
  targetProfile: ProfileCommentTarget | null
  locale: string
  commentScope: string | null
}

export function useProfileCommentFeed({
  activeAccount,
  targetProfile,
  locale,
  commentScope,
}: Options) {
  const [comments, setComments] = useState<ProfileCommentView[]>([])
  const [commentsPage, setCommentsPage] = useState(0)
  const [commentsHasMore, setCommentsHasMore] = useState(false)
  const [commentsLoading, setCommentsLoading] = useState(false)
  const [commentsError, setCommentsError] = useState(false)
  const [loadedCommentScope, setLoadedCommentScope] = useState<string | null>(null)
  const [replies, setReplies] = useState<ReplyThreadMap>({})
  const [expandedReplies, setExpandedReplies] = useState<Set<string>>(() => new Set())
  const scopeRevision = useRef(0)
  const rootRequestRevision = useRef(0)
  const replyRequestRevisions = useRef<Record<string, number>>({})
  const commentsLoadingRef = useRef(false)

  const resetCommentScope = useCallback((loadedScope: string | null) => {
    setComments([])
    setCommentsPage(0)
    setCommentsHasMore(false)
    setLoadedCommentScope(loadedScope)
    setReplies({})
    setExpandedReplies(new Set())
    replyRequestRevisions.current = {}
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

  return {
    comments,
    setComments,
    commentsHasMore,
    commentsLoading,
    commentsError,
    loadedCommentScope,
    replies,
    setReplies,
    expandedReplies,
    setExpandedReplies,
    scopeRevision,
    retryComments: loadInitialComments,
    loadMoreComments,
    toggleReplies,
    loadMoreReplies,
  }
}
