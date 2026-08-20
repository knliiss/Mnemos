import { PERSONALIZATION_TYPES } from "../contracts"
import type { PersonalizationTarget, PersonalizationType } from "../model"

export const personalizationTargets: Record<PersonalizationType, PersonalizationTarget> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}

export const accountPersonalizationTypes = PERSONALIZATION_TYPES.filter(
  (type) => personalizationTargets[type] === "ACCOUNT",
)

export function personalizationTypeTitle(
  translate: (key: string) => string,
  type: PersonalizationType,
) {
  return translate(`personalization.types.${type}.title`)
}

export function personalizationTypeNote(
  translate: (key: string) => string,
  type: PersonalizationType,
) {
  return translate(`personalization.types.${type}.note`)
}

export function personalizationTypeImpact(
  translate: (key: string) => string,
  type: PersonalizationType,
) {
  return translate(`personalization.types.${type}.impact`)
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

export function readPersonalizationType(value?: string): PersonalizationType | null {
  return PERSONALIZATION_TYPES.find((type) => type === value) ?? null
}
