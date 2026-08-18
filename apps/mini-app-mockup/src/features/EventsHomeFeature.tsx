import { AnimatePresence, motion } from "framer-motion"
import { useMemo, useState } from "react"
import { EventArt, OwnerCrown } from "../components/EventArt"
import { BOOSTER_TYPES, EVENT_KINDS, GLOBAL_EVENT_TYPES } from "../contracts"
import { eventVisualTone } from "../eventVisualTone"
import { feed, initialSubscriptions } from "../mockData"
import type {
  Account,
  BetItemType,
  BoosterType,
  EventKind,
  FeedEvent,
  GlobalEventType,
  Route,
} from "../model"
import {
  boosterLabels,
  eventLabels,
  globalEventLabels,
  itemTypeLabels,
} from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import {
  BottomSheet,
  ChoiceGrid,
  EmptyState,
  Field,
  PageHeader,
} from "../shared/Primitives"
import { formatOccurredAt } from "../timePresentation"

type Nav = (route: Route) => void

type EventSortOrder = "NEWEST" | "OLDEST"
type ItemRarityFilter = "ALL" | "MYTHICAL" | "SECRET"
type BoosterTypeFilter = "ALL" | BoosterType
type GlobalTypeFilter = "ALL" | GlobalEventType

const icons: Record<EventKind, "item-drop" | "booster" | "global" | "raid"> = {
  ITEM_DROP: "item-drop",
  BOOSTER: "booster",
  GLOBAL: "global",
  RAID: "raid",
}

const rarityFilterLabels: Record<ItemRarityFilter, string> = {
  ALL: "Усі",
  MYTHICAL: "Міфічні",
  SECRET: "Секретні",
}

const boosterTypeFilterLabels: Record<BoosterTypeFilter, string> = {
  ALL: "Усі",
  ...boosterLabels,
}

const globalTypeFilterLabels: Record<GlobalTypeFilter, string> = {
  ALL: "Усі",
  ...globalEventLabels,
}

const boosterTypeFilterOptions = ["ALL", ...BOOSTER_TYPES] as const satisfies readonly BoosterTypeFilter[]
const globalTypeFilterOptions = ["ALL", ...GLOBAL_EVENT_TYPES] as const satisfies readonly GlobalTypeFilter[]
const rarityFilterOptions = ["ALL", "MYTHICAL", "SECRET"] as const satisfies readonly ItemRarityFilter[]

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

function rarityLabel(value?: string) {
  if (value === "MYTHICAL") return "Міфічний"
  if (value === "SECRET") return "Секретний"
  return value
}

function raidLocations(event: FeedEvent) {
  return event.detail
    .split("·")
    .map((value) => Number(value.trim()))
    .filter((value) => Number.isInteger(value) && value > 0)
}

function raidLocationCountLabel(event: FeedEvent) {
  const count = raidLocations(event).length

  if (count === 1) return "1 локація"
  if (count >= 2 && count <= 4) return `${count} локації`

  return `${count} локацій`
}

function eventMetaLabel(event: FeedEvent) {
  if (!event.meta) return null

  if (event.kind === "ITEM_DROP" && event.meta in itemTypeLabels) {
    return itemTypeLabels[event.meta as BetItemType]
  }

  if (event.kind === "BOOSTER" && event.title in boosterLabels) {
    return boosterLabels[event.title as keyof typeof boosterLabels]
  }

  if (event.kind === "RAID") return raidLocationCountLabel(event)

  return event.meta
}

function eventSubject(event: FeedEvent) {
  if (event.kind === "GLOBAL") return "Cristalix"
  if (event.kind === "RAID") return `Локації ${event.detail}`
  return event.detail
}

function eventFeedDescription(event: FeedEvent) {
  if (event.kind === "ITEM_DROP") {
    return `${event.detail}${event.rarity ? ` · ${rarityLabel(event.rarity)}` : ""}`
  }

  if (event.kind === "GLOBAL") return "Глобальна подія"
  if (event.kind === "RAID") return `Локації ${event.detail}`

  return event.detail
}

function eventDetailDescription(event: FeedEvent) {
  if (event.kind === "GLOBAL") return "Глобальна подія"
  if (event.kind === "RAID") return "Рейд Cristalix"

  return rarityLabel(event.rarity) ?? eventMetaLabel(event) ?? eventLabels[event.kind]
}

function belongsToAccount(event: FeedEvent, account: Account) {
  return (event.kind === "ITEM_DROP" || event.kind === "BOOSTER")
    && event.detail.toLowerCase() === account.nickname.toLowerCase()
}

