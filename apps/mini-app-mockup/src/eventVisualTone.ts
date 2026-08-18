import type { FeedEvent, GlobalEventType } from "./model"

export type EventVisualTone =
  | "drop-secret"
  | "drop-mythical"
  | "drop-default"
  | "booster-luck"
  | "booster-money"
  | "booster-damage"
  | "booster-power"
  | "global-moon"
  | "global-eclipse"
  | "global-darkness"
  | "global-explosion"
  | "raid"

export function eventVisualTone(event: FeedEvent): EventVisualTone {
  if (event.kind === "ITEM_DROP") {
    if (event.rarity === "SECRET") return "drop-secret"
    if (event.rarity === "MYTHICAL") return "drop-mythical"
    return "drop-default"
  }

  if (event.kind === "BOOSTER") {
    if (event.title === "MONEY") return "booster-money"
    if (event.title === "DAMAGE") return "booster-damage"
    if (event.title === "POWER") return "booster-power"
    return "booster-luck"
  }

  if (event.kind === "RAID") return "raid"

  return globalEventVisualTone(event.title as GlobalEventType)
}

export function globalEventVisualTone(eventType: GlobalEventType): EventVisualTone {
  if (eventType === "MOON") return "global-moon"
  if (eventType === "DARKNESS") return "global-darkness"
  if (eventType === "EXPLOSION") return "global-explosion"
  return "global-eclipse"
}
