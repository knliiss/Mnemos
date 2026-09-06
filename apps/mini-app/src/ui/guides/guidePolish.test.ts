import { describe, expect, it } from "vitest"
import { guideCometArtworkStyle } from "./guideCometArtwork"
import { GUIDE_CRITICAL_CATALOGS } from "./guideCriticalCatalogs"
import { resolveSaoText } from "./guideLocalization"
import { guideNpcArtworkStyle } from "./guideNpcArtwork"

describe("guide source-backed artwork", () => {
  it("maps raid and quest NPC portraits across the 28 numbered locations", () => {
    expect(guideNpcArtworkStyle(1, "RAID_JOIN")).toMatchObject({
      backgroundPosition: "0px 0px",
      backgroundSize: "152px 2128px",
    })
    expect(guideNpcArtworkStyle(1, "QUEST")).toMatchObject({
      backgroundPosition: "-76px 0px",
    })
    expect(guideNpcArtworkStyle(28, "QUEST")).toMatchObject({
      backgroundPosition: "-76px -2052px",
    })
    expect(guideNpcArtworkStyle(0, "QUEST")).toBeUndefined()
    expect(guideNpcArtworkStyle(29, "RAID_JOIN")).toBeUndefined()
  })

  it("maps every supported comet creature to the rendered resource-pack atlas", () => {
    expect(guideCometArtworkStyle("sao.mob.comet11_1.name")).toMatchObject({
      backgroundPosition: "0px 0px",
      backgroundSize: "552px 276px",
    })
    expect(guideCometArtworkStyle("sao.mob.comet28_1.name")).toMatchObject({
      backgroundPosition: "-460px -184px",
    })
    expect(guideCometArtworkStyle("sao.mob.comet10_1.name")).toBeUndefined()
    expect(guideCometArtworkStyle("sao.mob.mob11_1.name")).toBeUndefined()
  })
})

describe("guide cold-start localization", () => {
  it("keeps Ukrainian RB and dungeon labels readable without remote localization", () => {
    expect(resolveSaoText("sao.rebirth.1.name", "uk_UA", GUIDE_CRITICAL_CATALOGS)).toBe(
      "Новачок",
    )
    expect(resolveSaoText("sao.rebirth.68.name", "uk_UA", GUIDE_CRITICAL_CATALOGS)).toBe(
      "Архангел",
    )
    expect(resolveSaoText("sao.dungeon.EASY.name", "uk_UA", GUIDE_CRITICAL_CATALOGS)).toBe(
      "Легке підземелля",
    )
    expect(resolveSaoText("sao.location.5.name", "uk_UA", GUIDE_CRITICAL_CATALOGS)).toBe(
      "Ефірне місто",
    )
  })

  it("keeps Russian and English critical fallback chains concrete", () => {
    expect(resolveSaoText("sao.rebirth.50.name", "ru_RU", GUIDE_CRITICAL_CATALOGS)).toBe(
      "Царь",
    )
    expect(resolveSaoText("sao.dungeon.INSANE.name", "en_US", GUIDE_CRITICAL_CATALOGS)).toBe(
      "Expert Dungeon",
    )
  })
})
