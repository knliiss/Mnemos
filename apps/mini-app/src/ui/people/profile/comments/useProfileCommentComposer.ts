import { useCallback, useRef, useState } from "react"
import type { ProfileCommentView, ReplyTarget } from "../model"

export type ProfileCommentEditingTarget = {
  comment: ProfileCommentView
  rootCommentId?: string
}

type Options = {
  busyIds: Set<string>
  submitting: boolean
  onEdit(comment: ProfileCommentView, content: string, rootCommentId?: string): Promise<void>
  onSubmit(content: string, replyTarget: ReplyTarget | null): Promise<void>
}

export function useProfileCommentComposer({ busyIds, submitting, onEdit, onSubmit }: Options) {
  const [draft, setDraft] = useState("")
  const [replyingTo, setReplyingTo] = useState<ReplyTarget | null>(null)
  const [editing, setEditing] = useState<ProfileCommentEditingTarget | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)
  const busy = submitting || (editing ? busyIds.has(editing.comment.id) : false)

  const reset = useCallback(() => {
    setDraft("")
    setReplyingTo(null)
    setEditing(null)
  }, [])

  const focus = () => {
    window.requestAnimationFrame(() => {
      inputRef.current?.focus()
    })
  }

  const startReply = (target: ReplyTarget) => {
    setEditing(null)
    setDraft("")
    setReplyingTo(target)
    focus()
  }

  const startEdit = (comment: ProfileCommentView, rootCommentId?: string) => {
    if (!comment.canEdit || busyIds.has(comment.id)) {
      return
    }

    setReplyingTo(null)
    setEditing({ comment, rootCommentId })
    setDraft(comment.content)
    focus()
  }

  const submit = async () => {
    const content = draft.trim()

    if (!content || busy) {
      return
    }

    try {
      if (editing) {
        await onEdit(editing.comment, content, editing.rootCommentId)
      } else {
        await onSubmit(content, replyingTo)
      }
    } catch {
      return
    }

    reset()
  }

  return {
    draft,
    setDraft,
    replyingTo,
    editing,
    inputRef,
    busy,
    reset,
    startReply,
    startEdit,
    submit,
  }
}
