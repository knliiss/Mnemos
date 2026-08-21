import type { TFunction } from "i18next"
import type { Subscription } from "../../model"

export function normalizeSubscriptionItemName(value: string) {
  return value.trim().toLowerCase()
}

export function subscriptionDescription(t: TFunction, subscription: Subscription) {
  if (subscription.type === "ITEM_DROP") {
    if (subscription.droppedFor && subscription.itemName) {
      return `${subscription.droppedFor} · ${subscription.itemName}`
    }

    if (subscription.droppedFor) {
      return t("events.subscriptions.playerRule", { player: subscription.droppedFor })
    }

    return t("events.subscriptions.itemRule", { item: subscription.itemName })
  }

  if (subscription.type === "BOOSTER") {
    return t(`events.boosters.${subscription.boosterType}`)
  }

  if (subscription.type === "DUNGEON") {
    return t(`events.dungeons.${subscription.dungeonType}`)
  }

  if (subscription.type === "GLOBAL") {
    return t(`events.globals.${subscription.eventType}`)
  }

  return subscription.locations.length
    ? t("events.subscriptions.locationRule", {
        locations: subscription.locations.join(", "),
      })
    : t("events.subscriptions.allLocations")
}
