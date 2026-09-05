import type { TFunction } from "i18next"
import type { Account, EventKind, FeedEvent } from "../model"

export function eventGlyph(kind: EventKind): "item-drop" | "booster" | "global" | "raid" {
  if (kind === "ITEM_DROP") {
    return "item-drop"
  }

  if (kind === "BOOSTER") {
    return "booster"
  }

  if (kind === "GLOBAL") {
    return "global"
  }

  return "raid"
}

export function eventKindLabel(t: TFunction, kind: EventKind) {
  return t(`events.kinds.${kind}`)
}

export function eventTitle(t: TFunction, event: FeedEvent) {
  if (event.kind === "ITEM_DROP" && event.localizationKey) {
    return t(event.localizationKey, { defaultValue: event.title })
  }

  if (event.kind === "GLOBAL") {
    return t(`events.globals.${event.title}`, { defaultValue: event.title })
  }

  if (event.kind === "BOOSTER") {
    return t(`events.boosters.${event.title}`, { defaultValue: event.title })
  }

  if (event.kind === "RAID") {
    return t("events.kinds.RAID")
  }

  return event.title
}

export function eventFeedDescription(t: TFunction, event: FeedEvent) {
  if (event.kind === "ITEM_DROP") {
    return [event.detail, event.rarity].filter(Boolean).join(" · ")
  }

  if (event.kind === "BOOSTER") {
    return event.detail
  }

  if (event.kind === "GLOBAL") {
    return t("events.feed.globalDescription")
  }

  return event.detail
    ? t("events.feed.locations", { locations: event.detail })
    : t("events.feed.raidDescription")
}

export function eventKindCaption(t: TFunction, event: FeedEvent) {
  return t(`events.detail.captions.${event.kind}`)
}

export function eventSubject(t: TFunction, event: FeedEvent) {
  if (event.kind === "GLOBAL") {
    return "Cristalix"
  }

  if (event.kind === "RAID") {
    return t("events.feed.locations", { locations: event.detail })
  }

  return event.detail
}

export function eventDetailDescription(t: TFunction, event: FeedEvent) {
  if (event.kind === "ITEM_DROP") {
    return `${eventTitle(t, event)} · ${rarityLabel(t, event.rarity) ?? eventTypeLabel(t, event)}`
  }

  if (event.kind === "BOOSTER" || event.kind === "GLOBAL") {
    return eventTitle(t, event)
  }

  return t("events.detail.raidActivity")
}

export function eventTypeLabel(t: TFunction, event: FeedEvent) {
  if (event.kind === "ITEM_DROP" && event.meta) {
    return t(`events.itemTypes.${event.meta}`, { defaultValue: event.meta })
  }

  if (event.kind === "BOOSTER") {
    return eventTitle(t, event)
  }

  if (event.kind === "GLOBAL") {
    return t("events.kinds.GLOBAL")
  }

  if (event.kind === "RAID") {
    return t("events.kinds.RAID")
  }

  return eventKindLabel(t, event.kind)
}

export function rarityLabel(t: TFunction, value?: string) {
  if (!value) {
    return undefined
  }

  return t(`events.rarities.${value}`, { defaultValue: value })
}

export function belongsToAccount(event: FeedEvent, account: Account) {
  return (
    (event.kind === "ITEM_DROP" || event.kind === "BOOSTER") &&
    event.detail.toLowerCase() === account.nickname.toLowerCase()
  )
}

export function notificationRuleDescription(t: TFunction, event: FeedEvent) {
  if (event.kind === "ITEM_DROP") {
    return t("events.detail.itemRule", { item: eventTitle(t, event) })
  }

  if (event.kind === "BOOSTER") {
    return t("events.detail.boosterRule", { booster: eventTitle(t, event) })
  }

  if (event.kind === "GLOBAL") {
    return t("events.detail.globalRule", { event: eventTitle(t, event) })
  }

  return t("events.detail.raidRule", { locations: event.detail })
}

export function parseEventLocations(value: string) {
  return value
    .split(/[·,\s]+/)
    .map((entry) => Number(entry.trim()))
    .filter((entry) => Number.isInteger(entry) && entry > 0)
}

export function localeForLanguage(language?: string) {
  if (language === "en") {
    return "en-US"
  }

  if (language === "ru") {
    return "ru-RU"
  }

  return "uk-UA"
}
