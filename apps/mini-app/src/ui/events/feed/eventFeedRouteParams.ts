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

export type EventFeedRouteChoice = {
  id: string
  type: "PLAYER" | "ITEM" | "BOOSTER" | "GLOBAL" | "RAID_LOCATION"
  value: string
  params: Record<string, string>
  location?: number
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

function raidLocations(event: FeedEvent): number[] {
  if (event.kind !== "RAID") {
    return []
  }

  return Array.from(
    new Set(
      event.detail
        .split("·")
        .map((value) => Number(value.trim()))
        .filter((value) => Number.isInteger(value) && value > 0),
    ),
  )
}

export function eventToFeedRouteChoices(event: FeedEvent): EventFeedRouteChoice[] {
  const choices: EventFeedRouteChoice[] = []

  if (event.kind === "ITEM_DROP" || event.kind === "BOOSTER") {
    const player = event.detail.trim()

    if (player) {
      choices.push({
        id: `player:${player.toLocaleLowerCase()}`,
        type: "PLAYER",
        value: player,
        params: { query: player },
      })
    }
  }

  if (event.kind === "ITEM_DROP") {
    const item = event.title.trim()

    if (item) {
      choices.push({
        id: `item:${item.toLocaleLowerCase()}`,
        type: "ITEM",
        value: item,
        params: { filter: "ITEM_DROP", query: item },
      })
    }
  } else if (event.kind === "BOOSTER" && boosterTypes.has(event.title as BoosterType)) {
    choices.push({
      id: `booster:${event.title}`,
      type: "BOOSTER",
      value: event.title,
      params: { filter: "BOOSTER", boosterType: event.title },
    })
  } else if (event.kind === "GLOBAL" && globalTypes.has(event.title as GlobalEventType)) {
    choices.push({
      id: `global:${event.title}`,
      type: "GLOBAL",
      value: event.title,
      params: { filter: "GLOBAL", globalType: event.title },
    })
  } else if (event.kind === "RAID") {
    raidLocations(event).forEach((location) => {
      choices.push({
        id: `raid-location:${location}`,
        type: "RAID_LOCATION",
        value: String(location),
        location,
        params: { filter: "RAID", raidLocation: String(location) },
      })
    })
  }

  return choices
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
