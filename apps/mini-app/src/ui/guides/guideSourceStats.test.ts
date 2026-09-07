import { describe, expect, it } from "vitest"
import rawItemStats1 from "./data/guideSourceItemStats1.generated.json"
import rawItemStats2 from "./data/guideSourceItemStats2.generated.json"
import rawItemStats3 from "./data/guideSourceItemStats3.generated.json"
import rawItemStats4 from "./data/guideSourceItemStats4.generated.json"
import rawItemStats5 from "./data/guideSourceItemStats5.generated.json"
import rawMobStats from "./data/guideSourceMobStats.generated.json"
import { getGuideSourceItemStats, getGuideSourceMobStats } from "./guideSourceStats"

const rawItemStats = [
  ...rawItemStats1,
  ...rawItemStats2,
  ...rawItemStats3,
  ...rawItemStats4,
  ...rawItemStats5,
]

describe("source-backed Guide stats", () => {
  it("keeps the complete unique item stat coverage from the attached dump", () => {
    const itemIds = rawItemStats.map(([itemId]) => itemId)

    expect(itemIds).toHaveLength(701)
    expect(new Set(itemIds)).toHaveLength(701)
    expect(JSON.stringify(rawItemStats).toLowerCase()).not.toContain("chance")
  })

  it("keeps the complete unique mob stat coverage from the attached dump", () => {
    const mobIds = rawMobStats.map(([mobId]) => mobId)

    expect(mobIds).toHaveLength(166)
    expect(new Set(mobIds)).toHaveLength(166)
  })

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
