import type { BoosterType, FeedEvent, GlobalEventType, Subscription } from "./model"

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
  | "global-comet-chaos"
  | "raid"
  | "dungeon"

function itemDropVisualTone(rarity?: string): EventVisualTone {
  if (rarity === "SECRET") {
    return "drop-secret"
  }
  if (rarity === "MYTHICAL") {
    return "drop-mythical"
  }

  return "drop-default"
}

function boosterVisualTone(boosterType: BoosterType): EventVisualTone {
  if (boosterType === "MONEY") {
    return "booster-money"
  }
  if (boosterType === "DAMAGE") {
    return "booster-damage"
  }
  if (boosterType === "POWER") {
    return "booster-power"
  }

  return "booster-luck"
}

export function globalEventVisualTone(eventType: GlobalEventType): EventVisualTone {
  if (eventType === "MOON") {
    return "global-moon"
  }
  if (eventType === "DARKNESS") {
    return "global-darkness"
  }
  if (eventType === "EXPLOSION") {
    return "global-explosion"
  }
  if (eventType === "COMET_CHAOS") {
    return "global-comet-chaos"
  }

  return "global-eclipse"
}

export function eventVisualTone(event: FeedEvent): EventVisualTone {
  if (event.kind === "ITEM_DROP") {
    return itemDropVisualTone(event.rarity)
  }
  if (event.kind === "BOOSTER") {
    return boosterVisualTone(event.title as BoosterType)
  }
  if (event.kind === "RAID") {
    return "raid"
  }

  return globalEventVisualTone(event.title as GlobalEventType)
}

export function subscriptionVisualTone(subscription: Subscription): EventVisualTone {
  if (subscription.type === "BOOSTER") {
    return boosterVisualTone(subscription.boosterType)
  }
  if (subscription.type === "GLOBAL") {
    return globalEventVisualTone(subscription.eventType)
  }
  if (subscription.type === "RAID") {
    return "raid"
  }
  if (subscription.type === "DUNGEON") {
    return "dungeon"
  }

  return "drop-default"
}
