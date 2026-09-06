import { describe, expect, it } from "vitest"
import { canAccessGuides } from "./guideAccess"
import { GUIDE_LOCATIONS } from "./guideCatalog"
import { GUIDE_EN_FALLBACK } from "./guideEnglishFallback"
import {
  localizationKeyExists,
  localizationKeyMatchesQuery,
  locationMatchesQuery,
  normalizeGuideSearchText,
  resolveSaoText,
  saoLanguageForUi,
} from "./guideLocalization"
import { verifiedGuideMechanics } from "./guideMechanics"
import { GUIDE_CASE_MILESTONES, GUIDE_DUNGEON_MILESTONES } from "./guideMilestones"
import { GUIDE_REBIRTH_STEPS } from "./guideRebirthData"

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

  it("maps source-backed weapon and pet localization keys by location", () => {
    const darkForest = GUIDE_LOCATIONS.find((candidate) => candidate.id === 1)!
    const pirateShip = GUIDE_LOCATIONS.find((candidate) => candidate.id === 2)!
    const secretLand = GUIDE_LOCATIONS.find((candidate) => candidate.id === 10)!
    const containerYard = GUIDE_LOCATIONS.find((candidate) => candidate.id === 11)!
    const vikingValley = GUIDE_LOCATIONS.find((candidate) => candidate.id === 12)!

    expect(darkForest.weaponKeys).toEqual([
      "sao.item.l1_1.name",
      "sao.item.l1_2.name",
      "sao.item.l1_4.name",
      "sao.item.l1_5.name",
      "sao.item.l1_6.name",
      "sao.item.l1_7.name",
      "sao.item.l1_8.name",
      "sao.item.l1_9.name",
    ])
    expect(pirateShip.weaponKeys).toEqual([
      "sao.item.l2_1.name",
      "sao.item.l2_2.name",
      "sao.item.l2_3.name",
      "sao.item.l2_5.name",
      "sao.item.l2_6.name",
      "sao.item.l2_7.name",
      "sao.item.l2_8.name",
      "sao.item.l2_9.name",
      "sao.item.l2_10.name",
    ])
    expect(darkForest.petKeys).toHaveLength(11)
    expect(darkForest.dungeonPetKeys).toHaveLength(5)
    expect(secretLand.weaponKeys).toHaveLength(12)
    expect(containerYard.materialKeys).toEqual(["sao.item.weapon_upgrade_material_11.name"])
    expect(vikingValley.petKeys).toHaveLength(14)
  })

  it("keeps special areas outside numbered equipment mappings", () => {
    const starterVillage = GUIDE_LOCATIONS.find((candidate) => candidate.id === 0)!
    const dungeonHall = GUIDE_LOCATIONS.find((candidate) => candidate.id === -1)!

    expect(starterVillage.weaponKeys).toEqual([])
    expect(starterVillage.petKeys).toEqual([])
    expect(dungeonHall.weaponKeys).toEqual([])
    expect(dungeonHall.petKeys).toEqual([])
  })
})

