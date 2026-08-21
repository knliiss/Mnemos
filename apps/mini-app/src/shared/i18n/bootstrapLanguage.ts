export type BootstrapLanguage = "en" | "ru" | "uk"

export function resolveBootstrapLanguage(
  telegramLanguage = window.Telegram?.WebApp.initDataUnsafe?.user?.language_code,
  browserLanguage = typeof navigator !== "undefined" ? navigator.language : undefined,
): BootstrapLanguage {
  return normalizeLanguage(telegramLanguage) ?? normalizeLanguage(browserLanguage) ?? "ru"
}

export function normalizeLanguage(language?: string): BootstrapLanguage | null {
  if (!language) {
    return null
  }

  const normalized = language.trim().toLowerCase().split(/[-_]/)[0]

  if (normalized === "ru") {
    return "ru"
  }

  if (normalized === "uk" || normalized === "ua") {
    return "uk"
  }

  if (normalized === "en") {
    return "en"
  }

  return null
}
