import { AnimatePresence, motion, useDragControls } from "framer-motion"
import {
  type FormEvent,
  type PointerEvent,
  useEffect,
  useRef,
  useState,
} from "react"
import { useTranslation } from "react-i18next"
import { Glyph } from "../../../shared/MnemosIcon"
import type {
  ProfileCommentView,
  ReplyTarget,
  ReplyThreadMap,
} from "../model"

type Props = {
  open: boolean
  profileNickname: string
  activeAccountNickname: string
  totalCount: number
  comments: ProfileCommentView[]
  commentsHasMore: boolean
  commentsLoading: boolean
  replies: ReplyThreadMap
  expandedReplies: Set<string>
  busyIds: Set<string>
  submitting: boolean
  onClose: () => void
  onLoadMoreComments: () => Promise<void>
  onToggleReplies: (comment: ProfileCommentView) => Promise<void>
  onLoadMoreReplies: (comment: ProfileCommentView) => Promise<void>
  onToggleLike: (comment: ProfileCommentView, rootCommentId?: string) => Promise<void>
  onDelete: (comment: ProfileCommentView, rootCommentId?: string) => Promise<void>
  onSubmit: (content: string, replyTarget: ReplyTarget | null) => Promise<void>
}

type CommentTone = "lime" | "plum" | "amber"

