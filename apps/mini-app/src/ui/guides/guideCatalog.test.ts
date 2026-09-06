import { describe, expect, it } from "vitest"
import { GUIDE_LOCATIONS } from "./guideCatalog"
import { GUIDE_EN_FALLBACK } from "./guideEnglishFallback"
import {
  locationMatchesQuery,
  normalizeGuideSearchText,
  resolveSaoText,
  saoLanguageForUi,
} from "./guideLocalization"
import { canAccessGuides } from "./guideAccess"

describe("guide catalog", () => {
  it("contains starter village, dungeon hall and all 28 numbered locations", () => {
    expect(GUIDE_LOCATIONS).toHaveLength(30)
    expect(GUIDE_LOCATIONS.map((location) => location.id)).toEqual([
      0,
      ...Array.from({ length: 28 }, (_, index) => index + 1),
      -1,
    ])
  })

  it("uses the SAO localization identity for location 23", () => {
    const location = GUIDE_LOCATIONS.find((candidate) => candidate.id === 23)

    expect(location).toBeDefined()
    expect(GUIDE_EN_FALLBACK[location!.nameKey]).toBe("Palisade")
  })

  it("keeps detailed creatures and NPCs for numbered locations", () => {
    const darkForest = GUIDE_LOCATIONS.find((candidate) => candidate.id === 1)

    expect(darkForest?.npcs).toHaveLength(2)
    expect(darkForest?.mobKeys).toHaveLength(4)
    expect(darkForest?.mobKeys.map((key) => GUIDE_EN_FALLBACK[key])).toEqual([
      "Goblin",
      "Dark Goblin",
      "Goblin Scout",
      "Goblin Warrior",
    ])
  })
})

describe("guide localization", () => {
  it("maps Mnemos UI languages to SAO locales", () => {
    expect(saoLanguageForUi("uk")).toBe("uk_UA")
    expect(saoLanguageForUi("ru")).toBe("ru_RU")
    expect(saoLanguageForUi("en")).toBe("en_US")
  })

  it("uses preferred language before RU and EN fallbacks", () => {
    const catalogs = {
      uk_UA: { key: "Українська" },
      ru_RU: { key: "Русский" },
      en_US: { key: "English" },
    }

    expect(resolveSaoText("key", "uk_UA", catalogs)).toBe("Українська")
    expect(resolveSaoText("key", "hy_AM", catalogs)).toBe("Русский")
  })

  it("searches bundled aliases independently from display language", () => {
    const darkForest = GUIDE_LOCATIONS.find((candidate) => candidate.id === 1)!

    const catalogs = {
      en_US: GUIDE_EN_FALLBACK,
      uk_UA: {
        [darkForest.nameKey]: "Темний ліс",
      },
    }

    expect(locationMatchesQuery(darkForest, "Темний ліс", catalogs)).toBe(true)
    expect(locationMatchesQuery(darkForest, "Goblin Scout", catalogs)).toBe(true)
    expect(locationMatchesQuery(darkForest, "локація 1", catalogs)).toBe(true)
  })

  it("normalizes diacritics and game formatting", () => {
    expect(normalizeGuideSearchText("¨4a8cec<Portal>  Café")).toBe("portal cafe")
  })
})

describe("guide access", () => {
  it("is restricted to administrators", () => {
    expect(canAccessGuides(["USER"])).toBe(false)
    expect(canAccessGuides(["MODERATOR"])).toBe(false)
    expect(canAccessGuides(["ADMIN"])).toBe(true)
    expect(canAccessGuides(["SUPER_ADMIN"])).toBe(true)
  })
})
