import type { Dispatch, SetStateAction } from "react"
import { useCallback, useEffect, useRef, useState } from "react"
import {
  createComment,
  deleteComment,
  editComment,
  getCommentLikeCount,
  getCommentLikeState,
  likeComment,
  unlikeComment,
} from "@/shared/api/profileSocialApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { ProfileCommentView, ReplyTarget, ReplyThreadMap } from "../model"
import { mergeProfileComments, toProfileCommentView } from "./profileCommentMappers"
import type { ProfileCommentTarget } from "./profileCommentContext"

type Options = {
  activeAccountId: string
  activeAccountNickname: string
  targetProfile: ProfileCommentTarget | null
  locale: string
  commentScope: string | null
  scopeRevision: { current: number }
  setComments: Dispatch<SetStateAction<ProfileCommentView[]>>
  setReplies: Dispatch<SetStateAction<ReplyThreadMap>>
  setExpandedReplies: Dispatch<SetStateAction<Set<string>>>
  refreshProfileSummary(): Promise<void>
}

export function useProfileCommentMutations({
  activeAccountId,
  activeAccountNickname,
  targetProfile,
  locale,
  commentScope,
  scopeRevision,
  setComments,
  setReplies,
  setExpandedReplies,
  refreshProfileSummary,
}: Options) {
  const [busyIds, setBusyIds] = useState<Set<string>>(() => new Set())
  const [submitting, setSubmitting] = useState(false)
  const busyIdsRef = useRef<Set<string>>(new Set())
  const submittingRef = useRef(false)

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

  useEffect(() => {
    busyIdsRef.current = new Set()
    setBusyIds(new Set())
    submittingRef.current = false
    setSubmitting(false)
  }, [commentScope])

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
    [scopeRevision, setBusy, setComments, setReplies],
  )

  const editProfileComment = useCallback(
    async (comment: ProfileCommentView, content: string, rootCommentId?: string) => {
      const normalized = content.trim()

      if (
        !comment.canEdit ||
        !normalized ||
        normalized === comment.content ||
        busyIdsRef.current.has(comment.id)
      ) {
        return
      }

      const requestedScope = scopeRevision.current
      setBusy(comment.id, true)

      try {
        const updated = await editComment(comment.id, normalized)

        if (requestedScope !== scopeRevision.current) {
          return
        }

        const update = (value: ProfileCommentView) =>
          value.id === comment.id
            ? {
                ...value,
                content: updated.content ?? normalized,
                deleted: updated.deleted,
                canEdit: updated.canEdit,
                canDelete: updated.canDelete,
              }
            : value

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

        throw error
      } finally {
        if (requestedScope === scopeRevision.current) {
          setBusy(comment.id, false)
        }
      }
    },
    [scopeRevision, setBusy, setComments, setReplies],
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
    [refreshProfileSummary, scopeRevision, setBusy, setComments, setExpandedReplies, setReplies],
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
    [
      activeAccountId,
      activeAccountNickname,
      locale,
      refreshProfileSummary,
      scopeRevision,
      setComments,
      setExpandedReplies,
      setReplies,
      targetProfile,
    ],
  )

  return {
    busyIds,
    submitting,
    toggleCommentLike,
    editProfileComment,
    removeComment,
    submitComment,
  }
}
