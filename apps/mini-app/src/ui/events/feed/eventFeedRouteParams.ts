import type { EventItemType } from "@/shared/api/eventApi"
import type { BoosterType, EventKind, FeedEvent, GlobalEventType, ItemRarity } from "../../model"

export type EventFeedInitialFilters = {
  filter: "ALL" | EventKind
  rarity: "ALL" | ItemRarity
  itemType: "ALL" | EventItemType
  boosterType: "ALL" | BoosterType
  globalType: "ALL" | GlobalEventType
  raidLocation: number | null
  query: string
}

const eventKinds = new Set<EventKind>(["ITEM_DROP", "BOOSTER", "GLOBAL", "RAID"])
const itemTypes = new Set<EventItemType>(["SWORD", "AURA", "PET", "BOOK", "JEWELRY"])
const rarities = new Set<ItemRarity>(["MYTHICAL", "SECRET"])
const boosterTypes = new Set<BoosterType>(["LUCK", "MONEY", "DAMAGE", "POWER"])
const globalTypes = new Set<GlobalEventType>([
  "DARKNESS",
  "MOON",
  "ECLIPSE",
  "EXPLOSION",
  "COMET_CHAOS",
])

export const emptyEventFeedFilters: EventFeedInitialFilters = {
  filter: "ALL",
  rarity: "ALL",
  itemType: "ALL",
  boosterType: "ALL",
  globalType: "ALL",
  raidLocation: null,
  query: "",
}

function oneRaidLocation(event: FeedEvent): number | null {
  if (event.kind !== "RAID") {
    return null
  }

  const locations = event.detail
    .split("·")
    .map((value) => Number(value.trim()))
    .filter((value) => Number.isInteger(value) && value > 0)

  return locations.length === 1 ? locations[0] : null
}

export function eventToFeedRouteParams(event: FeedEvent): Record<string, string> {
  const params: Record<string, string> = { filter: event.kind }

  if (event.kind === "ITEM_DROP") {
    if (event.meta && itemTypes.has(event.meta as EventItemType)) {
      params.itemType = event.meta
    }

    if (event.rarity) {
      params.rarity = event.rarity
    }
  } else if (event.kind === "BOOSTER" && boosterTypes.has(event.title as BoosterType)) {
    params.boosterType = event.title
  } else if (event.kind === "GLOBAL" && globalTypes.has(event.title as GlobalEventType)) {
    params.globalType = event.title
  } else if (event.kind === "RAID") {
    const location = oneRaidLocation(event)

    if (location !== null) {
      params.raidLocation = String(location)
    }
  }

  return params
}

export function readEventFeedRouteParams(params?: Record<string, string>): EventFeedInitialFilters {
  if (!params) {
    return emptyEventFeedFilters
  }

  const filter = eventKinds.has(params.filter as EventKind) ? (params.filter as EventKind) : "ALL"

  const next: EventFeedInitialFilters = {
    ...emptyEventFeedFilters,
    filter,
    query: params.query?.slice(0, 64) ?? "",
  }

  if (filter === "ITEM_DROP") {
    next.rarity = rarities.has(params.rarity as ItemRarity) ? (params.rarity as ItemRarity) : "ALL"
    next.itemType = itemTypes.has(params.itemType as EventItemType)
      ? (params.itemType as EventItemType)
      : "ALL"
  } else if (filter === "BOOSTER") {
    next.boosterType = boosterTypes.has(params.boosterType as BoosterType)
      ? (params.boosterType as BoosterType)
      : "ALL"
  } else if (filter === "GLOBAL") {
    next.globalType = globalTypes.has(params.globalType as GlobalEventType)
      ? (params.globalType as GlobalEventType)
      : "ALL"
  } else if (filter === "RAID") {
    const raidLocation = Number(params.raidLocation)
    next.raidLocation = Number.isInteger(raidLocation) && raidLocation > 0 ? raidLocation : null
  }

  return next
}
