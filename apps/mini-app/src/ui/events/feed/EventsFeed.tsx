import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { type EventFeedQuery, type EventItemType } from "@/shared/api/eventApi"
import { listWipes, type Wipe } from "@/shared/api/wipeApi"
import { handleError } from "@/shared/errors/errorHandler"
import { EventArt, OwnerCrown } from "../../components/events/EventArt"
import { RootNavIllustration } from "../../components/navigation/RootNavIllustration"
import { BET_ITEM_TYPES, BOOSTER_TYPES, GLOBAL_EVENT_TYPES } from "../../contracts"
import { useMnemosData } from "../../data/MnemosDataProvider"
import { eventVisualTone } from "../../eventVisualTone"
import type {
  Account,
  BoosterType,
  EventKind,
  GlobalEventType,
  ItemRarity,
  Route,
} from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { FeatureLoading } from "../../system/FeatureLoading"
import { formatOccurredAt } from "../../timePresentation"
import {
  belongsToAccount,
  eventFeedDescription,
  eventKindLabel,
  eventTitle,
  localeForLanguage,
} from "../eventPresentation"
import { EventKindMicroArt } from "./EventKindMicroArt"
import { readEventFeedRouteParams } from "./eventFeedRouteParams"
import { persistEventFeedState, readPersistedEventFeedState } from "./eventFeedSession"
import "./feed-controls.css"

type Props = {
  params?: Record<string, string>
  activeAccount: Account
  navigate: (route: Route) => void
}

type EventFilter = "ALL" | EventKind
type FeedSort = "NEWEST" | "OLDEST"
type ItemRarityFilter = "ALL" | ItemRarity
type ItemTypeFilter = "ALL" | EventItemType
type BoosterFilter = "ALL" | BoosterType
type GlobalFilter = "ALL" | GlobalEventType

const filters: EventKind[] = ["ITEM_DROP", "BOOSTER", "GLOBAL", "RAID"]
const rarityFilters: ItemRarity[] = ["MYTHICAL", "SECRET"]
const raidLocations = Array.from({ length: 28 }, (_, index) => index + 1)

function wipeStartedAt(wipe: Wipe): number {
  return new Date(wipe.startedAt).getTime()
}

