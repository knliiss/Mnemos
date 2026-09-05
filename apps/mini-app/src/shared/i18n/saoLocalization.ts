import { getSaoLocalization } from "@/shared/api/saoLocalizationApi"
import { ApiError } from "@/shared/api/httpClient"
import { i18n } from "@/shared/i18n"
import type { LocalizationLanguage } from "@/ui/model"

const REFRESH_INTERVAL_MS = 300_000
const STYLE_PREFIX_PATTERN = /^¨[0-9a-fA-F]{6}/

const languageMapping: Record<
  LocalizationLanguage,
  {
    i18nLanguage: string
    saoLocale: string
  }
> = {
  EN: {
    i18nLanguage: "en",
    saoLocale: "en_US",
  },
  RU: {
    i18nLanguage: "ru",
    saoLocale: "ru_RU",
  },
  UK: {
    i18nLanguage: "uk",
    saoLocale: "uk_UA",
  },
}

const loadedHashes = new Map<string, string>()

export function saoLanguageConfig(language: LocalizationLanguage) {
  return languageMapping[language]
}

export async function synchronizeSaoLocalization(language: LocalizationLanguage): Promise<boolean> {
  const config = languageMapping[language]

  try {
    const pack = await getSaoLocalization(config.saoLocale)

    if (loadedHashes.get(config.saoLocale) === pack.hash) {
      return false
    }

    i18n.addResourceBundle(
      config.i18nLanguage,
      "translation",
      toNestedResources(pack.properties),
      true,
      true,
    )
    loadedHashes.set(config.saoLocale, pack.hash)

    return true
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return false
    }

    console.warn(`Failed to synchronize SAO localization ${config.saoLocale}`, error)
    return false
  }
}

export function saoLocalizationRefreshIntervalMs(): number {
  return REFRESH_INTERVAL_MS
}

export function toNestedResources(
  properties: Record<string, string>,
): Record<string, unknown> {
  const root: Record<string, unknown> = {}

  for (const [key, value] of Object.entries(properties)) {
    const segments = key.split(".").filter(Boolean)

    if (segments.length === 0) {
      continue
    }

    let cursor = root

    for (const segment of segments.slice(0, -1)) {
      const current = cursor[segment]

      if (!isRecord(current)) {
        cursor[segment] = {}
      }

      cursor = cursor[segment] as Record<string, unknown>
    }

    cursor[segments[segments.length - 1]] = normalizeSaoValue(value)
  }

  return root
}

export function normalizeSaoValue(value: string): string {
  return value.replace(STYLE_PREFIX_PATTERN, "").trim()
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value)
}
