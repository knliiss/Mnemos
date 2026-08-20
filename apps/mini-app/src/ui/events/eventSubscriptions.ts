import {
  createBoosterSubscription,
  createGlobalSubscription,
  createItemDropSubscription,
  createRaidSubscription,
} from "@/shared/api/subscriptionApi"
import type { BoosterType, FeedEvent, GlobalEventType, Subscription } from "../model"
import { parseEventLocations } from "./eventPresentation"

export async function createSubscriptionForEvent(event: FeedEvent): Promise<void> {
  if (event.kind === "ITEM_DROP") {
    await createItemDropSubscription(undefined, event.title)
    return
  }

  if (event.kind === "BOOSTER") {
    await createBoosterSubscription(event.title as BoosterType)
    return
  }

  if (event.kind === "GLOBAL") {
    await createGlobalSubscription(event.title as GlobalEventType)
    return
  }

  await createRaidSubscription(parseEventLocations(event.detail))
}

export function subscriptionMatchesEvent(subscription: Subscription, event: FeedEvent) {
  if (event.kind === "ITEM_DROP" && subscription.type === "ITEM_DROP") {
    const itemMatches =
      !subscription.itemName || subscription.itemName.toLowerCase() === event.title.toLowerCase()
    const playerMatches =
      !subscription.droppedFor ||
      subscription.droppedFor.toLowerCase() === event.detail.toLowerCase()

    return itemMatches && playerMatches
  }

  if (event.kind === "BOOSTER" && subscription.type === "BOOSTER") {
    return subscription.boosterType === event.title
  }

  if (event.kind === "GLOBAL" && subscription.type === "GLOBAL") {
    return subscription.eventType === event.title
  }

  if (event.kind === "RAID" && subscription.type === "RAID") {
    if (!subscription.locations.length) {
      return true
    }

    const locations = parseEventLocations(event.detail)

    return subscription.locations.some((location) => locations.includes(location))
  }

  return false
}