describe("guide progression", () => {
  it("uses detailed per-location RB values over conflicting summary values", () => {
    expect(GUIDE_REBIRTH_STEPS).toHaveLength(67)
    expect(GUIDE_REBIRTH_STEPS.find((step) => step.rank === 46)?.growth).toBe("x5")
    expect(GUIDE_REBIRTH_STEPS.find((step) => step.rank === 50)?.growth).toBe("x2.39")
    expect(GUIDE_REBIRTH_STEPS.find((step) => step.rank === 51)?.growth).toBe("x2")
  })

  it("keeps detailed late-game ranks missing from the old summary", () => {
    expect(GUIDE_REBIRTH_STEPS.find((step) => step.rank === 65)).toMatchObject({
      requirement: "345Sgt",
      growth: "x5.19",
      nextRank: 66,
    })
    expect(GUIDE_REBIRTH_STEPS.find((step) => step.rank === 67)).toMatchObject({
      requirement: "1.93Noi",
      growth: "x3.75",
      nextRank: 68,
    })
  })

  it("contains the verified dungeon and case milestones", () => {
    expect(GUIDE_DUNGEON_MILESTONES.EASY.at(-1)).toEqual({
      stage: 50,
      reward: "PET_SLOT",
      value: "1",
    })
    expect(GUIDE_DUNGEON_MILESTONES.INSANE.at(-1)).toEqual({
      stage: 36,
      reward: "LUCK",
      value: "15%",
    })
    expect(GUIDE_CASE_MILESTONES.at(-1)).toEqual({
      opened: 5_000_000,
      reward: "LUCK",
      value: "7%",
    })
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

  it("matches one localization key across every loaded SAO language", () => {
    const catalogs = {
      en_US: { "sao.rebirth.1.name": "Beginner" },
      ru_RU: { "sao.rebirth.1.name": "Новичок" },
      uk_UA: { "sao.rebirth.1.name": "Новачок" },
    }

    expect(localizationKeyMatchesQuery("sao.rebirth.1.name", "Beginner", catalogs)).toBe(true)
    expect(localizationKeyMatchesQuery("sao.rebirth.1.name", "Новичок", catalogs)).toBe(true)
    expect(localizationKeyMatchesQuery("sao.rebirth.1.name", "Новачок", catalogs)).toBe(true)
    expect(localizationKeyMatchesQuery("sao.dungeon.EASY.name", "LP", catalogs, ["LP"])).toBe(true)
  })

  it("tracks whether an entity is actually backed by a loaded SAO catalog", () => {
    const catalogs = {
      en_US: { "sao.item.l1_1.name": "Old Sword" },
      ru_RU: {},
    }

    expect(localizationKeyExists("sao.item.l1_1.name", catalogs)).toBe(true)
    expect(localizationKeyExists("sao.item.l1_3.name", catalogs)).toBe(false)
  })

  it("searches location weapons and pets in every loaded SAO language", () => {
    const darkForest = GUIDE_LOCATIONS.find((candidate) => candidate.id === 1)!
    const weaponKey = darkForest.weaponKeys[0]
    const petKey = darkForest.petKeys[0]
    const catalogs = {
      en_US: {
        [weaponKey]: "Old Sword",
        [petKey]: "Treeling",
      },
      ru_RU: {
        [weaponKey]: "Старый меч",
        [petKey]: "Древень",
      },
      uk_UA: {
        [weaponKey]: "Старий меч",
        [petKey]: "Деревень",
      },
    }

    expect(locationMatchesQuery(darkForest, "Старый меч", catalogs)).toBe(true)
    expect(locationMatchesQuery(darkForest, "Деревень", catalogs)).toBe(true)
    expect(locationMatchesQuery(darkForest, "Treeling", catalogs)).toBe(true)
  })

  it("does not search guessed location entities that are missing from loaded catalogs", () => {
    const darkForest = GUIDE_LOCATIONS.find((candidate) => candidate.id === 1)!
    const missingWeaponKey = darkForest.weaponKeys[0]
    const catalogs = {
      en_US: {
        [darkForest.nameKey]: "Dark Forest",
      },
    }

    expect(localizationKeyExists(missingWeaponKey, catalogs)).toBe(false)
    expect(locationMatchesQuery(darkForest, "Old Sword", catalogs)).toBe(false)
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

  it("keeps mechanics hidden until a complete localized article is available", () => {
    const emptyCatalogs = {
      en_US: {},
    }
    const completeCatalogs = {
      en_US: {
        "sao.guide.first_hit.title": "First hit",
        "sao.guide.first_hit.text": "Hit your first enemy.",
        "sao.guide.first_hit.hint": "Approach an enemy.",
        "sao.guide.first_hit.where": "Starter Village",
      },
    }

    expect(verifiedGuideMechanics(emptyCatalogs)).toEqual([])
    expect(verifiedGuideMechanics(completeCatalogs).map((mechanic) => mechanic.id)).toEqual([
      "first_hit",
    ])
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
