import { useTranslation } from "react-i18next"
import type { ProfileCommentView, ReplyTarget } from "../model"
import { Glyph } from "../../../shared/MnemosIcon"
import { ProfileCommentAvatar } from "./ProfileCommentAvatar"

type ProfileCommentRowProps = {
  comment: ProfileCommentView
  busy: boolean
  repliesExpanded: boolean
  onStartReply(target: ReplyTarget): void
  onStartEdit(comment: ProfileCommentView): void
  onToggleReplies(): void
  onToggleLike(): void
  onDelete(): void
}

export function ProfileCommentRow({
  comment,
  busy,
  repliesExpanded,
  onStartReply,
  onStartEdit,
  onToggleReplies,
  onToggleLike,
  onDelete,
}: ProfileCommentRowProps) {
  const { t } = useTranslation()

  return (
    <div className="public-profile-comment">
      <ProfileCommentAvatar author={comment.author} />

      <div className="public-profile-comment__body">
        <header>
          <strong>{comment.author}</strong>
          <time>{comment.createdAt}</time>
        </header>

        <p>{comment.deleted ? t("people.profile.deletedComment") : comment.content}</p>

        <footer>
          {!comment.deleted ? (
            <button
              type="button"
              onClick={() =>
                onStartReply({
                  rootCommentId: comment.id,
                  targetCommentId: null,
                  author: comment.author,
                })
              }
            >
              {t("people.profile.reply")}
            </button>
          ) : null}

          {comment.canEdit && !comment.deleted ? (
            <button type="button" onClick={() => onStartEdit(comment)}>
              {t("people.profile.edit")}
            </button>
          ) : null}

          {comment.replies > 0 ? (
            <button
              type="button"
              className="public-profile-thread__toggle"
              onClick={onToggleReplies}
            >
              <i />
              {repliesExpanded
                ? t("people.profile.hideReplies")
                : t("people.profile.showReplies", { count: comment.replies })}
            </button>
          ) : null}
        </footer>
      </div>

      <div className="public-profile-comment__actions">
        {!comment.deleted ? (
          <button
            type="button"
            className={`public-profile-comment__like ${comment.liked ? "is-active" : ""}`}
            disabled={busy}
            onClick={onToggleLike}
            aria-label={comment.liked ? t("people.profile.unlike") : t("people.profile.like")}
          >
            <Glyph name="like" size={15} />
            <span>{comment.likes || ""}</span>
          </button>
        ) : null}

        {comment.canDelete && !comment.deleted ? (
          <button
            type="button"
            className="public-profile-comment__delete"
            disabled={busy}
            onClick={onDelete}
            aria-label={t("people.profile.deleteOwnComment")}
          >
            <img src="/ui-illustrations/mnemos-trash.svg" alt="" />
          </button>
        ) : null}
      </div>
    </div>
  )
}
