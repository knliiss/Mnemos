import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import type { ProfileCommentView } from "../model"

type PublicProfileCommentsPreviewProps = {
  totalCount: number
  latestComment: ProfileCommentView | null
  onOpen(): void
}

export function PublicProfileCommentsPreview({
  totalCount,
  latestComment,
  onOpen,
}: PublicProfileCommentsPreviewProps) {
  const { t } = useTranslation()

  return (
    <section className="public-profile-comments-preview">
      <header className="public-profile-section-head public-profile-comments-preview__head">
        <span>
          <small>{t("people.profile.social")}</small>
          <strong>{t("people.profile.comments")}</strong>
        </span>
        <b>{totalCount}</b>
      </header>

      <motion.button
        className="public-profile-comments-preview__open"
        type="button"
        whileTap={{ scale: 0.993 }}
        onClick={onOpen}
      >
        <span className="public-profile-comments-preview__art" aria-hidden="true">
          <img
            className="public-profile-comments-preview__illustration"
            src="/ui-illustrations/mnemos-comments.svg"
            alt=""
          />
          <span className="public-profile-comments-preview__spark is-lime" />
          <span className="public-profile-comments-preview__spark is-plum" />
        </span>

        <span className="public-profile-comments-preview__copy">
          <small>{t("people.profile.latestConversation")}</small>
          <strong>
            {latestComment ? (
              <>
                <b>@{latestComment.author}</b> {latestComment.content}
              </>
            ) : (
              t("people.profile.noComments")
            )}
          </strong>
          <em>
            {totalCount > 0
              ? t("people.profile.openAllComments", { count: totalCount })
              : t("people.profile.openComments")}
          </em>
        </span>

        <span className="public-profile-comments-preview__count">{totalCount}</span>
        <span className="public-profile-comments-preview__arrow" aria-hidden="true">
          ›
        </span>
      </motion.button>
    </section>
  )
}