function eventSearchText(event: FeedEvent) {
  return [
    eventTitle(event),
    event.title,
    event.detail,
    event.meta,
    event.rarity,
    eventMetaLabel(event),
    eventFeedDescription(event),
    eventLabels[event.kind],
  ]
    .filter(Boolean)
    .join(" ")
    .toLowerCase()
}

function searchPlaceholder(filter: "ALL" | EventKind) {
  if (filter === "ITEM_DROP") return "Предмет або нік"
  if (filter === "BOOSTER") return "Бустер або нік активатора"
  if (filter === "GLOBAL") return "Назва глобальної події"
  if (filter === "RAID") return "Номер локації"

  return "Предмет, нік або подія"
}

export function EventsHome({ navigate, activeAccount }: { navigate: Nav; activeAccount: Account }) {
  const [filter, setFilter] = useState<"ALL" | EventKind>("ALL")
  const [query, setQuery] = useState("")
  const [sortOrder, setSortOrder] = useState<EventSortOrder>("NEWEST")
  const [filtersOpen, setFiltersOpen] = useState(false)

  const [dropPlayer, setDropPlayer] = useState("")
  const [dropItem, setDropItem] = useState("")
  const [dropRarity, setDropRarity] = useState<ItemRarityFilter>("ALL")

  const [boosterType, setBoosterType] = useState<BoosterTypeFilter>("ALL")
  const [boosterActivator, setBoosterActivator] = useState("")

  const [globalType, setGlobalType] = useState<GlobalTypeFilter>("ALL")

  const [raidMinLocation, setRaidMinLocation] = useState("")
  const [raidMaxLocation, setRaidMaxLocation] = useState("")

  const visible = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase()
    const normalizedDropPlayer = dropPlayer.trim().toLowerCase()
    const normalizedDropItem = dropItem.trim().toLowerCase()
    const normalizedBoosterActivator = boosterActivator.trim().toLowerCase()
    const minLocation = raidMinLocation ? Number(raidMinLocation) : null
    const maxLocation = raidMaxLocation ? Number(raidMaxLocation) : null

    return feed
      .filter((event) => filter === "ALL" || event.kind === filter)
      .filter((event) => !normalizedQuery || eventSearchText(event).includes(normalizedQuery))
      .filter((event) => {
        if (filter !== "ITEM_DROP" || event.kind !== "ITEM_DROP") return true

        const playerMatches = !normalizedDropPlayer || event.detail.toLowerCase().includes(normalizedDropPlayer)
        const itemMatches = !normalizedDropItem || event.title.toLowerCase().includes(normalizedDropItem)
        const rarityMatches = dropRarity === "ALL" || event.rarity === dropRarity

        return playerMatches && itemMatches && rarityMatches
      })
      .filter((event) => {
        if (filter !== "BOOSTER" || event.kind !== "BOOSTER") return true

        const typeMatches = boosterType === "ALL" || event.title === boosterType
        const activatorMatches = !normalizedBoosterActivator
          || event.detail.toLowerCase().includes(normalizedBoosterActivator)

        return typeMatches && activatorMatches
      })
      .filter((event) => {
        if (filter !== "GLOBAL" || event.kind !== "GLOBAL") return true

        return globalType === "ALL" || event.title === globalType
      })
      .filter((event) => {
        if (filter !== "RAID" || event.kind !== "RAID") return true
        if (minLocation === null && maxLocation === null) return true

        return raidLocations(event).some((location) => (
          (minLocation === null || location >= minLocation)
          && (maxLocation === null || location <= maxLocation)
        ))
      })
      .sort((left, right) => {
        const delta = new Date(left.occurredAt).getTime() - new Date(right.occurredAt).getTime()

        return sortOrder === "NEWEST" ? -delta : delta
      })
  }, [
    boosterActivator,
    boosterType,
    dropItem,
    dropPlayer,
    dropRarity,
    filter,
    globalType,
    query,
    raidMaxLocation,
    raidMinLocation,
    sortOrder,
  ])

  const activeAdvancedFilters = useMemo(() => {
    if (filter === "ITEM_DROP") {
      return Number(Boolean(dropPlayer.trim()))
        + Number(Boolean(dropItem.trim()))
        + Number(dropRarity !== "ALL")
    }

    if (filter === "BOOSTER") {
      return Number(boosterType !== "ALL") + Number(Boolean(boosterActivator.trim()))
    }

    if (filter === "GLOBAL") {
      return Number(globalType !== "ALL")
    }

    if (filter === "RAID") {
      return Number(Boolean(raidMinLocation)) + Number(Boolean(raidMaxLocation))
    }

    return 0
  }, [
    boosterActivator,
    boosterType,
    dropItem,
    dropPlayer,
    dropRarity,
    filter,
    globalType,
    raidMaxLocation,
    raidMinLocation,
  ])

  const resetAdvancedFilters = () => {
    setDropPlayer("")
    setDropItem("")
    setDropRarity("ALL")
    setBoosterType("ALL")
    setBoosterActivator("")
    setGlobalType("ALL")
    setRaidMinLocation("")
    setRaidMaxLocation("")
  }

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

      <div className="event-browser-tools">
        <label className="event-browser-search">
          <Glyph name="search" size={17} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder={searchPlaceholder(filter)}
            aria-label="Пошук подій"
          />
          {query ? (
            <button type="button" onClick={() => setQuery("")} aria-label="Очистити пошук">
              <Glyph name="close" size={14} />
            </button>
          ) : null}
        </label>

        <div className="event-browser-actions">
          <button
            className="event-browser-action"
            type="button"
            onClick={() => setSortOrder((value) => value === "NEWEST" ? "OLDEST" : "NEWEST")}
            aria-label={sortOrder === "NEWEST" ? "Спочатку новіші" : "Спочатку старіші"}
          >
            <Glyph name="swap" size={16} />
            <span>{sortOrder === "NEWEST" ? "Нові" : "Старі"}</span>
          </button>

          <button
            className={`event-browser-action event-browser-action--filters ${activeAdvancedFilters ? "is-active" : ""}`}
            type="button"
            onClick={() => setFiltersOpen(true)}
            aria-label="Фільтри подій"
          >
            <Glyph name="settings" size={16} />
            {activeAdvancedFilters ? <b>{activeAdvancedFilters}</b> : null}
          </button>
        </div>
      </div>

      <div className="event-browser-summary">
        <span>{visible.length} {visible.length === 1 ? "подія" : "подій"}</span>
        {query ? <small>за запитом «{query.trim()}»</small> : null}
      </div>

      <div className="event-stream-redesign">
        {visible.length ? visible.map((event, index) => {
          const owned = belongsToAccount(event, activeAccount)
          const tone = eventVisualTone(event)

          return (
            <motion.button
              whileTap={{ scale: 0.992 }}
              className={`event-feed-card event-surface event-surface--${tone} ${index === 0 && sortOrder === "NEWEST" ? "is-latest" : ""}`}
              key={event.id}
              onClick={() => open(event)}
            >
              <EventArt event={event} size={48} />
              <span className="event-feed-card__copy">
                <span className="event-feed-card__meta">
                  <span className="event-feed-card__kind">
                    <b>{eventLabels[event.kind]}</b>
                    {owned ? <OwnerCrown compact /> : null}
                  </span>
                  <time>{formatOccurredAt(event.occurredAt)}</time>
                </span>
                <strong>{eventTitle(event)}</strong>
                <small>{eventFeedDescription(event)}</small>
              </span>
              <span className="event-feed-card__chevron">›</span>
            </motion.button>
          )
        }) : (
          <EmptyState
            title="Подій не знайдено"
            note="Змініть пошук або фільтри"
          />
        )}
      </div>

      <AnimatePresence>
        {filtersOpen ? (
          <BottomSheet title="Фільтри подій" onClose={() => setFiltersOpen(false)}>
            <div className="form-stack event-filter-sheet">
              {filter === "ALL" ? (
                <div className="event-filter-sheet__hint">
                  <Glyph name="settings" size={18} />
                  <span>
                    <strong>Специфічні фільтри</strong>
                    <small>Оберіть тип події зверху. Пошук уже працює по предметах, ніках і назвах подій.</small>
                  </span>
                </div>
              ) : null}

              {filter === "ITEM_DROP" ? (
                <>
                  <Field label="Гравець">
                    <input
                      value={dropPlayer}
                      onChange={(event) => setDropPlayer(event.target.value)}
                      placeholder="nickname"
                    />
                  </Field>
                  <Field label="Предмет">
                    <input
                      value={dropItem}
                      onChange={(event) => setDropItem(event.target.value)}
                      placeholder="Eternal Blade"
                    />
                  </Field>
                  <Field label="Рідкість">
                    <ChoiceGrid
                      value={dropRarity}
                      options={rarityFilterOptions}
                      onChange={setDropRarity}
                      labels={rarityFilterLabels}
                    />
                  </Field>
                </>
              ) : null}

              {filter === "BOOSTER" ? (
                <>
                  <Field label="Тип бустера">
                    <ChoiceGrid
                      value={boosterType}
                      options={boosterTypeFilterOptions}
                      onChange={setBoosterType}
                      labels={boosterTypeFilterLabels}
                    />
                  </Field>
                  <Field label="Активатор">
                    <input
                      value={boosterActivator}
                      onChange={(event) => setBoosterActivator(event.target.value)}
                      placeholder="nickname"
                    />
                  </Field>
                </>
              ) : null}

              {filter === "GLOBAL" ? (
                <Field label="Тип глобальної події">
                  <ChoiceGrid
                    value={globalType}
                    options={globalTypeFilterOptions}
                    onChange={setGlobalType}
                    labels={globalTypeFilterLabels}
                  />
                </Field>
              ) : null}

              {filter === "RAID" ? (
                <Field label="Діапазон локацій" hint="Так само, як minLocation / maxLocation на event-service">
                  <div className="event-filter-sheet__range">
                    <input
                      type="number"
                      min="1"
                      max="30"
                      value={raidMinLocation}
                      onChange={(event) => setRaidMinLocation(event.target.value)}
                      placeholder="Від"
                    />
                    <span>—</span>
                    <input
                      type="number"
                      min="1"
                      max="30"
                      value={raidMaxLocation}
                      onChange={(event) => setRaidMaxLocation(event.target.value)}
                      placeholder="До"
                    />
                  </div>
                </Field>
              ) : null}

              <div className="event-filter-sheet__actions">
                <button type="button" className="secondary-action" onClick={resetAdvancedFilters}>
                  Скинути
                </button>
                <button type="button" className="primary-action" onClick={() => setFiltersOpen(false)}>
                  Показати {visible.length}
                </button>
              </div>
            </div>
          </BottomSheet>
        ) : null}
      </AnimatePresence>
    </div>
  )
}

