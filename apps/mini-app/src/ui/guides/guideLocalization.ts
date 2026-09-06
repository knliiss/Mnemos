import { useEffect, useMemo, useState } from "react"
import { GUIDE_NPC_ROLE_KEYS, type GuideLocation } from "./guideCatalog"
import { GUIDE_EN_FALLBACK } from "./guideEnglishFallback"

export type SaoCatalogs = Record<string, Record<string, string>>

type LocalizationIndex = {
  langs?: Array<{
    lang?: string
    pies?: Array<{
      pieKey?: string
      url?: string
    }>
  }>
}

type LocalizationPie = {
  properties?: Record<string, string>
}

const LANGUAGE_INDEX_URL = "https://webdata.c7x.dev/client/lang.json"
const BUNDLED_CATALOGS: SaoCatalogs = { en_US: GUIDE_EN_FALLBACK }

let remoteCatalogsPromise: Promise<SaoCatalogs> | null = null

export function useSaoGuideLocalization(uiLanguage: string | undefined) {
  const [catalogs, setCatalogs] = useState<SaoCatalogs>(BUNDLED_CATALOGS)

  useEffect(() => {
    let active = true

    void loadRemoteSaoCatalogs().then((remoteCatalogs) => {
      if (!active || !Object.keys(remoteCatalogs).length) {
        return
      }

      setCatalogs({
        ...BUNDLED_CATALOGS,
        ...remoteCatalogs,
      })
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

  for (const fallbackCatalog of Object.values(BUNDLED_CATALOGS)) {
    const value = cleanGameFormatting(fallbackCatalog[key])

    if (value) {
      return value
    }
  }

  return canonicalLabel(key)
}

export function localizationKeyMatchesQuery(
  key: string,
  query: string,
  catalogs: SaoCatalogs,
  aliases: string[] = [],
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

  const searchableKeys = [
    location.nameKey,
    location.descriptionKey,
    ...location.npcs.flatMap((npc) => [
      npc.nameKey,
      GUIDE_NPC_ROLE_KEYS[npc.role].role,
      GUIDE_NPC_ROLE_KEYS[npc.role].description,
    ]),
    ...location.mobKeys,
    ...location.cometMobKeys,
    ...location.weaponKeys,
    ...location.petKeys,
    ...location.dungeonPetKeys,
    ...location.materialKeys,
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
  const indexResponse = await fetch(LANGUAGE_INDEX_URL, {
    cache: "no-store",
    credentials: "omit",
  })

  if (!indexResponse.ok) {
    return {}
  }

  const index = (await indexResponse.json()) as LocalizationIndex
  const saoPies = (index.langs ?? []).flatMap((language) => {
    const saoPie = language.pies?.find((pie) => pie.pieKey === "sao")

    if (!language.lang || !saoPie?.url) {
      return []
    }

    return [{ language: language.lang, url: saoPie.url }]
  })
  const results = await Promise.allSettled(
    saoPies.map(async ({ language, url }) => {
      const response = await fetch(url, {
        cache: "force-cache",
        credentials: "omit",
      })

      if (!response.ok) {
        throw new Error(`Failed to load SAO localization ${language}`)
      }

      const pie = (await response.json()) as LocalizationPie

      return [language, pie.properties ?? {}] as const
    }),
  )

  return Object.fromEntries(
    results.flatMap((result) => (result.status === "fulfilled" ? [result.value] : [])),
  )
}

function unique(values: string[]) {
  return [...new Set(values)]
}

function canonicalLabel(key: string) {
  return key.split(".").at(-2)?.replaceAll("_", " ") ?? key
}
