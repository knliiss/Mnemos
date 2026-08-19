import { motion } from "framer-motion"
import { useEffect, useMemo, useState } from "react"
import {
  createBoosterSubscription,
  createGlobalSubscription,
  createItemDropSubscription,
  createRaidSubscription,
} from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { EventArt, OwnerCrown } from "../components/EventArt"
import { useProductData } from "../data/ProductDataProvider"
import type {
  Account,
  BoosterType,
  FeedEvent,
  GlobalEventType,
  Route,
  Subscription,
} from "../model"
import { boosterLabels, eventLabels, globalEventLabels, itemTypeLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { formatOccurredAt } from "../timePresentation"

type Props = {
  eventId?: string
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

export function EventDetailProduction({ eventId, activeAccount, navigate, back }: Props) {
  const product = useProductData()
  const refreshEvents = product.refreshEvents
  const [followBusy, setFollowBusy] = useState(false)
  const event = product.feed.find((item) => item.id === eventId) ?? null

  useEffect(() => {
    if (!eventId || event) return

    void refreshEvents({ limit: 100 })
  }, [event, eventId, refreshEvents])

  const matchingSubscription = useMemo(
    () => event ? product.subscriptions.find((subscription) => matchesEvent(subscription, event)) ?? null : null,
    [event, product.subscriptions],
  )

  if (!event) {
    return (
      <div className="screen event-r4-screen">
        <PageHeader title="Подія" back={back} />
        <section className="events-r2-empty">
          <Glyph name="events" size={24} />
          <strong>{product.loadingEvents ? "Завантажуємо подію" : "Подію не знайдено"}</strong>
          <small>{product.loadingEvents ? "Оновлюємо стрічку…" : "Вона могла вийти за межі доступної історії."}</small>
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
      <PageHeader title={eventTitle(event)} eyebrow={eventLabels[event.kind]} back={back} />

      <motion.section
        className="event-r4-hero"
        initial={{ opacity: 0, y: 6 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <span className="event-r4-hero__scan" aria-hidden="true"><i /><i /><i /></span>
        <span className="event-r4-hero__art"><EventArt event={event} size={66} /></span>

        <span className="event-r4-hero__copy">
          <small>{eventKindCaption(event)}</small>
          <strong>{eventSubject(event)}</strong>
          <em>{eventDescription(event)}</em>
          {ownEvent ? <span className="event-r4-owner"><OwnerCrown compact />Твій аккаунт</span> : null}
        </span>

        <time>{formatOccurredAt(event.occurredAt)}</time>
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
          <small>СПОВІЩЕННЯ</small>
          <strong>{matchingSubscription ? "Стеження увімкнено" : "Стежити за такими"}</strong>
          <em>{notificationRuleDescription(event)}</em>
        </span>

        <motion.button
          className={matchingSubscription ? "is-active" : ""}
          whileTap={{ scale: 0.96 }}
          disabled={followBusy}
          onClick={() => void toggleFollow()}
        >
          {followBusy ? "…" : matchingSubscription ? "Не стежити" : "Стежити"}
        </motion.button>

        <button className="event-r4-follow__manage" onClick={() => navigate({ id: "subscriptions" })}>
          Усі правила
        </button>
      </section>

      <section className="event-r4-facts" aria-label="Дані події">
        <article className="is-wide">
          <small>СТАЛОСЯ</small>
          <strong>{new Intl.DateTimeFormat("uk-UA", { dateStyle: "medium", timeStyle: "short" }).format(new Date(event.occurredAt))}</strong>
          <i aria-hidden="true" />
        </article>

        <article>
          <small>КАТЕГОРІЯ</small>
          <strong>{eventTypeLabel(event)}</strong>
        </article>

        <article>
          <small>{event.rarity ? "РІДКІСТЬ" : "ДЖЕРЕЛО"}</small>
          <strong>{event.rarity ? rarityLabel(event.rarity) : "Cristalix"}</strong>
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

function eventTitle(event: FeedEvent) {
  if (event.kind === "GLOBAL") {
    return globalEventLabels[event.title as keyof typeof globalEventLabels] ?? event.title
  }

  if (event.kind === "BOOSTER") {
    return boosterLabels[event.title as keyof typeof boosterLabels] ?? event.title
  }

  if (event.kind === "RAID") return "Рейд"
  return event.title
}

function eventKindCaption(event: FeedEvent) {
  if (event.kind === "ITEM_DROP") return "ЗАФІКСОВАНО ДРОП"
  if (event.kind === "BOOSTER") return "АКТИВОВАНО БУСТЕР"
  if (event.kind === "GLOBAL") return "ГЛОБАЛЬНА ПОДІЯ"
  return "ЗАФІКСОВАНО РЕЙД"
}

function eventSubject(event: FeedEvent) {
  if (event.kind === "GLOBAL") return "Cristalix"
  if (event.kind === "RAID") return `Локації ${event.detail}`
  return event.detail
}

function eventDescription(event: FeedEvent) {
  if (event.kind === "ITEM_DROP") {
    return `${eventTitle(event)} · ${rarityLabel(event.rarity) ?? eventTypeLabel(event)}`
  }

  if (event.kind === "BOOSTER") return eventTitle(event)
  if (event.kind === "GLOBAL") return eventTitle(event)
  return "Рейдова активність"
}

function eventTypeLabel(event: FeedEvent) {
  if (event.kind === "ITEM_DROP" && event.meta && event.meta in itemTypeLabels) {
    return itemTypeLabels[event.meta as keyof typeof itemTypeLabels]
  }

  if (event.kind === "BOOSTER") return eventTitle(event)
  if (event.kind === "GLOBAL") return "Глобальна"
  if (event.kind === "RAID") return "Рейд"
  return eventLabels[event.kind]
}

function rarityLabel(value?: string) {
  if (value === "MYTHICAL") return "Міфічний"
  if (value === "SECRET") return "Секретний"
  return value
}

function belongsToAccount(event: FeedEvent, account: Account) {
  return (event.kind === "ITEM_DROP" || event.kind === "BOOSTER")
    && event.detail.toLowerCase() === account.nickname.toLowerCase()
}

function notificationRuleDescription(event: FeedEvent) {
  if (event.kind === "ITEM_DROP") return `Предмет: ${event.title}`
  if (event.kind === "BOOSTER") return `Бустер: ${eventTitle(event)}`
  if (event.kind === "GLOBAL") return `Глобальна: ${eventTitle(event)}`
  return `Рейд: локації ${event.detail}`
}

function parseLocations(value: string) {
  return value
    .split(/[·,\s]+/)
    .map((entry) => Number(entry.trim()))
    .filter((entry) => Number.isInteger(entry) && entry > 0)
}
