import { describe, expect, it } from "vitest"
import { getGuideSourceItemStats, getGuideSourceMobStats } from "./guideSourceStats"

describe("source-backed Guide stats", () => {
  it("exposes current item stats from the attached location dump", () => {
    expect(getGuideSourceItemStats("sao.item.l16_1.name")).toEqual([
      { type: "POWER", value: "600Oc" },
      { type: "SELL_PRICE", value: "1.74Ocd" },
      { type: "LEVEL", value: "1" },
    ])
  })

  it("exposes pet power without retaining drop chance data", () => {
    const stats = getGuideSourceItemStats("sao.item.pet_l16_1.name")

    expect(stats).toContainEqual({ type: "PET_POWER", value: "x3Qa" })
    expect(JSON.stringify(stats)).not.toContain("chance")
  })

  it("exposes source mob health and activity facts", () => {
    expect(getGuideSourceMobStats("sao.mob.mob16_1.name")).toEqual({
      spawn: "ALWAYS",
      health: "48.95Sxvg",
      moneyDrop: "1.68Ocd",
      killTime: "1с",
      kills: "3.76K",
    })
  })

  it("rejects unrelated localization keys", () => {
    expect(getGuideSourceItemStats("sao.mob.mob16_1.name")).toEqual([])
    expect(getGuideSourceMobStats("sao.item.l16_1.name")).toBeUndefined()
  })
})
