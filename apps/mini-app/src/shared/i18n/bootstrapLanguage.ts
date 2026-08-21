export type BootstrapLanguage = "en" | "ru" | "uk"

export function resolveBootstrapLanguage(
  telegramLanguage = resolveTelegramLanguage(),
  browserLanguage = resolveBrowserLanguage(),
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

function resolveTelegramLanguage(): string | undefined {
  if (typeof window === "undefined") {
    return undefined
  }

  return window.Telegram?.WebApp.initDataUnsafe?.user?.language_code
}

function resolveBrowserLanguage(): string | undefined {
  if (typeof navigator === "undefined") {
    return undefined
  }

  return navigator.language
}
