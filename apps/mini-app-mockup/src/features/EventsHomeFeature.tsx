import { useMemo, useState } from "react"
import { EVENT_KINDS } from "../contracts"
import { feed, initialSubscriptions } from "../mockData"
import type { EventKind, FeedEvent, Route } from "../model"
import { boosterLabels, eventLabels, globalEventLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader } from "../shared/Primitives"

type Nav = (route: Route) => void

const icons: Record<EventKind, "item-drop" | "booster" | "global" | "raid"> = {
  ITEM_DROP: "item-drop",
  BOOSTER: "booster",
  GLOBAL: "global",
  RAID: "raid",
}

const eventTone: Record<EventKind, string> = {
  ITEM_DROP: "amber",
  BOOSTER: "lime",
  GLOBAL: "plum",
  RAID: "neutral",
}

function eventTitle(event: FeedEvent) {
  if (event.kind === "GLOBAL" && event.title in globalEventLabels) {
    return globalEventLabels[event.title as keyof typeof globalEventLabels]
  }

  if (event.kind === "BOOSTER" && event.title in boosterLabels) {
    return boosterLabels[event.title as keyof typeof boosterLabels]
  }

  if (event.kind === "RAID") return "Рейд"

  return event.title
}

function eventTime(occurredAt: string) {
  const date = new Date(occurredAt)
  const diff = Date.now() - date.getTime()
  const minutes = Math.max(1, Math.floor(diff / 60_000))

  if (minutes < 60) return `${minutes} хв тому`
  if (minutes < 24 * 60) return `${Math.floor(minutes / 60)} год тому`

  return new Intl.DateTimeFormat("uk-UA", {
    day: "numeric",
    month: "short",
    year: date.getFullYear() === new Date().getFullYear() ? undefined : "numeric",
  }).format(date)
}

export function EventsHome({ navigate }: { navigate: Nav }) {
  const [filter, setFilter] = useState<"ALL" | EventKind>("ALL")
  const visible = useMemo(() => filter === "ALL" ? feed : feed.filter((event) => event.kind === filter), [filter])
  const open = (event: FeedEvent) => navigate({ id: "event-detail", params: { eventId: event.id } })

  return (
    <div className="screen events-root-screen root-screen">
      <header className="root-title-block events-title-block">
        <span><h1>Події</h1><p>Останнє з Cristalix</p></span>
        <button className="events-subscription-button" onClick={() => navigate({ id: "subscriptions" })}>
          <Glyph name="bell" size={18} />
          <span>Підписки</span>
          <b>{initialSubscriptions.length}</b>
        </button>
      </header>

      <div className="event-filter event-filter--redesign">
        <button className={filter === "ALL" ? "is-active" : ""} onClick={() => setFilter("ALL")}>Усі</button>
        {EVENT_KINDS.map((value) => (
          <button key={value} className={filter === value ? "is-active" : ""} onClick={() => setFilter(value)}>
            <Glyph name={icons[value]} size={15} />
            {eventLabels[value]}
          </button>
        ))}
      </div>

      <div className="event-stream-redesign">
        {visible.map((event, index) => (
          <button className={`event-feed-card event-feed-card--${eventTone[event.kind]} ${index === 0 ? "is-latest" : ""}`} key={event.id} onClick={() => open(event)}>
            <span className="event-feed-card__icon"><Glyph name={icons[event.kind]} size={20} /></span>
            <span className="event-feed-card__copy">
              <span className="event-feed-card__meta"><b>{eventLabels[event.kind]}</b><time>{eventTime(event.occurredAt)}</time></span>
              <strong>{eventTitle(event)}</strong>
              <small>{event.kind === "ITEM_DROP" ? `${event.detail}${event.rarity ? ` · ${event.rarity}` : ""}` : event.detail}</small>
            </span>
            <span className="event-feed-card__chevron">›</span>
          </button>
        ))}
      </div>
    </div>
  )
}

export function EventDetail({ eventId, back }: { eventId?: string; back: () => void }) {
  const event = feed.find((item) => item.id === eventId) ?? feed[0]
  const ownDrop = event.kind === "ITEM_DROP" && event.detail.toLowerCase() === "knalis"
  const [spotlight, setSpotlight] = useState<"READY" | "PENDING" | "ACTIVE">("READY")

  const publish = () => {
    if (spotlight !== "READY") return

    setSpotlight("PENDING")
    window.setTimeout(() => setSpotlight("ACTIVE"), 900)
  }

  return (
    <div className="screen event-detail-screen event-detail-redesign">
      <PageHeader title={eventTitle(event)} eyebrow={eventLabels[event.kind]} back={back} />
      <section className={`event-detail-hero event-detail-hero--${eventTone[event.kind]}`}>
        <span><Glyph name={icons[event.kind]} size={30} /></span>
        <div><strong>{event.detail}</strong><small>{event.rarity ?? event.meta ?? eventLabels[event.kind]}</small></div>
        <time>{eventTime(event.occurredAt)}</time>
      </section>
      <dl className="event-facts">
        <div><dt>Сталося</dt><dd>{new Intl.DateTimeFormat("uk-UA", { dateStyle: "medium", timeStyle: "short" }).format(new Date(event.occurredAt))}</dd></div>
        {event.meta ? <div><dt>Тип</dt><dd>{event.meta}</dd></div> : null}
        {event.rarity ? <div><dt>Рідкість</dt><dd>{event.rarity}</dd></div> : null}
      </dl>
      {ownDrop ? (
        <section className="spotlight-action spotlight-action--redesign">
          <div className="spotlight-action__mark"><Glyph name="spark" size={21} /></div>
          <div><strong>Алерт дропа</strong><small>Показати дроп користувачам Mini App</small></div>
          {spotlight === "READY" ? <button onClick={publish} aria-label="Купити алерт"><CurrencyValue currency="PRIME" value={25} compact /></button> : <span className={`spotlight-state spotlight-state--${spotlight.toLowerCase()}`}>{spotlight === "PENDING" ? "обробка" : "активний"}</span>}
        </section>
      ) : null}
    </div>
  )
}
