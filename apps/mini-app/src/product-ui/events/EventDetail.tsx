import { motion } from "framer-motion"
import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { ApiError } from "@/shared/api/httpClient"
import { getEventFeedItem } from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { EventArt, OwnerCrown } from "../components/events/EventArt"
import { useProductData } from "../data/ProductDataProvider"
import { toProductFeedEvent } from "../data/productMappers"
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
  const product = useProductData()
  const cachedEvent = product.feed.find((item) => item.id === eventId) ?? null
  const [lookupResult, setLookupResult] = useState<EventLookupResult | null>(null)
  const [followBusy, setFollowBusy] = useState(false)

  const resolvedEvent =
    lookupResult && lookupResult.eventId === eventId ? lookupResult.event : null
  const event = cachedEvent ?? resolvedEvent
  const lookupLoading = Boolean(eventId && !cachedEvent && lookupResult?.eventId !== eventId)
  const locale = localeForLanguage(i18n.resolvedLanguage)

  useEffect(() => {
    if (!eventId || cachedEvent) {
      return
    }

    let cancelled = false

    void getEventFeedItem(eventId)
      .then((response) => {
        if (cancelled) {
          return
        }

        setLookupResult({
          eventId,
          event: toProductFeedEvent(response),
        })
      })
      .catch((error) => {
        if (cancelled) {
          return
        }

        if (!(error instanceof ApiError && error.status === 404)) {
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
  }, [cachedEvent, eventId])

  const matchingSubscription = useMemo(
    () =>
      event
        ? (product.subscriptions.find((subscription) =>
            subscriptionMatchesEvent(subscription, event),
          ) ?? null)
        : null,
    [event, product.subscriptions],
  )

  if (!event) {
    return (
      <div className="screen event-r4-screen">
        <PageHeader title={t("events.detail.title")} back={back} />

        <section className="events-r2-empty">
          <Glyph name="events" size={24} />
          <strong>
            {lookupLoading ? t("events.detail.loadingTitle") : t("events.detail.notFoundTitle")}
          </strong>
          <small>
            {lookupLoading ? t("events.detail.loadingCopy") : t("events.detail.notFoundCopy")}
          </small>
        </section>
      </div>
    )
  }

  const ownEvent = belongsToAccount(event, activeAccount)

  const toggleFollow = async () => {
    if (followBusy) {
      return
    }

    setFollowBusy(true)

    try {
      if (matchingSubscription) {
        await product.removeSubscription(matchingSubscription.id)
      } else {
        await createSubscriptionForEvent(event)
        await product.refreshSubscriptions()
      }
    } catch (error) {
      if (!matchingSubscription) {
        handleError(error)
      }
    } finally {
      setFollowBusy(false)
    }
  }

  return (
    <div className={`screen event-r4-screen event-r4-screen--${event.kind.toLowerCase()}`}>
      <PageHeader
        title={eventTitle(t, event)}
        eyebrow={eventKindLabel(t, event.kind)}
        back={back}
      />

      <motion.section
        className="event-r4-hero"
        initial={{ opacity: 0, y: 6 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <span className="event-r4-hero__scan" aria-hidden="true">
          <i />
          <i />
          <i />
        </span>

        <span className="event-r4-hero__art">
          <EventArt event={event} size={66} />
        </span>

        <span className="event-r4-hero__copy">
          <small>{eventKindCaption(t, event)}</small>
          <strong>{eventSubject(t, event)}</strong>
          <em>{eventDetailDescription(t, event)}</em>

          {ownEvent ? (
            <span className="event-r4-owner">
              <OwnerCrown compact />
              {t("events.detail.ownAccount")}
            </span>
          ) : null}
        </span>

        <time>{formatOccurredAt(event.occurredAt, locale)}</time>

        <img
          className="event-r4-hero__mascot"
          src="/ui-illustrations/mnemos-mascot-event.svg"
          alt=""
          aria-hidden="true"
        />
      </motion.section>

      <section className={`event-r4-follow ${matchingSubscription ? "is-active" : ""}`}>
        <span className="event-r4-follow__mark" aria-hidden="true">
          <Glyph name="bell" size={18} />
          <i />
        </span>

        <span className="event-r4-follow__copy">
          <small>{t("events.detail.notifications")}</small>
          <strong>
            {matchingSubscription
              ? t("events.detail.followEnabled")
              : t("events.detail.followSimilar")}
          </strong>
          <em>{notificationRuleDescription(t, event)}</em>
        </span>

        <motion.button
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
          className="event-r4-follow__manage"
          onClick={() => navigate({ id: "subscriptions" })}
        >
          {t("events.detail.allRules")}
        </button>
      </section>

      <section className="event-r4-facts" aria-label={t("events.detail.dataAria")}>
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
