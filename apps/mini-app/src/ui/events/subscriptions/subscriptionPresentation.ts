import type { TFunction } from "i18next"
import type { Subscription } from "../../model"

export type SubscriptionSort = "NEWEST" | "OLDEST" | "TYPE" | "NAME"

const subscriptionTypeRank: Record<Subscription["type"], number> = {
  ITEM_DROP: 0,
  BOOSTER: 1,
  DUNGEON: 2,
  RAID: 3,
  GLOBAL: 4,
}

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

export function sortSubscriptions(
  subscriptions: Subscription[],
  sort: SubscriptionSort,
  t: TFunction,
): Subscription[] {
  return subscriptions.toSorted((left, right) => compareSubscriptions(left, right, sort, t))
}

function compareSubscriptions(
  left: Subscription,
  right: Subscription,
  sort: SubscriptionSort,
  t: TFunction,
): number {
  if (sort === "OLDEST") {
    return Date.parse(left.createdAt) - Date.parse(right.createdAt)
  }

  if (sort === "TYPE") {
    const typeDifference = subscriptionTypeRank[left.type] - subscriptionTypeRank[right.type]

    if (typeDifference !== 0) {
      return typeDifference
    }
  }

  if (sort === "NAME" || sort === "TYPE") {
    return subscriptionDescription(t, left).localeCompare(subscriptionDescription(t, right))
  }

  return Date.parse(right.createdAt) - Date.parse(left.createdAt)
}
