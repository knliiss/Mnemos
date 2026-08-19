import type { TFunction } from "i18next"
import { motion } from "framer-motion"
import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  createBoosterSubscription,
  createGlobalSubscription,
  createItemDropSubscription,
  createRaidSubscription,
  getEventFeedItem,
} from "@/shared/api/productApi"
import { ApiError } from "@/shared/api/httpClient"
import { handleError } from "@/shared/errors/errorHandler"
import { EventArt, OwnerCrown } from "../components/EventArt"
import { useProductData } from "../data/ProductDataProvider"
import { toProductFeedEvent } from "../data/productMappers"
import type {
  Account,
  BoosterType,
  FeedEvent,
  GlobalEventType,
  Route,
  Subscription,
} from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { formatOccurredAt } from "../timePresentation"

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

export function EventDetailProduction({ eventId, activeAccount, navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const product = useProductData()
  const cachedEvent = product.feed.find((item) => item.id === eventId) ?? null
  const [lookupResult, setLookupResult] = useState<EventLookupResult | null>(null)
  const [followBusy, setFollowBusy] = useState(false)
  const resolvedEvent = lookupResult?.eventId === eventId ? lookupResult.event : null
  const event = cachedEvent ?? resolvedEvent
  const lookupLoading = Boolean(eventId && !cachedEvent && lookupResult?.eventId !== eventId)
  const locale = localeForLanguage(i18n.resolvedLanguage)

  useEffect(() => {
    if (!eventId || cachedEvent) return

    let cancelled = false

    void getEventFeedItem(eventId)
      .then((response) => {
        if (!cancelled) {
          setLookupResult({
            eventId,
            event: toProductFeedEvent(response),
          })
        }
      })
      .catch((error) => {
        if (cancelled) return

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
    () => event ? product.subscriptions.find((subscription) => matchesEvent(subscription, event)) ?? null : null,
    [event, product.subscriptions],
  )

  if (!event) {
    return (
      <div className="screen event-r4-screen">
        <PageHeader title={t("events.detail.title")} back={back} />
        <section className="events-r2-empty">
          <Glyph name="events" size={24} />
          <strong>{lookupLoading ? t("events.detail.loadingTitle") : t("events.detail.notFoundTitle")}</strong>
          <small>{lookupLoading ? t("events.detail.loadingCopy") : t("events.detail.notFoundCopy")}</small>
        </section>
      </div>
    )
  }

  const ownEvent = belongsToAccount(event, activeAccount)

  const toggleFollow = async () => {
    if (followBusy) return

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
      <PageHeader title={eventTitle(t, event)} eyebrow={eventKindLabel(t, event)} back={back} />

      <motion.section
        className="event-r4-hero"
        initial={{ opacity: 0, y: 6 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <span className="event-r4-hero__scan" aria-hidden="true"><i /><i /><i /></span>
        <span className="event-r4-hero__art"><EventArt event={event} size={66} /></span>

        <span className="event-r4-hero__copy">
          <small>{eventKindCaption(t, event)}</small>
          <strong>{eventSubject(t, event)}</strong>
          <em>{eventDescription(t, event)}</em>
          {ownEvent ? <span className="event-r4-owner"><OwnerCrown compact />{t("events.detail.ownAccount")}</span> : null}
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
          <strong>{matchingSubscription ? t("events.detail.followEnabled") : t("events.detail.followSimilar")}</strong>
          <em>{notificationRuleDescription(t, event)}</em>
        </span>

        <motion.button
          className={matchingSubscription ? "is-active" : ""}
          whileTap={{ scale: 0.96 }}
          disabled={followBusy}
          onClick={() => void toggleFollow()}
        >
          {followBusy ? "…" : matchingSubscription ? t("events.detail.unfollow") : t("events.detail.follow")}
        </motion.button>

        <button className="event-r4-follow__manage" onClick={() => navigate({ id: "subscriptions" })}>
          {t("events.detail.allRules")}
        </button>
      </section>

      <section className="event-r4-facts" aria-label={t("events.detail.dataAria")}>
        <article className="is-wide">
          <small>{t("events.detail.happened")}</small>
          <strong>{new Intl.DateTimeFormat(locale, { dateStyle: "medium", timeStyle: "short" }).format(new Date(event.occurredAt))}</strong>
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

async function createSubscriptionForEvent(event: FeedEvent): Promise<void> {
  if (event.kind === "ITEM_DROP") {
    await createItemDropSubscription(undefined, event.title)
    return
  }

  if (event.kind === "BOOSTER") {
    await createBoosterSubscription(event.title as BoosterType)
    return
  }

  if (event.kind === "GLOBAL") {
    await createGlobalSubscription(event.title as GlobalEventType)
    return
  }

  await createRaidSubscription(parseLocations(event.detail))
}

function matchesEvent(subscription: Subscription, event: FeedEvent) {
  if (event.kind === "ITEM_DROP" && subscription.type === "ITEM_DROP") {
    const itemMatches = !subscription.itemName || subscription.itemName.toLowerCase() === event.title.toLowerCase()
    const playerMatches = !subscription.droppedFor || subscription.droppedFor.toLowerCase() === event.detail.toLowerCase()
    return itemMatches && playerMatches
  }

  if (event.kind === "BOOSTER" && subscription.type === "BOOSTER") {
    return subscription.boosterType === event.title
  }

  if (event.kind === "GLOBAL" && subscription.type === "GLOBAL") {
    return subscription.eventType === event.title
  }

  if (event.kind === "RAID" && subscription.type === "RAID") {
    if (!subscription.locations.length) return true

    const locations = parseLocations(event.detail)
    return subscription.locations.some((location) => locations.includes(location))
  }

  return false
}

function eventTitle(t: TFunction, event: FeedEvent) {
  if (event.kind === "GLOBAL") {
    return t(`events.globals.${event.title}`, { defaultValue: event.title })
  }

  if (event.kind === "BOOSTER") {
    return t(`events.boosters.${event.title}`, { defaultValue: event.title })
  }

  if (event.kind === "RAID") return t("events.kinds.RAID")
  return event.title
}

function eventKindLabel(t: TFunction, event: FeedEvent) {
  return t(`events.kinds.${event.kind}`)
}

function eventKindCaption(t: TFunction, event: FeedEvent) {
  return t(`events.detail.captions.${event.kind}`)
}

function eventSubject(t: TFunction, event: FeedEvent) {
  if (event.kind === "GLOBAL") return "Cristalix"
  if (event.kind === "RAID") return t("events.feed.locations", { locations: event.detail })
  return event.detail
}

function eventDescription(t: TFunction, event: FeedEvent) {
  if (event.kind === "ITEM_DROP") {
    return `${eventTitle(t, event)} · ${rarityLabel(t, event.rarity) ?? eventTypeLabel(t, event)}`
  }

  if (event.kind === "BOOSTER") return eventTitle(t, event)
  if (event.kind === "GLOBAL") return eventTitle(t, event)
  return t("events.detail.raidActivity")
}

function eventTypeLabel(t: TFunction, event: FeedEvent) {
  if (event.kind === "ITEM_DROP" && event.meta) {
    return t(`events.itemTypes.${event.meta}`, { defaultValue: event.meta })
  }

  if (event.kind === "BOOSTER") return eventTitle(t, event)
  if (event.kind === "GLOBAL") return t("events.kinds.GLOBAL")
  if (event.kind === "RAID") return t("events.kinds.RAID")
  return eventKindLabel(t, event)
}

function rarityLabel(t: TFunction, value?: string) {
  if (!value) return undefined
  return t(`events.rarities.${value}`, { defaultValue: value })
}

function belongsToAccount(event: FeedEvent, account: Account) {
  return (event.kind === "ITEM_DROP" || event.kind === "BOOSTER")
    && event.detail.toLowerCase() === account.nickname.toLowerCase()
}

function notificationRuleDescription(t: TFunction, event: FeedEvent) {
  if (event.kind === "ITEM_DROP") return t("events.detail.itemRule", { item: event.title })
  if (event.kind === "BOOSTER") return t("events.detail.boosterRule", { booster: eventTitle(t, event) })
  if (event.kind === "GLOBAL") return t("events.detail.globalRule", { event: eventTitle(t, event) })
  return t("events.detail.raidRule", { locations: event.detail })
}

function parseLocations(value: string) {
  return value
    .split(/[·,\s]+/)
    .map((entry) => Number(entry.trim()))
    .filter((entry) => Number.isInteger(entry) && entry > 0)
}

function localeForLanguage(language?: string) {
  if (language === "en") return "en-US"
  if (language === "ru") return "ru-RU"
  return "uk-UA"
}
