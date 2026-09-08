import { describe, expect, it } from "vitest"
import { resolveGuideWeaponSprite } from "./guideWeaponArtwork"

describe("Guide weapon artwork", () => {
  it("maps a source-backed location weapon to the inventory-rendered Guide atlas", () => {
    expect(resolveGuideWeaponSprite("sao.item.l23_1.name")).toEqual({
      column: 0,
      row: 22,
      atlas: {
        path: "/guides/entities/weapon-inventory-atlas.webp",
        columns: 12,
        rows: 28,
      },
    })
  })

  it("does not invent sparse early-location weapons", () => {
    expect(resolveGuideWeaponSprite("sao.item.l1_3.name")).toBeNull()
    expect(resolveGuideWeaponSprite("sao.item.l2_4.name")).toBeNull()
  })

  it("rejects unrelated and out-of-range keys", () => {
    expect(resolveGuideWeaponSprite("sao.item.d1_324.name")).toBeNull()
    expect(resolveGuideWeaponSprite("sao.item.l29_1.name")).toBeNull()
    expect(resolveGuideWeaponSprite("sao.item.l23_13.name")).toBeNull()
  })
})
