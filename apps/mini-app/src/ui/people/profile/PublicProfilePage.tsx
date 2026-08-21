import { useState } from "react"
import { useTranslation } from "react-i18next"
import "../../events/event-surfaces.css"
import type { Account, Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureError } from "../../system/FeatureError"
import { FeatureLoading } from "../../system/FeatureLoading"
import { ProfileCommentsSheet } from "./comments/ProfileCommentsSheet"
import { useProfileComments } from "./comments/useProfileComments"
import { usePublicProfileData } from "./data/usePublicProfileData"
import "./profile-state.css"
import { localeForLanguage } from "./profilePresentation"
import "./public-profile.css"
import { PublicProfileCommentsPreview } from "./sections/PublicProfileCommentsPreview"
import { PublicProfileDropsSection } from "./sections/PublicProfileDropsSection"
import { PublicProfileSummary } from "./sections/PublicProfileSummary"

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
    error,
    reactionBusy,
    refresh,
    refreshProfileSummary,
    toggleReaction,
  } = usePublicProfileData(accountId, activeAccount.id)
  const comments = useProfileComments({
    activeAccountId: activeAccount.id,
    activeAccountNickname: activeAccount.nickname,
    profile,
    locale,
    refreshProfileSummary,
  })

  if (loading && !profile) {
    return (
      <div className="screen public-profile-screen">
        <PageHeader title={t("people.profile.title")} back={back} />
        <FeatureLoading label={t("people.profile.loading")} />
      </div>
    )
  }

  if (error) {
    return <FeatureError back={back} onRetry={() => void refresh()} />
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

  return (
    <div className="screen public-profile-screen">
      <PageHeader title={t("people.profile.title")} back={back} />

      <PublicProfileSummary
        profile={profile}
        reaction={reaction}
        dropsCount={drops.length}
        reactionBusy={reactionBusy}
        onToggleReaction={toggleReaction}
        onOpenComments={() => setCommentsOpen(true)}
      />

      <PublicProfileDropsSection drops={drops} locale={locale} navigate={navigate} />

      <PublicProfileCommentsPreview
        totalCount={profile.comments}
        latestComment={comments.latestComment}
        onOpen={() => setCommentsOpen(true)}
      />

      <ProfileCommentsSheet
        open={commentsOpen}
        profileNickname={profile.cristalixNickname}
        activeAccountNickname={activeAccount.nickname}
        totalCount={profile.comments}
        comments={comments.comments}
        commentsHasMore={comments.commentsHasMore}
        commentsLoading={comments.commentsLoading}
        commentsError={comments.commentsError}
        replies={comments.replies}
        expandedReplies={comments.expandedReplies}
        busyIds={comments.busyIds}
        submitting={comments.submitting}
        onClose={() => setCommentsOpen(false)}
        onLoadMoreComments={comments.loadMoreComments}
        onRetryComments={comments.retryComments}
        onToggleReplies={comments.toggleReplies}
        onLoadMoreReplies={comments.loadMoreReplies}
        onToggleLike={comments.toggleCommentLike}
        onEdit={comments.editProfileComment}
        onDelete={comments.removeComment}
        onSubmit={comments.submitComment}
      />
    </div>
  )
}