export function ProfileCommentsSheet({
  open,
  profileNickname,
  activeAccountNickname,
  totalCount,
  comments,
  commentsHasMore,
  commentsLoading,
  replies,
  expandedReplies,
  busyIds,
  submitting,
  onClose,
  onLoadMoreComments,
  onToggleReplies,
  onLoadMoreReplies,
  onToggleLike,
  onDelete,
  onSubmit,
}: Props) {
  const { t } = useTranslation()
  const [draft, setDraft] = useState("")
  const [replyingTo, setReplyingTo] = useState<ReplyTarget | null>(null)
  const composerInputRef = useRef<HTMLInputElement>(null)
  const dragControls = useDragControls()

  useEffect(() => {
    if (!open) {
      setReplyingTo(null)
      return
    }

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        onClose()
      }
    }

    window.addEventListener("keydown", handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener("keydown", handleKeyDown)
    }
  }, [onClose, open])

  const submit = async (event: FormEvent) => {
    event.preventDefault()

    const content = draft.trim()

    if (!content || submitting) {
      return
    }

    await onSubmit(content, replyingTo)
    setDraft("")
    setReplyingTo(null)
  }

  const startReply = (target: ReplyTarget) => {
    setReplyingTo(target)

    window.requestAnimationFrame(() => {
      composerInputRef.current?.focus()
    })
  }

  const startSheetDrag = (event: PointerEvent<HTMLButtonElement>) => {
    dragControls.start(event)
  }

  const handleScroll = (event: React.UIEvent<HTMLDivElement>) => {
    const element = event.currentTarget
    const remaining = element.scrollHeight - element.scrollTop - element.clientHeight

    if (remaining < 140 && commentsHasMore && !commentsLoading) {
      void onLoadMoreComments()
    }
  }

  return (
    <AnimatePresence>
      {open ? (
        <motion.div
          className="public-profile-comments-layer"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onMouseDown={(event) => {
            if (event.currentTarget === event.target) {
              onClose()
            }
          }}
        >
          <motion.section
            className="public-profile-comments-sheet"
            initial={{ y: "100%" }}
            animate={{ y: 0 }}
            exit={{ y: "100%" }}
            transition={{ duration: 0.28, ease: [0.22, 1, 0.36, 1] }}
            drag="y"
            dragListener={false}
            dragControls={dragControls}
            dragConstraints={{ top: 0, bottom: 280 }}
            dragElastic={{ top: 0, bottom: 0.18 }}
            dragSnapToOrigin
            onDragEnd={(_, info) => {
              if (info.offset.y > 110 || info.velocity.y > 720) {
                onClose()
              }
            }}
            aria-modal="true"
            role="dialog"
            aria-label={t("people.profile.comments")}
          >
            <button
              type="button"
              className="public-profile-comments-sheet__drag-zone"
              onPointerDown={startSheetDrag}
              aria-label={t("people.profile.dragToClose")}
            >
              <span className="public-profile-comments-sheet__grabber" />
            </button>

            <header className="public-profile-comments-sheet__header">
              <span className="public-profile-comments-sheet__mark" aria-hidden="true">
                <img src="/ui-illustrations/mnemos-comments.svg" alt="" />
              </span>

              <span className="public-profile-comments-sheet__title">
                <small>
                  {t("people.profile.profileLabel")} · {profileNickname}
                </small>
                <strong>
                  {t("people.profile.comments")} <b>{totalCount}</b>
                </strong>
              </span>

              <button
                type="button"
                className="public-profile-comments-sheet__close"
                onClick={onClose}
                aria-label={t("people.profile.closeComments")}
              >
                <Glyph name="close" size={15} />
              </button>
            </header>

            <div className="public-profile-comments-sheet__scroll" onScroll={handleScroll}>
              <AnimatePresence initial={false}>
                {comments.map((comment, index) => {
                  const thread = replies[comment.id]
                  const repliesExpanded = expandedReplies.has(comment.id)
                  const threadItems = thread?.items ?? []

                  return (
                    <motion.article
                      layout="position"
                      id={`profile-thread-${comment.id}`}
                      key={comment.id}
                      className={`public-profile-thread is-${commentTone(comment.author)}`}
                      initial={{ opacity: 0, y: 5 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, scale: 0.985 }}
                      transition={{ delay: Math.min(index * 0.025, 0.1) }}
                    >
                      <div className="public-profile-comment">
                        <CommentAvatar author={comment.author} />

                        <div className="public-profile-comment__body">
                          <header>
                            <strong>{comment.author}</strong>
                            <time>{comment.createdAt}</time>
                          </header>

                          <p>
                            {comment.deleted
                              ? t("people.profile.deletedComment")
                              : comment.content}
                          </p>

                          <footer>
                            {!comment.deleted ? (
                              <button
                                type="button"
                                onClick={() =>
                                  startReply({
                                    rootCommentId: comment.id,
                                    targetCommentId: null,
                                    author: comment.author,
                                  })
                                }
                              >
                                {t("people.profile.reply")}
                              </button>
                            ) : null}

                            {comment.replies > 0 ? (
                              <button
                                type="button"
                                className="public-profile-thread__toggle"
                                onClick={() => void onToggleReplies(comment)}
                              >
                                <i />
                                {repliesExpanded
                                  ? t("people.profile.hideReplies")
                                  : t("people.profile.showReplies", {
                                      count: comment.replies,
                                    })}
                              </button>
                            ) : null}
                          </footer>
                        </div>

                        <div className="public-profile-comment__actions">
                          {!comment.deleted ? (
                            <button
                              type="button"
                              className={`public-profile-comment__like ${comment.liked ? "is-active" : ""}`}
                              disabled={busyIds.has(comment.id)}
                              onClick={() => void onToggleLike(comment)}
                              aria-label={
                                comment.liked
                                  ? t("people.profile.unlike")
                                  : t("people.profile.like")
                              }
                            >
                              <Glyph name="like" size={15} />
                              <span>{comment.likes || ""}</span>
                            </button>
                          ) : null}

                          {comment.canDelete && !comment.deleted ? (
                            <button
                              type="button"
                              className="public-profile-comment__delete"
                              disabled={busyIds.has(comment.id)}
                              onClick={() => void onDelete(comment)}
                              aria-label={t("people.profile.deleteOwnComment")}
                            >
                              <img src="/ui-illustrations/mnemos-trash.svg" alt="" />
                            </button>
                          ) : null}
                        </div>
                      </div>

                      {repliesExpanded ? (
                        <motion.div
                          className="public-profile-replies"
                          initial={{ opacity: 0, y: -4 }}
                          animate={{ opacity: 1, y: 0 }}
                          transition={{ duration: 0.16 }}
                        >
                          {threadItems.map((reply) => (
                            <div
                              className={`public-profile-reply is-${commentTone(reply.author)}`}
                              key={reply.id}
                            >
                              <CommentAvatar author={reply.author} compact />

                              <div className="public-profile-reply__body">
                                <header>
                                  <strong>{reply.author}</strong>
                                  <time>{reply.createdAt}</time>
                                </header>

                                <p>
                                  {reply.replyToAuthor ? (
                                    <span className="public-profile-reply__mention">
                                      @{reply.replyToAuthor}
                                    </span>
                                  ) : null}
                                  {reply.deleted
                                    ? t("people.profile.deletedComment")
                                    : reply.content}
                                </p>

                                {!reply.deleted ? (
                                  <footer>
                                    <button
                                      type="button"
                                      onClick={() =>
                                        startReply({
                                          rootCommentId: comment.id,
                                          targetCommentId: reply.id,
                                          author: reply.author,
                                        })
                                      }
                                    >
                                      {t("people.profile.reply")}
                                    </button>
                                  </footer>
                                ) : null}
                              </div>

                              <div className="public-profile-reply__actions">
                                {!reply.deleted ? (
                                  <button
                                    type="button"
                                    className={`public-profile-reply__like ${reply.liked ? "is-active" : ""}`}
                                    disabled={busyIds.has(reply.id)}
                                    onClick={() => void onToggleLike(reply, comment.id)}
                                    aria-label={
                                      reply.liked
                                        ? t("people.profile.unlike")
                                        : t("people.profile.like")
                                    }
                                  >
                                    <Glyph name="like" size={12} />
                                    <span>{reply.likes || ""}</span>
                                  </button>
                                ) : null}

                                {reply.canDelete && !reply.deleted ? (
                                  <button
                                    type="button"
                                    className="public-profile-reply__delete"
                                    disabled={busyIds.has(reply.id)}
                                    onClick={() => void onDelete(reply, comment.id)}
                                    aria-label={t("people.profile.deleteOwnReply")}
                                  >
                                    <img src="/ui-illustrations/mnemos-trash.svg" alt="" />
                                  </button>
                                ) : null}
                              </div>
                            </div>
                          ))}

                          {thread?.loading ? (
                            <span className="public-profile-replies__loading">…</span>
                          ) : null}

                          {thread?.hasMore && !thread.loading ? (
                            <button
                              type="button"
                              className="public-profile-replies__more"
                              onClick={() => void onLoadMoreReplies(comment)}
                            >
                              <i />
                              {t("people.profile.showMoreReplies")}
                            </button>
                          ) : null}
                        </motion.div>
                      ) : null}
                    </motion.article>
                  )
                })}
              </AnimatePresence>

              {!comments.length && !commentsLoading ? (
                <div className="public-profile-comments-sheet__empty">
                  <img src="/ui-illustrations/mnemos-comments.svg" alt="" />
                  <strong>{t("people.profile.noComments")}</strong>
                  <small>{t("people.profile.firstComment")}</small>
                </div>
              ) : null}

              {commentsLoading ? (
                <span className="public-profile-comments-sheet__loading">…</span>
              ) : null}
            </div>

            <div className="public-profile-comments-sheet__composer-wrap">
              <AnimatePresence initial={false}>
                {replyingTo ? (
                  <motion.div
                    className="public-profile-reply-context"
                    initial={{ opacity: 0, y: 4 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: 4 }}
                  >
                    <span>
                      {t("people.profile.replyingTo")} <strong>@{replyingTo.author}</strong>
                    </span>
                    <button
                      type="button"
                      onClick={() => setReplyingTo(null)}
                      aria-label={t("people.profile.cancelReply")}
                    >
                      <Glyph name="close" size={12} />
                    </button>
                  </motion.div>
                ) : null}
              </AnimatePresence>

              <form className="public-profile-composer" onSubmit={(event) => void submit(event)}>
                <CommentAvatar author={activeAccountNickname} compact />

                <input
                  ref={composerInputRef}
                  value={draft}
                  maxLength={500}
                  disabled={submitting}
                  onChange={(event) => setDraft(event.target.value)}
                  placeholder={
                    replyingTo
                      ? t("people.profile.replyPlaceholder", {
                          author: replyingTo.author,
                        })
                      : t("people.profile.writePlaceholder")
                  }
                />

                <motion.button
                  type="submit"
                  whileTap={{ scale: 0.94 }}
                  disabled={!draft.trim() || submitting}
                  aria-label={t("people.profile.send")}
                >
                  <Glyph name="send" size={15} />
                </motion.button>
              </form>
            </div>
          </motion.section>
        </motion.div>
      ) : null}
    </AnimatePresence>
  )
}

function CommentAvatar({ author, compact = false }: { author: string; compact?: boolean }) {
  return (
    <span
      className={`public-profile-comment-avatar is-${commentTone(author)} ${compact ? "is-compact" : ""}`}
      aria-hidden="true"
    >
      <span className="public-profile-comment-avatar__halo" />
      <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" />
      <i />
    </span>
  )
}

function commentTone(author: string): CommentTone {
  const tones: CommentTone[] = ["lime", "plum", "amber"]
  const score = Array.from(author).reduce(
    (sum, character) => sum + character.charCodeAt(0),
    0,
  )

  return tones[score % tones.length]
}
