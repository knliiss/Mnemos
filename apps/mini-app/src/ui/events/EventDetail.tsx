import { motion } from "framer-motion"
import { useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { ApiError } from "@/shared/api/httpClient"
import { getEventFeedItem } from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { EventArt, OwnerCrown } from "../components/events/EventArt"
import { toFeedEvent } from "../data/dataMappers"
import { useMnemosData } from "../data/MnemosDataProvider"
import type { Account, FeedEvent, Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { formatOccurredAt } from "../timePresentation"
import {
  belongsToAccount,
  eventDetailDescription,
  eventKindCaption,
  eventKindLabel,
  eventSubject,
  eventTitle,
  eventTypeLabel,
  localeForLanguage,
  notificationRuleDescription,
  rarityLabel,
} from "./eventPresentation"
import { createSubscriptionForEvent, subscriptionMatchesEvent } from "./eventSubscriptions"

type Props = {
  eventId?: string
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

type EventLookupResult = {
  eventId: string
  event: FeedEvent | null
}

export function EventDetail({ eventId, activeAccount, navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const data = useMnemosData()
  const cachedEvent = data.feed.find((item) => item.id === eventId) ?? null
  const [lookupResult, setLookupResult] = useState<EventLookupResult | null>(null)
  const [lookupErrorId, setLookupErrorId] = useState<string | null>(null)
  const [lookupRevision, setLookupRevision] = useState(0)
  const [followBusyScope, setFollowBusyScope] = useState<string | null>(null)
  const followBusyRef = useRef<string | null>(null)
  const mountedRef = useRef(true)
  const actionScope = `${activeAccount.id}:${eventId ?? ""}`
  const followBusy = followBusyScope === actionScope

  const resolvedEvent =
    lookupResult && lookupResult.eventId === eventId ? lookupResult.event : null
  const event = cachedEvent ?? resolvedEvent
  const lookupLoading = Boolean(
    eventId &&
      !cachedEvent &&
      lookupResult?.eventId !== eventId &&
      lookupErrorId !== eventId,
  )
  const lookupError = lookupErrorId === eventId
  const locale = localeForLanguage(i18n.resolvedLanguage)

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

  useEffect(() => {
    if (!eventId || cachedEvent) {
      return
    }

    let cancelled = false
    setLookupErrorId((current) => (current === eventId ? null : current))

    void getEventFeedItem(eventId)
      .then((response) => {
        if (cancelled) {
          return
        }

        setLookupErrorId(null)
        setLookupResult({
          eventId,
          event: toFeedEvent(response),
        })
      })
      .catch((error) => {
        if (cancelled) {
          return
        }

        const notFound = error instanceof ApiError && error.status === 404

        if (!notFound) {
          setLookupErrorId(eventId)
          handleError(error)
        }

        setLookupResult({
          eventId,
          event: null,
        })
      })

    return () => {
      cancelled = true
    }
  }, [cachedEvent, eventId, lookupRevision])

  const matchingSubscription = useMemo(
    () =>
      event
        ? (data.subscriptions.find((subscription) =>
            subscriptionMatchesEvent(subscription, event),
          ) ?? null)
        : null,
    [data.subscriptions, event],
  )

  if (!event) {
    return (
      <div className="screen event-detail-screen">
        <PageHeader title={t("events.detail.title")} back={back} />

        <section className="events-feed-empty">
          <Glyph name="events" size={24} />
          <strong>
            {lookupLoading
              ? t("events.detail.loadingTitle")
              : lookupError
                ? t("common.loadErrorTitle")
                : t("events.detail.notFoundTitle")}
          </strong>
          <small>
            {lookupLoading
              ? t("events.detail.loadingCopy")
              : lookupError
                ? t("common.loadErrorCopy")
                : t("events.detail.notFoundCopy")}
          </small>
          {lookupError ? (
            <button type="button" onClick={() => setLookupRevision((value) => value + 1)}>
              {t("common.retry")}
            </button>
          ) : null}
        </section>
      </div>
    )
  }

  const ownEvent = belongsToAccount(event, activeAccount)

  const toggleFollow = async () => {
    if (followBusyRef.current === actionScope) {
      return
    }

    const requestedScope = actionScope

    followBusyRef.current = requestedScope
    setFollowBusyScope(requestedScope)

    try {
      if (matchingSubscription) {
        await data.removeSubscription(matchingSubscription.id)
      } else {
        await data.mutateSubscriptions(() => createSubscriptionForEvent(event))
      }
    } finally {
      if (followBusyRef.current === requestedScope) {
        followBusyRef.current = null
        if (mountedRef.current) {
          setFollowBusyScope(null)
        }
      }
    }
  }

  return (
    <div className={`screen event-detail-screen event-detail-screen--${event.kind.toLowerCase()}`}>
      <PageHeader
        title={eventTitle(t, event)}
        eyebrow={eventKindLabel(t, event.kind)}
        back={back}
      />

      <motion.section
        className="event-detail-hero"
        initial={{ opacity: 0, y: 6 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <span className="event-detail-hero__scan" aria-hidden="true">
          <i />
          <i />
          <i />
        </span>

        <span className="event-detail-hero__art">
          <EventArt event={event} size={66} />
        </span>

        <span className="event-detail-hero__copy">
          <small>{eventKindCaption(t, event)}</small>
          <strong>{eventSubject(t, event)}</strong>
          <em>{eventDetailDescription(t, event)}</em>

          {ownEvent ? (
            <span className="event-detail-owner">
              <OwnerCrown compact />
              {t("events.detail.ownAccount")}
            </span>
          ) : null}
        </span>

        <time>{formatOccurredAt(event.occurredAt, locale)}</time>

        <img
          className="event-detail-hero__mascot"
          src="/ui-illustrations/mnemos-mascot-event.svg"
          alt=""
          aria-hidden="true"
        />
      </motion.section>

      <section className={`event-detail-follow ${matchingSubscription ? "is-active" : ""}`}>
        <span className="event-detail-follow__mark" aria-hidden="true">
          <Glyph name="bell" size={18} />
          <i />
        </span>

        <span className="event-detail-follow__copy">
          <small>{t("events.detail.notifications")}</small>
          <strong>
            {matchingSubscription
              ? t("events.detail.followEnabled")
              : t("events.detail.followSimilar")}
          </strong>
          <em>{notificationRuleDescription(t, event)}</em>
        </span>

        <motion.button
          type="button"
          className={matchingSubscription ? "is-active" : ""}
          whileTap={{ scale: 0.96 }}
          disabled={followBusy}
          onClick={() => void toggleFollow()}
        >
          {followBusy
            ? "…"
            : matchingSubscription
              ? t("events.detail.unfollow")
              : t("events.detail.follow")}
        </motion.button>

        <button
          type="button"
          className="event-detail-follow__manage"
          onClick={() => navigate({ id: "subscriptions" })}
        >
          {t("events.detail.allRules")}
        </button>
      </section>

      <section className="event-detail-facts" aria-label={t("events.detail.dataAria")}>
        <article className="is-wide">
          <small>{t("events.detail.happened")}</small>
          <strong>
            {new Intl.DateTimeFormat(locale, {
              dateStyle: "medium",
              timeStyle: "short",
            }).format(new Date(event.occurredAt))}
          </strong>
          <i aria-hidden="true" />
        </article>

        <article>
          <small>{t("events.detail.category")}</small>
          <strong>{eventTypeLabel(t, event)}</strong>
        </article>

        <article>
          <small>{event.rarity ? t("events.detail.rarity") : t("events.detail.source")}</small>
          <strong>{event.rarity ? rarityLabel(t, event.rarity) : "Cristalix"}</strong>
        </article>
      </section>
    </div>
  )
}
