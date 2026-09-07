import { getSaoLocalization, listSaoLocalizations } from "@/shared/api/saoLocalizationApi"
import { useEffect, useMemo, useState } from "react"
import { GUIDE_NPC_ROLE_KEYS, type GuideLocation } from "./guideCatalog"
import { GUIDE_CRITICAL_CATALOGS } from "./guideCriticalCatalogs"
import { GUIDE_EN_PROGRESSION_FALLBACK } from "./guideEnglishProgressionFallback"
import { GUIDE_EN_FALLBACK } from "./guideEnglishFallback"
import { GUIDE_INTERFACE_CATALOGS } from "./guideInterfaceCatalogs"

export type SaoCatalogs = Record<string, Record<string, string>>

const BUNDLED_CATALOGS = mergeCatalogs(
  mergeCatalogs(
    {
      en_US: {
        ...GUIDE_EN_FALLBACK,
        ...GUIDE_EN_PROGRESSION_FALLBACK,
      },
    },
    GUIDE_CRITICAL_CATALOGS,
  ),
  GUIDE_INTERFACE_CATALOGS,
)

let remoteCatalogsPromise: Promise<SaoCatalogs> | null = null

export function useSaoGuideLocalization(uiLanguage: string | undefined) {
  const [catalogs, setCatalogs] = useState<SaoCatalogs>(BUNDLED_CATALOGS)

  useEffect(() => {
    let active = true

    void loadRemoteSaoCatalogs().then((remoteCatalogs) => {
      if (!active || !Object.keys(remoteCatalogs).length) {
        return
      }

      setCatalogs((currentCatalogs) => mergeCatalogs(currentCatalogs, remoteCatalogs))
    })

    return () => {
      active = false
    }
  }, [])

  const preferredLanguage = saoLanguageForUi(uiLanguage)

  return useMemo(
    () => ({
      catalogs,
      preferredLanguage,
      text: (key: string) => resolveSaoText(key, preferredLanguage, catalogs),
    }),
    [catalogs, preferredLanguage],
  )
}

export function saoLanguageForUi(uiLanguage: string | undefined) {
  if (uiLanguage?.toLowerCase().startsWith("uk")) {
    return "uk_UA"
  }

  if (uiLanguage?.toLowerCase().startsWith("ru")) {
    return "ru_RU"
  }

  return "en_US"
}

export function resolveSaoText(key: string, preferredLanguage: string, catalogs: SaoCatalogs) {
  const fallbackChain = unique([preferredLanguage, "ru_RU", "en_US"])

  for (const language of fallbackChain) {
    const value = cleanGameFormatting(catalogs[language]?.[key])

    if (value) {
      return value
    }
  }

  for (const language of fallbackChain) {
    const value = cleanGameFormatting(BUNDLED_CATALOGS[language]?.[key])

    if (value) {
      return value
    }
  }

  return key
}

export function localizationKeyExists(key: string, catalogs: SaoCatalogs) {
  return Object.values(catalogs).some((catalog) => Boolean(cleanGameFormatting(catalog[key])))
}

export function localizationKeyMatchesQuery(
  key: string,
  query: string,
  catalogs: SaoCatalogs,
  aliases: readonly string[] = [],
) {
  const normalizedQuery = normalizeGuideSearchText(query)

  if (!normalizedQuery) {
    return true
  }

  const values = new Set(aliases)

  for (const catalog of Object.values(catalogs)) {
    const value = catalog[key]

    if (value) {
      values.add(value)
    }
  }

  for (const catalog of Object.values(BUNDLED_CATALOGS)) {
    const value = catalog[key]

    if (value) {
      values.add(value)
    }
  }

  return [...values].some((value) => normalizeGuideSearchText(value).includes(normalizedQuery))
}

export function locationMatchesQuery(
  location: GuideLocation,
  query: string,
  catalogs: SaoCatalogs,
) {
  const normalizedQuery = normalizeGuideSearchText(query)

  if (!normalizedQuery) {
    return true
  }

  const confirmedEntityKeys = [
    ...location.npcs.flatMap((npc) => [npc.nameKey]),
    ...location.mobKeys,
    ...location.cometMobKeys,
    ...location.weaponKeys,
    ...location.petKeys,
    ...location.donatePetKeys,
    ...location.materialKeys,
  ].filter((key) => localizationKeyExists(key, catalogs))
  const searchableKeys = [
    location.nameKey,
    location.descriptionKey,
    ...location.npcs.flatMap((npc) => [
      GUIDE_NPC_ROLE_KEYS[npc.role].role,
      GUIDE_NPC_ROLE_KEYS[npc.role].description,
    ]),
    ...confirmedEntityKeys,
  ]
  const aliases = [
    String(location.id),
    `location ${location.id}`,
    `локация ${location.id}`,
    `локація ${location.id}`,
  ]

  return (
    searchableKeys.some((key) => localizationKeyMatchesQuery(key, query, catalogs)) ||
    aliases.some((value) => normalizeGuideSearchText(value).includes(normalizedQuery))
  )
}

export function normalizeGuideSearchText(value: string) {
  return cleanGameFormatting(value)
    .normalize("NFKD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLocaleLowerCase()
    .replace(/ı/g, "i")
    .replace(/ß/g, "ss")
    .replace(/æ/g, "ae")
    .replace(/œ/g, "oe")
    .replace(/ø/g, "o")
    .replace(/ł/g, "l")
    .replace(/đ/g, "d")
    .replace(/[^\p{L}\p{N}]+/gu, " ")
    .trim()
    .replace(/\s+/g, " ")
}

export function cleanGameFormatting(value: string | undefined) {
  return (value ?? "")
    .replace(/¨[0-9a-fA-F]{6}/g, "")
    .replace(/§./g, "")
    .trim()
}

async function loadRemoteSaoCatalogs() {
  if (!remoteCatalogsPromise) {
    remoteCatalogsPromise = fetchRemoteSaoCatalogs().catch(() => ({}))
  }

  return remoteCatalogsPromise
}

async function fetchRemoteSaoCatalogs(): Promise<SaoCatalogs> {
  const summaries = await listSaoLocalizations()
  const results = await Promise.allSettled(
    summaries.map(async ({ locale }) => {
      const pack = await getSaoLocalization(locale)
      return [locale, pack.properties] as const
    }),
  )

  return Object.fromEntries(
    results.flatMap((result) => (result.status === "fulfilled" ? [result.value] : [])),
  )
}

function mergeCatalogs(base: SaoCatalogs, overrides: SaoCatalogs): SaoCatalogs {
  const merged: SaoCatalogs = { ...base }

  for (const [language, catalog] of Object.entries(overrides)) {
    merged[language] = {
      ...(merged[language] ?? {}),
      ...catalog,
    }
  }

  return merged
}

function unique(values: string[]) {
  return [...new Set(values)]
}
