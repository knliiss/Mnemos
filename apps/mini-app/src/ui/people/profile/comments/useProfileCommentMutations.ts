import type { Dispatch, SetStateAction } from "react"
import { useCallback, useRef, useState } from "react"
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

type BusyState = {
  scope: string | null
  ids: Set<string>
}

type SubmissionState = {
  scope: string | null
  active: boolean
}

const EMPTY_BUSY_IDS = new Set<string>()

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
  const [busyState, setBusyState] = useState<BusyState>(() => ({
    scope: commentScope,
    ids: new Set(),
  }))
  const [submissionState, setSubmissionState] = useState<SubmissionState>(() => ({
    scope: commentScope,
    active: false,
  }))
  const busyStateRef = useRef<BusyState>({
    scope: commentScope,
    ids: new Set(),
  })
  const submissionStateRef = useRef<SubmissionState>({
    scope: commentScope,
    active: false,
  })

  const busyIds = busyState.scope === commentScope ? busyState.ids : EMPTY_BUSY_IDS
  const submitting = submissionState.scope === commentScope && submissionState.active

  const isBusy = useCallback(
    (id: string) =>
      busyStateRef.current.scope === commentScope && busyStateRef.current.ids.has(id),
    [commentScope],
  )

  const setBusy = useCallback(
    (id: string, busy: boolean) => {
      const currentIds =
        busyStateRef.current.scope === commentScope ? busyStateRef.current.ids : EMPTY_BUSY_IDS
      const nextIds = new Set(currentIds)

      if (busy) {
        nextIds.add(id)
      } else {
        nextIds.delete(id)
      }

      const nextState = {
        scope: commentScope,
        ids: nextIds,
      }

      busyStateRef.current = nextState
      setBusyState(nextState)
    },
    [commentScope],
  )

  const toggleCommentLike = useCallback(
    async (comment: ProfileCommentView, rootCommentId?: string) => {
      if (isBusy(comment.id)) {
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
    [isBusy, scopeRevision, setBusy, setComments, setReplies],
  )

  const editProfileComment = useCallback(
    async (comment: ProfileCommentView, content: string, rootCommentId?: string) => {
      const normalized = content.trim()

      if (!comment.canEdit || !normalized || normalized === comment.content || isBusy(comment.id)) {
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
    [isBusy, scopeRevision, setBusy, setComments, setReplies],
  )

  const removeComment = useCallback(
    async (comment: ProfileCommentView, rootCommentId?: string) => {
      if (!comment.canDelete || isBusy(comment.id)) {
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
    [
      isBusy,
      refreshProfileSummary,
      scopeRevision,
      setBusy,
      setComments,
      setExpandedReplies,
      setReplies,
    ],
  )

  const submitComment = useCallback(
    async (content: string, replyTarget: ReplyTarget | null) => {
      const alreadySubmitting =
        submissionStateRef.current.scope === commentScope && submissionStateRef.current.active

      if (!targetProfile || alreadySubmitting) {
        return
      }

      const requestedScope = scopeRevision.current
      const activeSubmission = {
        scope: commentScope,
        active: true,
      }

      submissionStateRef.current = activeSubmission
      setSubmissionState(activeSubmission)

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
          const inactiveSubmission = {
            scope: commentScope,
            active: false,
          }

          submissionStateRef.current = inactiveSubmission
          setSubmissionState(inactiveSubmission)
        }
      }
    },
    [
      activeAccountId,
      activeAccountNickname,
      commentScope,
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
