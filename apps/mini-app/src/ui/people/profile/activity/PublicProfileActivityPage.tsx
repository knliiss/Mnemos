import { motion } from "framer-motion"
import { useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import type { EventFeedQuery } from "@/shared/api/eventApi"
import { getPublicProfile } from "@/shared/api/profileSocialApi"
import { handleError } from "@/shared/errors/errorHandler"
import { EventArt } from "../../../components/events/EventArt"
import { eventVisualTone } from "../../../eventVisualTone"
import { useEventFeedData } from "../../../events/feed/useEventFeedData"
import type { Route } from "../../../model"
import { PageHeader } from "../../../shared/Primitives"
import { FeatureError } from "../../../system/FeatureError"
import { FeatureLoading } from "../../../system/FeatureLoading"
import { formatOccurredAt } from "../../../timePresentation"
import { localeForLanguage, rarityLabel } from "../profilePresentation"
import "./profile-activity.css"

type PublicProfileActivityPageProps = {
  accountId?: string
  nickname?: string
  navigate: (route: Route) => void
  back: () => void
}

type ResolvedProfileIdentity = {
  accountId: string
  nickname: string
}

export function PublicProfileActivityPage({
  accountId,
  nickname,
  navigate,
  back,
}: PublicProfileActivityPageProps) {
  const { t, i18n } = useTranslation()
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const directNickname = nickname?.trim() || null
  const directAccountId = accountId?.trim() || null
  const [resolvedProfile, setResolvedProfile] = useState<ResolvedProfileIdentity | null>(null)
  const [failedAccountId, setFailedAccountId] = useState<string | null>(null)
  const [resolutionRevision, setResolutionRevision] = useState(0)
  const loadMoreAnchor = useRef<HTMLDivElement | null>(null)
  const { feed, eventsHasMore, loadingEvents, eventsError, refreshEvents, loadMoreEvents } =
    useEventFeedData()

  const resolvedNickname =
    directNickname ??
    (resolvedProfile?.accountId === directAccountId ? resolvedProfile.nickname : null)
  const resolutionFailed = Boolean(
    !directNickname && directAccountId && failedAccountId === directAccountId,
  )
  const resolvingProfile = Boolean(
    !directNickname && directAccountId && !resolvedNickname && !resolutionFailed,
  )
  const feedQuery = useMemo<EventFeedQuery>(
    () => ({
      types: ["ITEM_DROP"],
      droppedFor: resolvedNickname ?? undefined,
      sort: "NEWEST",
      limit: 24,
    }),
    [resolvedNickname],
  )

  useEffect(() => {
    if (directNickname || !directAccountId) {
      return
    }

    let cancelled = false

    void getPublicProfile(directAccountId)
      .then((profile) => {
        if (cancelled) {
          return
        }

        setResolvedProfile({
          accountId: directAccountId,
          nickname: profile.cristalixNickname,
        })
        setFailedAccountId(null)
      })
      .catch((error) => {
        if (cancelled) {
          return
        }

        handleError(error)
        setFailedAccountId(directAccountId)
      })

    return () => {
      cancelled = true
    }
  }, [directAccountId, directNickname, resolutionRevision])

  useEffect(() => {
    if (!resolvedNickname) {
      return
    }

    void refreshEvents(feedQuery)
  }, [feedQuery, refreshEvents, resolvedNickname])

  useEffect(() => {
    const anchor = loadMoreAnchor.current

    if (!anchor || !resolvedNickname || !eventsHasMore || loadingEvents) {
      return
    }

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          void loadMoreEvents(feedQuery)
        }
      },
      { rootMargin: "180px 0px" },
    )

    observer.observe(anchor)

    return () => observer.disconnect()
  }, [eventsHasMore, feedQuery, loadMoreEvents, loadingEvents, resolvedNickname])

  if (!directNickname && !directAccountId) {
    return <FeatureError back={back} onRetry={() => undefined} />
  }

  if (resolvingProfile) {
    return (
      <div className="screen profile-activity-screen">
        <PageHeader title={t("people.profile.activityTitle")} back={back} />
        <FeatureLoading label={t("people.profile.loadingActivity")} />
      </div>
    )
  }

  if (resolutionFailed || !resolvedNickname) {
    return <FeatureError back={back} onRetry={() => setResolutionRevision((value) => value + 1)} />
  }

  if (eventsError && !feed.length) {
    return <FeatureError back={back} onRetry={() => void refreshEvents(feedQuery)} />
  }

  return (
    <div className="screen profile-activity-screen">
      <PageHeader title={t("people.profile.activityTitle")} back={back} />

      <section className="profile-activity-hero">
        <span className="profile-activity-hero__copy">
          <small>{t("people.profile.activityEyebrow")}</small>
          <strong>{t("people.profile.activityHistory")}</strong>
          <span>{resolvedNickname}</span>
          <p>{t("people.profile.activityCopy")}</p>
        </span>

        <img src="/ui-illustrations/account-activity.svg" alt="" aria-hidden="true" />
      </section>

      <section className="profile-activity-list" aria-busy={loadingEvents}>
        {feed.map((drop, index) => (
          <motion.button
            type="button"
            key={drop.id}
            className={`profile-activity-row event-surface--${eventVisualTone(drop)}`}
            initial={{ opacity: 0, y: 5 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: Math.min(index, 8) * 0.025 }}
            whileTap={{ scale: 0.993 }}
            onClick={() => navigate({ id: "event-detail", params: { eventId: drop.id } })}
          >
            <span className="profile-activity-row__art">
              <EventArt event={drop} size={46} />
            </span>

            <span className="profile-activity-row__copy">
              <small>{rarityLabel(drop.rarity, t)}</small>
              <strong>{drop.title}</strong>
              <em>{drop.meta}</em>
            </span>

            <time>{formatOccurredAt(drop.occurredAt, locale)}</time>
            <span className="profile-activity-row__open" aria-hidden="true">
              ›
            </span>
          </motion.button>
        ))}

        {!feed.length && !loadingEvents ? (
          <div className="profile-activity-empty">
            <img src="/ui-illustrations/account-activity.svg" alt="" aria-hidden="true" />
            <strong>{t("people.profile.activityEmptyTitle")}</strong>
            <small>{t("people.profile.activityEmptyCopy")}</small>
          </div>
        ) : null}

        <div ref={loadMoreAnchor} className="profile-activity-load-more" aria-live="polite">
          {loadingEvents ? (
            <span>{t("people.profile.loadingMore")}</span>
          ) : feed.length && !eventsHasMore ? (
            <span>{t("people.profile.activityEnd")}</span>
          ) : null}
        </div>
      </section>
    </div>
  )
}
