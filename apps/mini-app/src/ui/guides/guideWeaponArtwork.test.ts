import { describe, expect, it } from "vitest"
import { guideEntityIndex, guideWeaponArtworkStyle } from "./guideWeaponArtwork"

describe("guide weapon artwork", () => {
  it("maps confirmed weapon keys to the source-backed resource-pack atlas", () => {
    expect(guideWeaponArtworkStyle("sao.item.l1_4.name")).toMatchObject({
      backgroundImage: "url(/guides/entities/weapon-atlas.webp)",
      backgroundPosition: "-168px 0px",
      backgroundSize: "672px 1568px",
    })

    expect(guideWeaponArtworkStyle("sao.item.l28_12.name")).toMatchObject({
      backgroundImage: "url(/guides/entities/weapon-atlas.webp)",
      backgroundPosition: "-616px -1512px",
      backgroundSize: "672px 1568px",
    })
  })

  it("scales the exact same source cell for full in-app detail artwork", () => {
    expect(guideWeaponArtworkStyle("sao.item.l5_2.name", 92)).toMatchObject({
      backgroundImage: "url(/guides/entities/weapon-atlas.webp)",
      backgroundPosition: "-92px -368px",
      backgroundSize: "1104px 2576px",
    })
  })

  it("does not manufacture artwork mappings for unrelated keys", () => {
    expect(guideWeaponArtworkStyle("sao.item.pet_l1_1.name")).toBeUndefined()
    expect(guideWeaponArtworkStyle("sao.item.l29_1.name")).toBeUndefined()
  })

  it("preserves the canonical entity index when sparse weapon slots are displayed", () => {
    expect(guideEntityIndex("sao.item.l1_4.name", 3)).toBe(4)
    expect(guideEntityIndex("sao.mob.l1_1.name", 7)).toBe(1)
    expect(guideEntityIndex("sao.ui.weapon", 7)).toBe(7)
  })
})
