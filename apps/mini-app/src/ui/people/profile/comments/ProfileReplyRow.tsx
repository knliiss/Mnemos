import { useTranslation } from "react-i18next"
import type { ProfileCommentView, ReplyTarget } from "../model"
import { Glyph } from "../../../shared/MnemosIcon"
import { ProfileCommentAvatar } from "./ProfileCommentAvatar"
import { profileCommentTone } from "./profileCommentPresentation"

type ProfileReplyRowProps = {
  reply: ProfileCommentView
  rootCommentId: string
  busy: boolean
  onStartReply(target: ReplyTarget): void
  onStartEdit(comment: ProfileCommentView, rootCommentId: string): void
  onToggleLike(): void
  onDelete(): void
}

export function ProfileReplyRow({
  reply,
  rootCommentId,
  busy,
  onStartReply,
  onStartEdit,
  onToggleLike,
  onDelete,
}: ProfileReplyRowProps) {
  const { t } = useTranslation()

  return (
    <div className={`public-profile-reply is-${profileCommentTone(reply.author)}`}>
      <ProfileCommentAvatar author={reply.author} compact />

      <div className="public-profile-reply__body">
        <header>
          <strong>{reply.author}</strong>
          <time>{reply.createdAt}</time>
        </header>

        <p>
          {reply.replyToAuthor ? (
            <span className="public-profile-reply__mention">@{reply.replyToAuthor}</span>
          ) : null}
          {reply.deleted ? t("people.profile.deletedComment") : reply.content}
        </p>

        {!reply.deleted ? (
          <footer>
            <button
              type="button"
              onClick={() =>
                onStartReply({
                  rootCommentId,
                  targetCommentId: reply.id,
                  author: reply.author,
                })
              }
            >
              {t("people.profile.reply")}
            </button>

            {reply.canEdit ? (
              <button type="button" onClick={() => onStartEdit(reply, rootCommentId)}>
                {t("people.profile.edit")}
              </button>
            ) : null}
          </footer>
        ) : null}
      </div>

      <div className="public-profile-reply__actions">
        {!reply.deleted ? (
          <button
            type="button"
            className={`public-profile-reply__like ${reply.liked ? "is-active" : ""}`}
            disabled={busy}
            onClick={onToggleLike}
            aria-label={reply.liked ? t("people.profile.unlike") : t("people.profile.like")}
          >
            <Glyph name="like" size={12} />
            <span>{reply.likes || ""}</span>
          </button>
        ) : null}

        {reply.canDelete && !reply.deleted ? (
          <button
            type="button"
            className="public-profile-reply__delete"
            disabled={busy}
            onClick={onDelete}
            aria-label={t("people.profile.deleteOwnReply")}
          >
            <img src="/ui-illustrations/mnemos-trash.svg" alt="" />
          </button>
        ) : null}
      </div>
    </div>
  )
}
