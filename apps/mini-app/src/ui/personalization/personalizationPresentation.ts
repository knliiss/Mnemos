import { PERSONALIZATION_TYPES } from "../contracts"
import type { PersonalizationTarget, PersonalizationType } from "../model"

export const personalizationTargets: Record<PersonalizationType, PersonalizationTarget> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  PROFILE_BACKGROUND: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}

export const personalizationIllustrations: Record<PersonalizationType, string> = {
  NOTIFICATION_PACK: "/ui-illustrations/creator-notification.svg",
  NICK_STYLE: "/ui-illustrations/creator-nick.svg",
  PROFILE_THEME: "/ui-illustrations/creator-theme.svg",
  PROFILE_BACKGROUND: "/ui-illustrations/creator-background.svg",
  AVATAR_FRAME: "/ui-illustrations/creator-frame.svg",
  PROFILE_EFFECT: "/ui-illustrations/creator-effect.svg",
}

export const accountPersonalizationTypes = PERSONALIZATION_TYPES.filter(
  (type) => personalizationTargets[type] === "ACCOUNT",
)

const profileBackgroundFallback = {
  title: "Фон профиля",
  note: "Живая сцена за содержимым профиля",
  impact: "Добавляет отдельный анимированный фон публичной страницы профиля, не меняя тему карточек.",
}

export function personalizationTypeTitle(
  translate: (key: string) => string,
  type: PersonalizationType,
) {
  return translateWithProfileBackgroundFallback(translate, type, "title")
}

export function personalizationTypeNote(
  translate: (key: string) => string,
  type: PersonalizationType,
) {
  return translateWithProfileBackgroundFallback(translate, type, "note")
}

export function personalizationTypeImpact(
  translate: (key: string) => string,
  type: PersonalizationType,
) {
  return translateWithProfileBackgroundFallback(translate, type, "impact")
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

function translateWithProfileBackgroundFallback(
  translate: (key: string) => string,
  type: PersonalizationType,
  field: keyof typeof profileBackgroundFallback,
) {
  const key = `personalization.types.${type}.${field}`
  const translated = translate(key)

  if (type === "PROFILE_BACKGROUND" && translated === key) {
    return profileBackgroundFallback[field]
  }

  return translated
}
