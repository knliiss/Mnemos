import { describe, expect, it } from "vitest"
import { guideEntityIndex, guideWeaponArtworkStyle } from "./guideWeaponArtwork"

describe("guide weapon artwork", () => {
  it("maps confirmed weapon keys to their resource-pack atlas cell", () => {
    expect(guideWeaponArtworkStyle("sao.item.l1_4.name")).toMatchObject({
      backgroundPosition: "-114px 0px",
      backgroundSize: "456px 1064px",
    })

    expect(guideWeaponArtworkStyle("sao.item.l28_12.name")).toMatchObject({
      backgroundPosition: "-418px -1026px",
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