export function EventsFeed({ params, activeAccount, navigate }: Props) {
  const { t, i18n } = useTranslation()
  const routeFilters = useMemo(() => readEventFeedRouteParams(params), [params])
  const persistedState = useMemo(
    () =>
      params && Object.keys(params).length > 0
        ? null
        : readPersistedEventFeedState(activeAccount.id),
    [activeAccount.id, params],
  )
  const initialFilters = persistedState ?? routeFilters
  const {
    feed,
    eventsHasMore,
    loadingEvents,
    eventsError,
    loadMoreEvents,
    refreshEvents,
    subscriptions,
  } = useMnemosData()
  const [filter, setFilter] = useState<EventFilter>(() => initialFilters.filter)
  const [rarity, setRarity] = useState<ItemRarityFilter>(() => initialFilters.rarity)
  const [itemType, setItemType] = useState<ItemTypeFilter>(() => initialFilters.itemType)
  const [boosterType, setBoosterType] = useState<BoosterFilter>(() => initialFilters.boosterType)
  const [globalType, setGlobalType] = useState<GlobalFilter>(() => initialFilters.globalType)
  const [raidLocation, setRaidLocation] = useState<number | null>(() => initialFilters.raidLocation)
  const [query, setQuery] = useState(() => initialFilters.query)
  const [sort, setSort] = useState<FeedSort>(() => persistedState?.sort ?? "NEWEST")
  const [wipes, setWipes] = useState<Wipe[]>([])
  const [wipeId, setWipeId] = useState<string | null>(() => persistedState?.wipeId ?? null)
  const [referenceNow, setReferenceNow] = useState(() => Date.now())

  const locale = localeForLanguage(i18n.resolvedLanguage)
  const selectableWipes = useMemo(() => {
    const current = wipes.find((wipe) => wipe.active) ?? null
    const previous =
      wipes
        .filter((wipe) => !wipe.active && wipeStartedAt(wipe) <= referenceNow)
        .sort((left, right) => wipeStartedAt(right) - wipeStartedAt(left))[0] ?? null

    return [current, previous].filter((wipe): wipe is Wipe => wipe !== null)
  }, [referenceNow, wipes])

  const feedQuery = useMemo<EventFeedQuery>(
    () => ({
      types: filter === "ALL" ? undefined : [filter],
      search: query,
      itemRarity: filter === "ITEM_DROP" && rarity !== "ALL" ? rarity : undefined,
      itemType: filter === "ITEM_DROP" && itemType !== "ALL" ? itemType : undefined,
      boosterType: filter === "BOOSTER" && boosterType !== "ALL" ? boosterType : undefined,
      globalType: filter === "GLOBAL" && globalType !== "ALL" ? globalType : undefined,
      raidLocation: filter === "RAID" && raidLocation !== null ? raidLocation : undefined,
      wipeId: wipeId ?? undefined,
      sort,
      limit: 60,
    }),
    [boosterType, filter, globalType, itemType, query, raidLocation, rarity, sort, wipeId],
  )

  useEffect(() => {
    const interval = window.setInterval(() => {
      setReferenceNow(Date.now())
    }, 30_000)

    return () => window.clearInterval(interval)
  }, [])

  useEffect(() => {
    let cancelled = false

    void listWipes()
      .then((result) => {
        if (cancelled) {
          return
        }

        setWipes(result)
        setWipeId((current) =>
          current && result.some((wipe) => wipe.id === current)
            ? current
            : (result.find((wipe) => wipe.active)?.id ?? null),
        )
      })
      .catch((error) => {
        if (!cancelled) {
          handleError(error)
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    persistEventFeedState(activeAccount.id, {
      filter,
      rarity,
      itemType,
      boosterType,
      globalType,
      raidLocation,
      query,
      sort,
      wipeId,
    })
  }, [
    activeAccount.id,
    boosterType,
    filter,
    globalType,
    itemType,
    query,
    raidLocation,
    rarity,
    sort,
    wipeId,
  ])

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

  function wipeName(wipe: Wipe): string {
    if (wipe.name === "Legacy") {
      return t("leaderboards.legacyWipe")
    }

    return wipe.name ?? t("leaderboards.wipeFallback")
  }

  function selectFilter(nextFilter: EventFilter) {
    setFilter(nextFilter)
    setRarity("ALL")
    setItemType("ALL")
    setBoosterType("ALL")
    setGlobalType("ALL")
    setRaidLocation(null)
  }

  return (
    <div className="screen events-feed-screen root-screen">
      <header className="root-title-block events-feed-title">
        <span>
          <small>{t("events.feed.eyebrow")}</small>
          <h1>{t("events.feed.title")}</h1>
          <p>{t("events.feed.description")}</p>
        </span>

        <button
          type="button"
          className="events-feed-subscriptions"
          onClick={() => navigate({ id: "subscriptions" })}
        >
          <span className="events-feed-subscriptions__art" aria-hidden="true">
            <img src="/ui-illustrations/notifications-hub.svg" alt="" />
          </span>
          <span className="events-feed-subscriptions__copy">
            <span className="events-feed-subscriptions__label">
              {t("events.subscriptions.title")}
            </span>
            <small>{t("events.subscriptions.entryDescription")}</small>
          </span>
          <b>{subscriptions.length}</b>
        </button>
      </header>

      {selectableWipes.length > 0 ? (
        <section className="events-feed-wipes" aria-label={t("leaderboards.periodAria")}>
          {selectableWipes.map((wipe) => (
            <button
              type="button"
              key={wipe.id}
              className={wipeId === wipe.id ? "is-active" : undefined}
              onClick={() => setWipeId(wipe.id)}
            >
              <small>#{wipe.number}</small>
              <strong>{wipeName(wipe)}</strong>
              {wipe.active ? <i>{t("wipeAdmin.active")}</i> : null}
            </button>
          ))}
        </section>
      ) : null}

      <section
        className="events-feed-filters events-feed-kind-filters"
        aria-label={t("events.feed.filterAria")}
      >
        <button
          type="button"
          className={filter === "ALL" ? "is-active" : ""}
          onClick={() => selectFilter("ALL")}
        >
          <EventKindMicroArt kind="ALL" />
          <span>{t("events.feed.all")}</span>
        </button>

        {filters.map((kind) => (
          <button
            type="button"
            key={kind}
            className={filter === kind ? "is-active" : ""}
            onClick={() => selectFilter(kind)}
          >
            <EventKindMicroArt kind={kind} />
            <span>{eventKindLabel(t, kind)}</span>
          </button>
        ))}
      </section>

      {filter === "ITEM_DROP" ? (
        <section className="events-feed-context-filters">
          <div className="events-feed-context-filters__row">
            <button
              type="button"
              className={itemType === "ALL" ? "is-active" : undefined}
              onClick={() => setItemType("ALL")}
            >
              {t("events.feed.all")}
            </button>
            {BET_ITEM_TYPES.map((value) => (
              <button
                type="button"
                key={value}
                className={itemType === value ? "is-active" : undefined}
                onClick={() => setItemType(value)}
              >
                {t(`events.itemTypes.${value}`)}
              </button>
            ))}
          </div>

          <div className="events-feed-context-filters__row is-compact">
            <button
              type="button"
              className={rarity === "ALL" ? "is-active" : undefined}
              onClick={() => setRarity("ALL")}
            >
              {t("events.feed.allRarities")}
            </button>
            {rarityFilters.map((value) => (
              <button
                type="button"
                key={value}
                className={rarity === value ? "is-active" : undefined}
                onClick={() => setRarity(value)}
              >
                {t(`events.rarities.${value}`)}
              </button>
            ))}
          </div>
        </section>
      ) : null}

      {filter === "BOOSTER" ? (
        <section className="events-feed-context-filters__row">
          <button
            type="button"
            className={boosterType === "ALL" ? "is-active" : undefined}
            onClick={() => setBoosterType("ALL")}
          >
            {t("events.feed.all")}
          </button>
          {BOOSTER_TYPES.map((value) => (
            <button
              type="button"
              key={value}
              className={boosterType === value ? "is-active" : undefined}
              onClick={() => setBoosterType(value)}
            >
              {t(`events.boosters.${value}`)}
            </button>
          ))}
        </section>
      ) : null}

      {filter === "GLOBAL" ? (
        <section className="events-feed-context-filters__row">
          <button
            type="button"
            className={globalType === "ALL" ? "is-active" : undefined}
            onClick={() => setGlobalType("ALL")}
          >
            {t("events.feed.all")}
          </button>
          {GLOBAL_EVENT_TYPES.map((value) => (
            <button
              type="button"
              key={value}
              className={globalType === value ? "is-active" : undefined}
              onClick={() => setGlobalType(value)}
            >
              {t(`events.globals.${value}`)}
            </button>
          ))}
        </section>
      ) : null}

      {filter === "RAID" ? (
        <section
          className="events-feed-location-filter"
          aria-label={t("events.subscriptions.locations")}
        >
          <button
            type="button"
            className={raidLocation === null ? "is-active" : undefined}
            onClick={() => setRaidLocation(null)}
          >
            {t("events.subscriptions.allLocations")}
          </button>
          <div>
            {raidLocations.map((location) => (
              <button
                type="button"
                key={location}
                className={raidLocation === location ? "is-active" : undefined}
                onClick={() => setRaidLocation(location)}
              >
                {location}
              </button>
            ))}
          </div>
        </section>
      ) : null}

      <div className="events-feed-query">
        <label className="events-feed-search">
          <Glyph name="search" size={18} />
          <input
            value={query}
            maxLength={64}
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

      {!loadingEvents && eventsError ? (
        <section className="events-feed-empty events-feed-empty--illustrated">
          <span className="events-feed-empty__illustration" aria-hidden="true">
            <RootNavIllustration route="events" />
          </span>
          <strong>{t("events.feed.errorTitle")}</strong>
          <small>{t("events.feed.errorCopy")}</small>
          <button type="button" onClick={() => void refreshEvents(feedQuery)}>
            {t("events.feed.retry")}
          </button>
        </section>
      ) : null}

      {!eventsError && featured ? (
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

      {!eventsError ? (
        <header className="events-feed-section-head">
          <span>
            <small>{t("events.feed.stream")}</small>
            <strong>{t("events.feed.eventCount", { count: feed.length })}</strong>
          </span>
          <i className={loadingEvents ? "is-loading" : ""} />
        </header>
      ) : null}

      {!eventsError ? (
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

                <span className="events-feed-row__time">
                  {formatOccurredAt(event.occurredAt, locale)}
                </span>
                <span className="events-feed-row__edge" />
              </motion.button>
            ))}
          </AnimatePresence>
        </section>
      ) : null}

      {!eventsError && eventsHasMore ? (
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

      {!loadingEvents && !eventsError && !feed.length ? (
        <section className="events-feed-empty events-feed-empty--illustrated">
          <span className="events-feed-empty__illustration" aria-hidden="true">
            <RootNavIllustration route="events" />
          </span>
          <strong>{t("events.feed.emptyTitle")}</strong>
          <small>{t("events.feed.emptyCopy")}</small>
        </section>
      ) : null}
    </div>
  )
}
