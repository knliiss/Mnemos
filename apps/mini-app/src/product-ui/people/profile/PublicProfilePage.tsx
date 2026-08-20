import { motion } from "framer-motion"
import { useState } from "react"
import { useTranslation } from "react-i18next"
import { EventArt } from "../../components/events/EventArt"
import { CristalixHead } from "../../components/profile/CristalixHead"
import { eventVisualTone } from "../../eventVisualTone"
import type { Account, Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { formatOccurredAt } from "../../timePresentation"
import { ProfileCommentsSheet } from "./comments/ProfileCommentsSheet"
import { useProfileComments } from "./comments/useProfileComments"
import { usePublicProfileData } from "./data/usePublicProfileData"
import {
  localeForLanguage,
  nicknameStyle,
  profileSkinUrl,
  rarityLabel,
} from "./profilePresentation"

type Props = {
  activeAccount: Account
  accountId: string
  navigate: (route: Route) => void
  back: () => void
}

export function PublicProfilePage({ activeAccount, accountId, navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const [commentsOpen, setCommentsOpen] = useState(false)
  const {
    profile,
    reaction,
    drops,
    loading,
    reactionBusy,
    refreshProfileSummary,
    toggleReaction,
  } = usePublicProfileData(accountId)
  const comments = useProfileComments({
    activeAccount,
    profile,
    locale,
    refreshProfileSummary,
  })

  if (loading && !profile) {
    return (
      <div className="screen public-profile-screen">
        <PageHeader title={t("people.profile.title")} back={back} />
        <div
          className="public-profile-state public-profile-state--loading"
          aria-label={t("people.profile.loading")}
        >
          <i />
          <i />
          <i />
        </div>
      </div>
    )
  }

  if (!profile) {
    return (
      <div className="screen public-profile-screen">
        <PageHeader title={t("people.profile.title")} back={back} />
        <section className="public-profile-state">
          <Glyph name="profile-mark" size={24} />
          <strong>{t("people.profile.notFoundTitle")}</strong>
          <small>{t("people.profile.notFoundCopy")}</small>
        </section>
      </div>
    )
  }

  const reputation = profile.likes - profile.dislikes

  return (
    <div className="screen public-profile-screen">
      <PageHeader title={t("people.profile.title")} back={back} />

      <motion.section
        className="public-profile-card"
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="public-profile-card__main">
          <div className="public-profile-head">
            <CristalixHead skinUrl={profileSkinUrl(profile.cristalixUuid)} size={78} />
            <span className="public-profile-presence">
              <i />
              CRISTALIX
            </span>
          </div>

          <div className="public-profile-identity">
            <small>{t("people.profile.accountCard")}</small>
            <strong style={nicknameStyle(profile)}>{profile.cristalixNickname}</strong>
            <span>{profile.cristalixUuid.slice(0, 8)}</span>
          </div>

          <div className="public-profile-reactions" aria-label={t("people.profile.reactionAria")}>
            <motion.button
              type="button"
              className={reaction === "LIKE" ? "is-active" : ""}
              whileTap={{ scale: 0.95 }}
              disabled={reactionBusy}
              onClick={() => void toggleReaction("LIKE")}
            >
              <Glyph name="like" size={15} />
              <b>{profile.likes}</b>
            </motion.button>

            <motion.button
              type="button"
              className={reaction === "DISLIKE" ? "is-active is-negative" : ""}
              whileTap={{ scale: 0.95 }}
              disabled={reactionBusy}
              onClick={() => void toggleReaction("DISLIKE")}
            >
              <Glyph name="dislike" size={15} />
              <b>{profile.dislikes}</b>
            </motion.button>
          </div>
        </div>

        <div className="public-profile-metrics">
          <article>
            <span className="public-profile-metric-icon">
              <Glyph name="events" size={14} />
            </span>
            <span>
              <small>{t("people.profile.drops")}</small>
              <strong>{drops.length}</strong>
            </span>
          </article>

          <button type="button" onClick={() => setCommentsOpen(true)}>
            <span className="public-profile-metric-icon">
              <Glyph name="chat" size={14} />
            </span>
            <span>
              <small>{t("people.profile.comments")}</small>
              <strong>{profile.comments}</strong>
            </span>
          </button>

          <article>
            <span className="public-profile-metric-icon is-reputation">
              <Glyph name="like" size={14} />
            </span>
            <span>
              <small>{t("people.profile.reputation")}</small>
              <strong>{reputation}</strong>
            </span>
          </article>
        </div>
      </motion.section>

      <section className="public-profile-section">
        <header className="public-profile-section-head">
          <span>
            <small>{t("people.profile.activity")}</small>
            <strong>{t("people.profile.latestDrops")}</strong>
          </span>
          <b>{drops.length}</b>
        </header>

        <div className="public-profile-drops">
          {drops.map((drop, index) => (
            <motion.button
              key={drop.id}
              type="button"
              className={`public-profile-drop event-surface--${eventVisualTone(drop)}`}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.045 }}
              whileTap={{ scale: 0.992 }}
              onClick={() =>
                navigate({
                  id: "event-detail",
                  params: { eventId: drop.id },
                })
              }
            >
              <span className="public-profile-drop__art">
                <EventArt event={drop} size={46} />
              </span>

              <span className="public-profile-drop__copy">
                <small>{rarityLabel(drop.rarity, t)}</small>
                <strong>{drop.title}</strong>
                <em>{formatOccurredAt(drop.occurredAt, locale)}</em>
              </span>

              <span className="public-profile-drop__order">0{index + 1}</span>
              <span className="public-profile-drop__open" aria-hidden="true">
                ›
              </span>
            </motion.button>
          ))}

          {!drops.length ? (
            <div className="public-profile-state public-profile-state--compact">
              <Glyph name="item-drop" size={22} />
              <strong>{t("people.profile.noDrops")}</strong>
            </div>
          ) : null}
        </div>
      </section>

      <section className="public-profile-comments-preview">
        <header className="public-profile-section-head public-profile-comments-preview__head">
          <span>
            <small>{t("people.profile.social")}</small>
            <strong>{t("people.profile.comments")}</strong>
          </span>
          <b>{profile.comments}</b>
        </header>

        <motion.button
          className="public-profile-comments-preview__open"
          type="button"
          whileTap={{ scale: 0.993 }}
          onClick={() => setCommentsOpen(true)}
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
              {comments.latestComment ? (
                <>
                  <b>@{comments.latestComment.author}</b> {comments.latestComment.content}
                </>
              ) : (
                t("people.profile.noComments")
              )}
            </strong>
            <em>
              {profile.comments > 0
                ? t("people.profile.openAllComments", {
                    count: profile.comments,
                  })
                : t("people.profile.openComments")}
            </em>
          </span>

          <span className="public-profile-comments-preview__count">{profile.comments}</span>
          <span className="public-profile-comments-preview__arrow" aria-hidden="true">
            ›
          </span>
        </motion.button>
      </section>

      <ProfileCommentsSheet
        open={commentsOpen}
        profileNickname={profile.cristalixNickname}
        activeAccountNickname={activeAccount.nickname}
        totalCount={profile.comments}
        comments={comments.comments}
        commentsHasMore={comments.commentsHasMore}
        commentsLoading={comments.commentsLoading}
        replies={comments.replies}
        expandedReplies={comments.expandedReplies}
        busyIds={comments.busyIds}
        submitting={comments.submitting}
        onClose={() => setCommentsOpen(false)}
        onLoadMoreComments={comments.loadMoreComments}
        onToggleReplies={comments.toggleReplies}
        onLoadMoreReplies={comments.loadMoreReplies}
        onToggleLike={comments.toggleCommentLike}
        onDelete={comments.removeComment}
        onSubmit={comments.submitComment}
      />
    </div>
  )
}