export function EventDetail({ eventId, activeAccount, back }: { eventId?: string; activeAccount: Account; back: () => void }) {
  const event = feed.find((item) => item.id === eventId) ?? feed[0]
  const ownEvent = belongsToAccount(event, activeAccount)
  const ownDrop = event.kind === "ITEM_DROP" && ownEvent
  const tone = eventVisualTone(event)
  const [spotlight, setSpotlight] = useState<"READY" | "PENDING" | "SCHEDULED">("READY")

  const publish = () => {
    if (spotlight !== "READY") return

    setSpotlight("PENDING")
    window.setTimeout(() => setSpotlight("SCHEDULED"), 900)
  }

  return (
    <div className="screen event-detail-screen event-detail-redesign event-detail-polished">
      <PageHeader title={eventTitle(event)} eyebrow={eventLabels[event.kind]} back={back} />

      <section className={`event-detail-hero event-surface event-surface--${tone}`}>
        <EventArt event={event} size={62} />
        <div>
          <span className="event-detail-hero__identity">
            <strong>{eventSubject(event)}</strong>
            {ownEvent ? <OwnerCrown /> : null}
          </span>
          <small>{eventDetailDescription(event)}</small>
        </div>
        <time>{formatOccurredAt(event.occurredAt)}</time>
      </section>

      <dl className="event-facts event-facts--polished">
        <div className="event-fact-wide"><dt>Сталося</dt><dd>{new Intl.DateTimeFormat("uk-UA", { dateStyle: "medium", timeStyle: "short" }).format(new Date(event.occurredAt))}</dd></div>
        {event.meta ? <div><dt>Тип</dt><dd>{eventMetaLabel(event)}</dd></div> : null}
        {event.rarity ? <div><dt>Рідкість</dt><dd>{rarityLabel(event.rarity)}</dd></div> : null}
      </dl>

      {ownDrop ? (
        <section className="spotlight-action spotlight-action--redesign spotlight-action--polished">
          <span className="spotlight-action__mark"><Glyph name="spark" size={20} /></span>
          <span className="spotlight-action__copy">
            <strong>Алерт дропа</strong>
            <small>Spotlight цього дропа для користувачів Mini App</small>
          </span>
          {spotlight === "READY" ? (
            <motion.button whileTap={{ scale: 0.96 }} className="spotlight-buy" onClick={publish} aria-label="Купити алерт за 25 PRIME">
              <CurrencyMark currency="PRIME" size={14} />
              <strong>25</strong>
            </motion.button>
          ) : (
            <span className={`spotlight-state spotlight-state--${spotlight.toLowerCase()}`}>
              {spotlight === "PENDING" ? "Обробка" : "У черзі"}
            </span>
          )}
        </section>
      ) : null}
    </div>
  )
}
