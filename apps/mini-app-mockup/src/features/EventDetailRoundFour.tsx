import { motion } from "framer-motion"
import { useState } from "react"
import { EventArt, OwnerCrown } from "../components/EventArt"
import { feed, initialSubscriptions } from "../mockData"
import type { Account, FeedEvent, Route } from "../model"
import { boosterLabels, eventLabels, globalEventLabels, itemTypeLabels } from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { formatOccurredAt } from "../timePresentation"

type Props = {
  eventId?: string
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

export function EventDetailRoundFour({ eventId, activeAccount, navigate, back }: Props) {
  const event = feed.find((item) => item.id === eventId) ?? feed[0]
  const ownEvent = belongsToAccount(event, activeAccount)
  const ownDrop = event.kind === "ITEM_DROP" && ownEvent
  const [subscribed, setSubscribed] = useState(() => hasMatchingSubscription(event))
  const [spotlight, setSpotlight] = useState<"READY" | "PENDING" | "SCHEDULED">("READY")

  const publish = () => {
    if (spotlight !== "READY") return

    setSpotlight("PENDING")
    window.setTimeout(() => setSpotlight("SCHEDULED"), 900)
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

      <section className={`event-r4-follow ${subscribed ? "is-active" : ""}`}>
        <span className="event-r4-follow__mark" aria-hidden="true">
          <Glyph name="bell" size={18} />
          <i />
        </span>

        <span className="event-r4-follow__copy">
          <small>СПОВІЩЕННЯ</small>
          <strong>{subscribed ? "Стеження увімкнено" : "Стежити за такими"}</strong>
          <em>{notificationRuleDescription(event)}</em>
        </span>

        <motion.button
          className={subscribed ? "is-active" : ""}
          whileTap={{ scale: 0.96 }}
          onClick={() => setSubscribed((value) => !value)}
        >
          {subscribed ? "Не стежити" : "Стежити"}
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

      {ownDrop ? (
        <section className="event-r4-spotlight">
          <span className="event-r4-spotlight__art" aria-hidden="true"><Glyph name="spark" size={20} /></span>
          <span className="event-r4-spotlight__copy">
            <small>SPOTLIGHT</small>
            <strong>Показати цей дроп у Mini App</strong>
            <em>Одноразовий алерт усім користувачам з твоїм активним notification pack.</em>
          </span>

          {spotlight === "READY" ? (
            <motion.button whileTap={{ scale: 0.96 }} onClick={publish} aria-label="Купити Spotlight за 25 PRIME">
              <CurrencyMark currency="PRIME" size={14} />
              <strong>25</strong>
            </motion.button>
          ) : (
            <span className={`event-r4-spotlight__state is-${spotlight.toLowerCase()}`}>
              {spotlight === "PENDING" ? "Обробка" : "У черзі"}
            </span>
          )}
        </section>
      ) : null}
    </div>
  )
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

function hasMatchingSubscription(event: FeedEvent) {
  return initialSubscriptions.some((subscription) => {
    if (event.kind === "ITEM_DROP" && subscription.type === "ITEM_DROP") {
      const itemMatches = !subscription.itemName
        || subscription.itemName.toLowerCase() === event.title.toLowerCase()
      const accountMatches = !subscription.droppedFor
        || subscription.droppedFor.toLowerCase() === event.detail.toLowerCase()

      return itemMatches && accountMatches
    }

    if (event.kind === "BOOSTER" && subscription.type === "BOOSTER") {
      return subscription.boosterType === event.title
    }

    if (event.kind === "GLOBAL" && subscription.type === "GLOBAL") {
      return subscription.eventType === event.title
    }

    if (event.kind === "RAID" && subscription.type === "RAID") {
      if (!subscription.locations.length) return true

      const locations = event.detail
        .split("·")
        .map((value) => Number(value.trim()))
        .filter((value) => Number.isInteger(value))

      return subscription.locations.some((location) => locations.includes(location))
    }

    return false
  })
}
