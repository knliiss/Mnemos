import type { TFunction } from "i18next"
import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { EventArt, OwnerCrown } from "../components/EventArt"
import { useProductData } from "../data/ProductDataProvider"
import { eventVisualTone } from "../eventVisualTone"
import type { Account, EventKind, FeedEvent, Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { formatOccurredAt } from "../timePresentation"

type Props = {
  activeAccount: Account
  navigate: (route: Route) => void
}

type EventFilter = "ALL" | EventKind

type FeedSort = "NEWEST" | "OLDEST"

const filters: EventKind[] = ["ITEM_DROP", "BOOSTER", "GLOBAL", "RAID"]

export function EventsFeed({ activeAccount, navigate }: Props) {
  const { t, i18n } = useTranslation()
  const { feed, loadingEvents, refreshEvents, subscriptions } = useProductData()
  const [filter, setFilter] = useState<EventFilter>("ALL")
  const [query, setQuery] = useState("")
  const [sort, setSort] = useState<FeedSort>("NEWEST")
  const locale = localeForLanguage(i18n.resolvedLanguage)

  useEffect(() => {
    const timeout = window.setTimeout(
      () => {
        void refreshEvents({
          types: filter === "ALL" ? undefined : [filter],
          search: query,
          sort,
          limit: 60,
        })
      },
      query.trim() ? 240 : 0,
    )

    return () => window.clearTimeout(timeout)
  }, [filter, query, refreshEvents, sort])

  const featured = feed[0]
  const rest = feed.slice(1)
  const sortLabel = sort === "NEWEST" ? t("events.feed.newest") : t("events.feed.oldest")

  return (
    <div className="screen events-r2-screen root-screen">
      <header className="root-title-block events-r2-title">
        <span>
          <small>LIVE FEED</small>
          <h1>{t("events.feed.title")}</h1>
          <p>{t("events.feed.description")}</p>
        </span>
        <button
          className="events-r2-subscriptions"
          onClick={() => navigate({ id: "subscriptions" })}
        >
          <Glyph name="bell" size={17} />
          <b>{subscriptions.length}</b>
        </button>
      </header>

      <section className="events-r2-filter-rail" aria-label={t("events.feed.filterAria")}>
        <button className={filter === "ALL" ? "is-active" : ""} onClick={() => setFilter("ALL")}>
          <span className="events-r2-filter-dot" />
          {t("events.feed.all")}
        </button>
        {filters.map((kind) => (
          <button
            key={kind}
            className={filter === kind ? "is-active" : ""}
            onClick={() => setFilter(kind)}
          >
            <Glyph name={eventGlyph(kind)} size={15} />
            {eventKindLabel(t, kind)}
          </button>
        ))}
      </section>

      <div className="events-r2-query-row">
        <label className="events-r2-search">
          <Glyph name="search" size={18} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder={t("events.feed.searchPlaceholder")}
          />
          {query ? (
            <button onClick={() => setQuery("")} aria-label={t("events.feed.clear")}>
              <Glyph name="close" size={14} />
            </button>
          ) : null}
        </label>
        <motion.button
          type="button"
          className={`events-r2-sort ${sort === "OLDEST" ? "is-oldest" : ""}`}
          whileTap={{ scale: 0.94 }}
          onClick={() => setSort((current) => (current === "NEWEST" ? "OLDEST" : "NEWEST"))}
          aria-label={sortLabel}
          title={sortLabel}
        >
          <Glyph name="swap" size={18} />
        </motion.button>
      </div>

      {loadingEvents && !feed.length ? (
        <section className="events-r2-loading" aria-label={t("events.feed.loading")}>
          <i />
          <i />
          <i />
        </section>
      ) : null}

      {featured ? (
        <motion.button
          className={`events-r2-featured event-surface--${eventVisualTone(featured)}`}
          whileTap={{ scale: 0.992 }}
          onClick={() => navigate({ id: "event-detail", params: { eventId: featured.id } })}
        >
          <span className="events-r2-featured__signal">
            <i />
            <i />
            <i />
          </span>
          <span className="events-r2-featured__art">
            <EventArt event={featured} size={78} />
          </span>
          <span className="events-r2-featured__copy">
            <small>
              {sort === "NEWEST" ? t("events.feed.latest") : t("events.feed.oldestFeatured")} ·{" "}
              {formatOccurredAt(featured.occurredAt, locale)}
            </small>
            <strong>{eventDisplayTitle(t, featured)}</strong>
            <em>{eventDisplayDescription(t, featured)}</em>
            <span>
              {eventKindLabel(t, featured.kind)}
              {belongsToAccount(featured, activeAccount) ? <OwnerCrown compact /> : null}
            </span>
          </span>
          <b className="events-r2-featured__open">›</b>
        </motion.button>
      ) : null}

      <header className="events-r2-section-head">
        <span>
          <small>{t("events.feed.stream")}</small>
          <strong>{t("events.feed.eventCount", { count: feed.length })}</strong>
        </span>
        <i className={loadingEvents ? "is-loading" : ""} />
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
              <span className="events-r2-row__art">
                <EventArt event={event} size={48} />
              </span>
              <span className="events-r2-row__copy">
                <small>{eventKindLabel(t, event.kind)}</small>
                <strong>{eventDisplayTitle(t, event)}</strong>
                <em>{eventDisplayDescription(t, event)}</em>
              </span>
              <span className="events-r2-row__time">
                {formatOccurredAt(event.occurredAt, locale)}
              </span>
              <span className="events-r2-row__edge" />
            </motion.button>
          ))}
        </AnimatePresence>
      </section>

      {!loadingEvents && !feed.length ? (
        <section className="events-r2-empty">
          <Glyph name="events" size={24} />
          <strong>{t("events.feed.emptyTitle")}</strong>
          <small>{t("events.feed.emptyCopy")}</small>
        </section>
      ) : null}
    </div>
  )
}

function eventGlyph(kind: EventKind): "item-drop" | "booster" | "global" | "raid" {
  if (kind === "ITEM_DROP") {
    return "item-drop"
  }
  if (kind === "BOOSTER") {
    return "booster"
  }
  if (kind === "GLOBAL") {
    return "global"
  }
  return "raid"
}

function eventKindLabel(t: TFunction, kind: EventKind) {
  return t(`events.kinds.${kind}`)
}

function eventDisplayTitle(t: TFunction, event: FeedEvent) {
  if (event.kind === "GLOBAL") {
    return t(`events.globals.${event.title}`, { defaultValue: event.title })
  }

  if (event.kind === "BOOSTER") {
    return t(`events.boosters.${event.title}`, { defaultValue: event.title })
  }

  if (event.kind === "RAID") {
    return t("events.kinds.RAID")
  }
  return event.title
}

function eventDisplayDescription(t: TFunction, event: FeedEvent) {
  if (event.kind === "ITEM_DROP") {
    return [event.detail, event.rarity].filter(Boolean).join(" · ")
  }

  if (event.kind === "BOOSTER") {
    return event.detail
  }
  if (event.kind === "GLOBAL") {
    return t("events.feed.globalDescription")
  }
  return event.detail
    ? t("events.feed.locations", { locations: event.detail })
    : t("events.feed.raidDescription")
}

function belongsToAccount(event: FeedEvent, activeAccount: Account) {
  return (
    event.kind === "ITEM_DROP" &&
    event.detail.toLowerCase() === activeAccount.nickname.toLowerCase()
  )
}

function localeForLanguage(language?: string) {
  if (language === "en") {
    return "en-US"
  }
  if (language === "ru") {
    return "ru-RU"
  }
  return "uk-UA"
}
