import type { AccountRole } from "../model"

export const NO_BASE_ROLE = "__none__"

export function countEnabledPermissions(role: AccountRole | null) {
  if (!role) {
    return 0
  }

  return Object.values(role.permissions).filter(Boolean).length
}

export function formatJoinedAt(value: string, locale: string) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat(locale, {
    day: "numeric",
    month: "short",
    year: "numeric",
  }).format(date)
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
