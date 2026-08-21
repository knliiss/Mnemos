import { AnimatePresence, motion, useDragControls } from "framer-motion"
import type { PointerEvent, UIEvent } from "react"
import { useCallback, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { Glyph } from "../../../shared/MnemosIcon"
import type { ProfileCommentView, ReplyTarget, ReplyThreadMap } from "../model"
import { ProfileCommentComposer } from "./ProfileCommentComposer"
import { ProfileCommentThread } from "./ProfileCommentThread"
import { useProfileCommentComposer } from "./useProfileCommentComposer"

type Props = {
  open: boolean
  profileNickname: string
  activeAccountNickname: string
  totalCount: number
  comments: ProfileCommentView[]
  commentsHasMore: boolean
  commentsLoading: boolean
  commentsError: boolean
  replies: ReplyThreadMap
  expandedReplies: Set<string>
  busyIds: Set<string>
  submitting: boolean
  onClose: () => void
  onLoadMoreComments: () => Promise<void>
  onRetryComments: () => Promise<void>
  onToggleReplies: (comment: ProfileCommentView) => Promise<void>
  onLoadMoreReplies: (comment: ProfileCommentView) => Promise<void>
  onToggleLike: (comment: ProfileCommentView, rootCommentId?: string) => Promise<void>
  onEdit: (comment: ProfileCommentView, content: string, rootCommentId?: string) => Promise<void>
  onDelete: (comment: ProfileCommentView, rootCommentId?: string) => Promise<void>
  onSubmit: (content: string, replyTarget: ReplyTarget | null) => Promise<void>
}

export function ProfileCommentsSheet({
  open,
  profileNickname,
  activeAccountNickname,
  totalCount,
  comments,
  commentsHasMore,
  commentsLoading,
  commentsError,
  replies,
  expandedReplies,
  busyIds,
  submitting,
  onClose,
  onLoadMoreComments,
  onRetryComments,
  onToggleReplies,
  onLoadMoreReplies,
  onToggleLike,
  onEdit,
  onDelete,
  onSubmit,
}: Props) {
  const { t } = useTranslation()
  const dragControls = useDragControls()
  const composer = useProfileCommentComposer({
    busyIds,
    submitting,
    onEdit,
    onSubmit,
  })

  const close = useCallback(() => {
    composer.reset()
    onClose()
  }, [composer, onClose])

  useEffect(() => {
    if (!open) {
      return
    }

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        close()
      }
    }

    window.addEventListener("keydown", handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener("keydown", handleKeyDown)
    }
  }, [close, open])

  const startSheetDrag = (event: PointerEvent<HTMLButtonElement>) => {
    dragControls.start(event)
  }

  const handleScroll = (event: UIEvent<HTMLDivElement>) => {
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
              close()
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
                close()
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
                onClick={close}
                aria-label={t("people.profile.closeComments")}
              >
                <Glyph name="close" size={15} />
              </button>
            </header>

            <div className="public-profile-comments-sheet__scroll" onScroll={handleScroll}>
              <AnimatePresence initial={false}>
                {comments.map((comment, index) => (
                  <ProfileCommentThread
                    key={comment.id}
                    comment={comment}
                    index={index}
                    thread={replies[comment.id]}
                    repliesExpanded={expandedReplies.has(comment.id)}
                    busyIds={busyIds}
                    onStartReply={composer.startReply}
                    onStartEdit={composer.startEdit}
                    onToggleReplies={onToggleReplies}
                    onLoadMoreReplies={onLoadMoreReplies}
                    onToggleLike={onToggleLike}
                    onDelete={onDelete}
                  />
                ))}
              </AnimatePresence>

              {commentsError ? (
                <div className="public-profile-comments-sheet__empty is-error">
                  <Glyph name="spark" size={24} />
                  <strong>{t("common.loadErrorTitle")}</strong>
                  <small>{t("common.loadErrorCopy")}</small>
                  <button type="button" onClick={() => void onRetryComments()}>
                    {t("common.retry")}
                  </button>
                </div>
              ) : null}

              {!comments.length && !commentsLoading && !commentsError ? (
                <div className="public-profile-comments-sheet__empty">
                  <img src="/ui-illustrations/mnemos-comments.svg" alt="" />
                  <strong>{t("people.profile.noComments")}</strong>
                  <small>{t("people.profile.firstComment")}</small>
                </div>
              ) : null}

              {commentsLoading && !commentsError ? (
                <span className="public-profile-comments-sheet__loading">…</span>
              ) : null}
            </div>

            <ProfileCommentComposer
              activeAccountNickname={activeAccountNickname}
              draft={composer.draft}
              replyingTo={composer.replyingTo}
              editing={composer.editing}
              inputRef={composer.inputRef}
              busy={composer.busy}
              onDraftChange={composer.setDraft}
              onReset={composer.reset}
              onSubmit={composer.submit}
            />
          </motion.section>
        </motion.div>
      ) : null}
    </AnimatePresence>
  )
}
