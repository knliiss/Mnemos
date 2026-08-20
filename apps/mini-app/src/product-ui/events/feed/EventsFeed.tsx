import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import type { EventFeedQuery } from "@/shared/api/productApi"
import { EventArt, OwnerCrown } from "../../components/events/EventArt"
import { useProductData } from "../../data/ProductDataProvider"
import { eventVisualTone } from "../../eventVisualTone"
import type { Account, EventKind, ItemRarity, Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { FeatureLoading } from "../../system/FeatureLoading"
import { formatOccurredAt } from "../../timePresentation"
import {
  belongsToAccount,
  eventFeedDescription,
  eventGlyph,
  eventKindLabel,
  eventTitle,
  localeForLanguage,
} from "../eventPresentation"

type Props = {
  activeAccount: Account
  navigate: (route: Route) => void
}

type EventFilter = "ALL" | EventKind
type FeedSort = "NEWEST" | "OLDEST"
type ItemRarityFilter = "ALL" | ItemRarity

const filters: EventKind[] = ["ITEM_DROP", "BOOSTER", "GLOBAL", "RAID"]
const rarityFilters: ItemRarity[] = ["MYTHICAL", "SECRET"]

export function EventsFeed({ activeAccount, navigate }: Props) {
  const { t, i18n } = useTranslation()
  const {
    feed,
    eventsHasMore,
    loadingEvents,
    loadMoreEvents,
    refreshEvents,
    subscriptions,
  } = useProductData()
  const [filter, setFilter] = useState<EventFilter>("ALL")
  const [rarity, setRarity] = useState<ItemRarityFilter>("ALL")
  const [query, setQuery] = useState("")
  const [sort, setSort] = useState<FeedSort>("NEWEST")

  const locale = localeForLanguage(i18n.resolvedLanguage)
  const feedQuery = useMemo<EventFeedQuery>(
    () => ({
      types: filter === "ALL" ? undefined : [filter],
      search: query,
      itemRarity: rarity === "ALL" ? undefined : rarity,
      sort,
      limit: 60,
    }),
    [filter, query, rarity, sort],
  )

  useEffect(() => {
    const timeout = window.setTimeout(
      () => {
        void refreshEvents(feedQuery)
      },
      query.trim() ? 240 : 0,
    )

    return () => window.clearTimeout(timeout)
  }, [feedQuery, query, refreshEvents])

  const featured = feed[0]
  const rest = feed.slice(1)
  const sortLabel = sort === "NEWEST" ? t("events.feed.newest") : t("events.feed.oldest")

  const selectFilter = (nextFilter: EventFilter) => {
    setFilter(nextFilter)

    if (nextFilter !== "ITEM_DROP") {
      setRarity("ALL")
    }
  }

  return (
    <div className="screen events-feed-screen root-screen">
      <header className="root-title-block events-feed-title">
        <span>
          <small>LIVE FEED</small>
          <h1>{t("events.feed.title")}</h1>
          <p>{t("events.feed.description")}</p>
        </span>

        <button
          type="button"
          className="events-feed-subscriptions"
          onClick={() => navigate({ id: "subscriptions" })}
        >
          <Glyph name="bell" size={17} />
          <b>{subscriptions.length}</b>
        </button>
      </header>

      <section className="events-feed-filters" aria-label={t("events.feed.filterAria")}>
        <button
          type="button"
          className={filter === "ALL" ? "is-active" : ""}
          onClick={() => selectFilter("ALL")}
        >
          <span className="events-feed-filter-dot" />
          {t("events.feed.all")}
        </button>

        {filters.map((kind) => (
          <button
            type="button"
            key={kind}
            className={filter === kind ? "is-active" : ""}
            onClick={() => selectFilter(kind)}
          >
            <Glyph name={eventGlyph(kind)} size={15} />
            {eventKindLabel(t, kind)}
          </button>
        ))}
      </section>

      {filter === "ITEM_DROP" ? (
        <section
          className="events-feed-filters events-feed-rarity-filters"
          aria-label={t("events.feed.rarityFilterAria")}
        >
          <button
            type="button"
            className={rarity === "ALL" ? "is-active" : ""}
            onClick={() => setRarity("ALL")}
          >
            {t("events.feed.allRarities")}
          </button>

          {rarityFilters.map((itemRarity) => (
            <button
              type="button"
              key={itemRarity}
              className={rarity === itemRarity ? "is-active" : ""}
              onClick={() => setRarity(itemRarity)}
            >
              {t(`events.rarities.${itemRarity}`)}
            </button>
          ))}
        </section>
      ) : null}

      <div className="events-feed-query">
        <label className="events-feed-search">
          <Glyph name="search" size={18} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder={t("events.feed.searchPlaceholder")}
          />

          {query ? (
            <button type="button" onClick={() => setQuery("")} aria-label={t("events.feed.clear")}>
              <Glyph name="close" size={14} />
            </button>
          ) : null}
        </label>

        <motion.button
          type="button"
          className={`events-feed-sort ${sort === "OLDEST" ? "is-oldest" : ""}`}
          whileTap={{ scale: 0.94 }}
          onClick={() => setSort((current) => (current === "NEWEST" ? "OLDEST" : "NEWEST"))}
          aria-label={sortLabel}
          title={sortLabel}
        >
          <Glyph name="swap" size={18} />
        </motion.button>
      </div>

      {loadingEvents && !feed.length ? <FeatureLoading label={t("events.feed.loading")} /> : null}

      {featured ? (
        <motion.button
          type="button"
          className={`events-feed-featured event-surface--${eventVisualTone(featured)}`}
          whileTap={{ scale: 0.992 }}
          onClick={() =>
            navigate({
              id: "event-detail",
              params: { eventId: featured.id },
            })
          }
        >
          <span className="events-feed-featured__signal">
            <i />
            <i />
            <i />
          </span>

          <span className="events-feed-featured__art">
            <EventArt event={featured} size={78} />
          </span>

          <span className="events-feed-featured__copy">
            <small>
              {sort === "NEWEST" ? t("events.feed.latest") : t("events.feed.oldestFeatured")} ·{" "}
              {formatOccurredAt(featured.occurredAt, locale)}
            </small>
            <strong>{eventTitle(t, featured)}</strong>
            <em>{eventFeedDescription(t, featured)}</em>
            <span>
              {eventKindLabel(t, featured.kind)}
              {belongsToAccount(featured, activeAccount) ? <OwnerCrown compact /> : null}
            </span>
          </span>

          <b className="events-feed-featured__open">›</b>
        </motion.button>
      ) : null}

      <header className="events-feed-section-head">
        <span>
          <small>{t("events.feed.stream")}</small>
          <strong>{t("events.feed.eventCount", { count: feed.length })}</strong>
        </span>
        <i className={loadingEvents ? "is-loading" : ""} />
      </header>

      <section className="events-feed-stream">
        <AnimatePresence initial={false}>
          {rest.map((event, index) => (
            <motion.button
              layout
              type="button"
              key={event.id}
              className={`events-feed-row event-surface--${eventVisualTone(event)}`}
              initial={{ opacity: 0, y: 5 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.985 }}
              transition={{ delay: Math.min(index * 0.02, 0.08) }}
              onClick={() =>
                navigate({
                  id: "event-detail",
                  params: { eventId: event.id },
                })
              }
            >
              <span className="events-feed-row__art">
                <EventArt event={event} size={48} />
              </span>

              <span className="events-feed-row__copy">
                <small>{eventKindLabel(t, event.kind)}</small>
                <strong>{eventTitle(t, event)}</strong>
                <em>{eventFeedDescription(t, event)}</em>
              </span>

              <span className="events-feed-row__time">{formatOccurredAt(event.occurredAt, locale)}</span>
              <span className="events-feed-row__edge" />
            </motion.button>
          ))}
        </AnimatePresence>
      </section>

      {eventsHasMore ? (
        <motion.button
          type="button"
          className="events-feed-load-more"
          whileTap={{ scale: 0.98 }}
          disabled={loadingEvents}
          onClick={() => void loadMoreEvents(feedQuery)}
        >
          {loadingEvents ? t("events.feed.loading") : t("events.feed.loadMore")}
        </motion.button>
      ) : null}

      {!loadingEvents && !feed.length ? (
        <section className="events-feed-empty">
          <Glyph name="events" size={24} />
          <strong>{t("events.feed.emptyTitle")}</strong>
          <small>{t("events.feed.emptyCopy")}</small>
        </section>
      ) : null}
    </div>
  )
}
