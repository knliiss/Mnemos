import type { TFunction } from "i18next"
import type { PersonalizationStatus, PersonalizationType } from "../model"

export type StudioFilter = "ALL" | "DRAFT" | "REVIEW" | "PUBLISHED"

export const creatorTypeOrder: PersonalizationType[] = [
  "NICK_STYLE",
  "PROFILE_THEME",
  "PROFILE_BACKGROUND",
  "AVATAR_FRAME",
  "PROFILE_EFFECT",
  "NOTIFICATION_PACK",
]

export const creatorTypeGlyph: Record<
  PersonalizationType,
  "spark" | "creator" | "profile-mark" | "events" | "bell"
> = {
  NICK_STYLE: "spark",
  PROFILE_THEME: "creator",
  PROFILE_BACKGROUND: "creator",
  AVATAR_FRAME: "profile-mark",
  PROFILE_EFFECT: "events",
  NOTIFICATION_PACK: "bell",
}

export function creatorTypeLabel(t: TFunction, type: PersonalizationType) {
  return t(`personalization.types.${type}.title`)
}

export function creatorStatusLabel(t: TFunction, status: PersonalizationStatus) {
  return t(`creator.status.${status}`)
}

export function creatorLocale(language?: string) {
  if (language === "en") {
    return "en-US"
  }

  if (language === "ru") {
    return "ru-RU"
  }

  return "uk-UA"
}

export function formatCreatorOperationDate(value: string, locale: string) {
  return new Intl.DateTimeFormat(locale, {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value))
}
