import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import type { ProfileCommentView, ReplyTarget, ReplyThreadMap } from "../model"
import { ProfileCommentRow } from "./ProfileCommentRow"
import { ProfileReplyRow } from "./ProfileReplyRow"
import { profileCommentTone } from "./profileCommentPresentation"

type ProfileCommentThreadProps = {
  comment: ProfileCommentView
  index: number
  thread: ReplyThreadMap[string] | undefined
  repliesExpanded: boolean
  busyIds: Set<string>
  onStartReply(target: ReplyTarget): void
  onStartEdit(comment: ProfileCommentView, rootCommentId?: string): void
  onToggleReplies(comment: ProfileCommentView): Promise<void>
  onLoadMoreReplies(comment: ProfileCommentView): Promise<void>
  onToggleLike(comment: ProfileCommentView, rootCommentId?: string): Promise<void>
  onDelete(comment: ProfileCommentView, rootCommentId?: string): Promise<void>
}

export function ProfileCommentThread({
  comment,
  index,
  thread,
  repliesExpanded,
  busyIds,
  onStartReply,
  onStartEdit,
  onToggleReplies,
  onLoadMoreReplies,
  onToggleLike,
  onDelete,
}: ProfileCommentThreadProps) {
  const { t } = useTranslation()
  const threadItems = thread?.items ?? []

  return (
    <motion.article
      layout="position"
      id={`profile-thread-${comment.id}`}
      className={`public-profile-thread is-${profileCommentTone(comment.author)}`}
      initial={{ opacity: 0, y: 5 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, scale: 0.985 }}
      transition={{ delay: Math.min(index * 0.025, 0.1) }}
    >
      <ProfileCommentRow
        comment={comment}
        busy={busyIds.has(comment.id)}
        repliesExpanded={repliesExpanded}
        onStartReply={onStartReply}
        onStartEdit={onStartEdit}
        onToggleReplies={() => void onToggleReplies(comment)}
        onToggleLike={() => void onToggleLike(comment)}
        onDelete={() => void onDelete(comment)}
      />

      {repliesExpanded ? (
        <motion.div
          className="public-profile-replies"
          initial={{ opacity: 0, y: -4 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.16 }}
        >
          {threadItems.map((reply) => (
            <ProfileReplyRow
              key={reply.id}
              reply={reply}
              rootCommentId={comment.id}
              busy={busyIds.has(reply.id)}
              onStartReply={onStartReply}
              onStartEdit={onStartEdit}
              onToggleLike={() => void onToggleLike(reply, comment.id)}
              onDelete={() => void onDelete(reply, comment.id)}
            />
          ))}

          {thread?.loading ? <span className="public-profile-replies__loading">…</span> : null}

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
}
