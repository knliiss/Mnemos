import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useMemo, useState } from "react"
import { EventArt, OwnerCrown } from "../components/EventArt"
import { useProductData } from "../data/ProductDataProvider"
import { eventVisualTone } from "../eventVisualTone"
import type { Account, EventKind, FeedEvent, Route } from "../model"
import { eventLabels, globalEventLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { formatOccurredAt } from "../timePresentation"

type Props = {
  activeAccount: Account
  navigate: (route: Route) => void
}

type EventFilter = "ALL" | EventKind

type FeedSort = "NEWEST" | "OLDEST"

const filters: EventKind[] = ["ITEM_DROP", "BOOSTER", "GLOBAL", "RAID"]

export function EventsFeedProduction({ activeAccount, navigate }: Props) {
  const product = useProductData()
  const [filter, setFilter] = useState<EventFilter>("ALL")
  const [query, setQuery] = useState("")
  const [sort, setSort] = useState<FeedSort>("NEWEST")

  useEffect(() => {
    const timeout = window.setTimeout(() => {
      void product.refreshEvents({
        types: filter === "ALL" ? undefined : [filter],
        search: query,
        sort,
        limit: 60,
      })
    }, query.trim() ? 240 : 0)

    return () => window.clearTimeout(timeout)
  }, [filter, query, sort, product.refreshEvents])

  const visible = useMemo(() => product.feed, [product.feed])
  const featured = visible[0]
  const rest = visible.slice(1)

  return (
    <div className="screen events-r2-screen root-screen">
      <header className="root-title-block events-r2-title">
        <span>
          <small>LIVE FEED</small>
          <h1>Події</h1>
          <p>Що відбувається на Cristalix просто зараз</p>
        </span>
        <button className="events-r2-subscriptions" onClick={() => navigate({ id: "subscriptions" })}>
          <Glyph name="bell" size={17} />
          <b>{product.subscriptions.length}</b>
        </button>
      </header>

      <section className="events-r2-filter-rail" aria-label="Тип подій">
        <button className={filter === "ALL" ? "is-active" : ""} onClick={() => setFilter("ALL")}>
          <span className="events-r2-filter-dot" />
          Усі
        </button>
        {filters.map((kind) => (
          <button key={kind} className={filter === kind ? "is-active" : ""} onClick={() => setFilter(kind)}>
            <Glyph name={eventGlyph(kind)} size={15} />
            {eventLabels[kind]}
          </button>
        ))}
      </section>

      <div className="events-r2-query-row">
        <label className="events-r2-search">
          <Glyph name="search" size={18} />
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Предмет, нік або подія" />
          {query ? <button onClick={() => setQuery("")} aria-label="Очистити"><Glyph name="close" size={14} /></button> : null}
        </label>
        <motion.button
          type="button"
          className={`events-r2-sort ${sort === "OLDEST" ? "is-oldest" : ""}`}
          whileTap={{ scale: 0.94 }}
          onClick={() => setSort((current) => current === "NEWEST" ? "OLDEST" : "NEWEST")}
          aria-label={sort === "NEWEST" ? "Спочатку нові" : "Спочатку старі"}
          title={sort === "NEWEST" ? "Спочатку нові" : "Спочатку старі"}
        >
          <Glyph name="swap" size={18} />
        </motion.button>
      </div>

      {product.loadingEvents && !visible.length ? (
        <section className="events-r2-loading" aria-label="Завантаження подій">
          <i /><i /><i />
        </section>
      ) : null}

      {featured ? (
        <motion.button
          className={`events-r2-featured event-surface--${eventVisualTone(featured)}`}
          whileTap={{ scale: 0.992 }}
          onClick={() => navigate({ id: "event-detail", params: { eventId: featured.id } })}
        >
          <span className="events-r2-featured__signal"><i /><i /><i /></span>
          <span className="events-r2-featured__art"><EventArt event={featured} size={78} /></span>
          <span className="events-r2-featured__copy">
            <small>{sort === "NEWEST" ? "ОСТАННЄ" : "НАЙСТАРІШЕ"} · {formatOccurredAt(featured.occurredAt)}</small>
            <strong>{eventDisplayTitle(featured)}</strong>
            <em>{eventDisplayDescription(featured)}</em>
            <span>{eventLabels[featured.kind]}{belongsToAccount(featured, activeAccount) ? <OwnerCrown compact /> : null}</span>
          </span>
          <b className="events-r2-featured__open">›</b>
        </motion.button>
      ) : null}

      <header className="events-r2-section-head">
        <span><small>СТРІЧКА</small><strong>{visible.length} подій</strong></span>
        <i className={product.loadingEvents ? "is-loading" : ""} />
      </header>

      <section className="events-r2-stream">
        <AnimatePresence initial={false}>
          {rest.map((event, index) => (
            <motion.button
              layout
              key={event.id}
              className={`events-r2-row event-surface--${eventVisualTone(event)}`}
              initial={{ opacity: 0, y: 5 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.985 }}
              transition={{ delay: Math.min(index * 0.02, 0.08) }}
              onClick={() => navigate({ id: "event-detail", params: { eventId: event.id } })}
            >
              <span className="events-r2-row__art"><EventArt event={event} size={48} /></span>
              <span className="events-r2-row__copy">
                <small>{eventLabels[event.kind]}</small>
                <strong>{eventDisplayTitle(event)}</strong>
                <em>{eventDisplayDescription(event)}</em>
              </span>
              <span className="events-r2-row__time">{formatOccurredAt(event.occurredAt)}</span>
              <span className="events-r2-row__edge" />
            </motion.button>
          ))}
        </AnimatePresence>
      </section>

      {!product.loadingEvents && !visible.length ? (
        <section className="events-r2-empty">
          <Glyph name="events" size={24} />
          <strong>Подій не знайдено</strong>
          <small>Зміни тип, порядок або пошуковий запит</small>
        </section>
      ) : null}
    </div>
  )
}

function eventGlyph(kind: EventKind): "item-drop" | "booster" | "global" | "raid" {
  if (kind === "ITEM_DROP") return "item-drop"
  if (kind === "BOOSTER") return "booster"
  if (kind === "GLOBAL") return "global"
  return "raid"
}

function eventDisplayTitle(event: FeedEvent) {
  if (event.kind === "GLOBAL") {
    return globalEventLabels[event.title as keyof typeof globalEventLabels] ?? event.title
  }

  if (event.kind === "BOOSTER") {
    const labels: Record<string, string> = {
      LUCK: "Удача",
      MONEY: "Монети",
      DAMAGE: "Шкода",
      POWER: "Сила",
    }
    return labels[event.title] ?? event.title
  }

  if (event.kind === "RAID") return "Рейд"
  return event.title
}

function eventDisplayDescription(event: FeedEvent) {
  if (event.kind === "ITEM_DROP") {
    return [event.detail, event.rarity].filter(Boolean).join(" · ")
  }

  if (event.kind === "BOOSTER") return event.detail
  if (event.kind === "GLOBAL") return "Глобальна подія Cristalix"
  return event.detail ? `Локації ${event.detail}` : "Рейд Cristalix"
}

function belongsToAccount(event: FeedEvent, activeAccount: Account) {
  return event.kind === "ITEM_DROP" && event.detail.toLowerCase() === activeAccount.nickname.toLowerCase()
}
