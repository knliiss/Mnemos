import { useMemo, useState } from "react"
import { EVENT_KINDS } from "../contracts"
import { feed } from "../mockData"
import type { EventKind, FeedEvent, Route } from "../model"
import { eventLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, IconButton, PageHeader, StatusDot } from "../shared/Primitives"

type Nav = (route: Route) => void
const icons: Record<EventKind, "item-drop" | "booster" | "global" | "raid"> = { ITEM_DROP: "item-drop", BOOSTER: "booster", GLOBAL: "global", RAID: "raid" }

export function EventsHome({ navigate }: { navigate: Nav }) {
  const [filter, setFilter] = useState<"ALL" | EventKind>("ALL")
  const visible = useMemo(() => filter === "ALL" ? feed : feed.filter(event => event.kind === filter), [filter])
  const latest = visible[0]
  const open = (event: FeedEvent) => navigate({ id: "event-detail", params: { eventId: event.id } })

  return <div className="screen"><PageHeader title="Події" actions={<><IconButton icon="search" label="Пошук" onClick={() => navigate({ id: "search" })} /><IconButton icon="bell" label="Підписки" onClick={() => navigate({ id: "subscriptions" })} /><IconButton icon="bet" label="Ставки" onClick={() => navigate({ id: "bets" })} /></>} />
    {latest ? <button className="event-lead" onClick={() => open(latest)}><span className="event-lead__glyph"><Glyph name={icons[latest.kind]} size={31} /></span><span className="event-lead__main"><span className="event-lead__top"><StatusDot tone="live" />{eventLabels[latest.kind]}<time>{latest.relative}</time></span><strong>{latest.title}</strong><span>{latest.detail}{latest.rarity ? ` · ${latest.rarity}` : ""}</span></span></button> : null}
    <div className="event-filter"><button className={filter === "ALL" ? "is-active" : ""} onClick={() => setFilter("ALL")}>Усі</button>{EVENT_KINDS.map(value => <button key={value} className={filter === value ? "is-active" : ""} onClick={() => setFilter(value)}>{eventLabels[value]}</button>)}</div>
    <div className="event-stream">{visible.map((event, index) => <button className="event-line event-line--button" key={event.id} onClick={() => open(event)}><span className="event-line__axis"><span className={index === 0 ? "is-live" : ""} /></span><span className="event-line__glyph"><Glyph name={icons[event.kind]} size={18} /></span><span className="event-line__copy"><strong>{event.title}</strong><small>{event.detail}{event.meta ? ` · ${event.meta}` : ""}</small></span><time>{event.relative}</time></button>)}</div>
  </div>
}

export function EventDetail({ eventId, back }: { eventId?: string; back: () => void }) {
  const event = feed.find(item => item.id === eventId) ?? feed[0]
  const ownDrop = event.kind === "ITEM_DROP" && event.detail.toLowerCase() === "knalis"
  const [spotlight, setSpotlight] = useState<"READY" | "PENDING" | "ACTIVE">("READY")
  const publish = () => { if (spotlight !== "READY") return; setSpotlight("PENDING"); window.setTimeout(() => setSpotlight("ACTIVE"), 900) }

  return <div className="screen event-detail-screen"><PageHeader title={event.title} eyebrow={eventLabels[event.kind]} back={back} /><section className="event-detail-hero"><span><Glyph name={icons[event.kind]} size={34} /></span><div><strong>{event.detail}</strong><small>{event.rarity ?? event.meta ?? eventLabels[event.kind]}</small></div><time>{event.relative}</time></section><dl className="event-facts"><div><dt>Час</dt><dd>{event.occurredAt}</dd></div>{event.meta ? <div><dt>Тип</dt><dd>{event.meta}</dd></div> : null}{event.rarity ? <div><dt>Рідкість</dt><dd>{event.rarity}</dd></div> : null}</dl>{ownDrop ? <section className="spotlight-action"><div className="spotlight-action__mark"><Glyph name="spark" size={22} /></div><div><strong>Spotlight</strong><small>1 год · дроп до 2 год</small></div>{spotlight === "READY" ? <button onClick={publish} aria-label="Купити Spotlight"><CurrencyValue currency="PRIME" value={25} compact /></button> : <span className={`spotlight-state spotlight-state--${spotlight.toLowerCase()}`}>{spotlight === "PENDING" ? "обробка" : "активний"}</span>}</section> : null}</div>
}
