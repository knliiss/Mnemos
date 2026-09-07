import rawItemStats1 from "./data/guideSourceItemStats1.generated.json"
import rawItemStats2 from "./data/guideSourceItemStats2.generated.json"
import rawItemStats3 from "./data/guideSourceItemStats3.generated.json"
import rawItemStats4 from "./data/guideSourceItemStats4.generated.json"
import rawItemStats5 from "./data/guideSourceItemStats5.generated.json"
import rawMobStats from "./data/guideSourceMobStats.generated.json"

export type GuideSourceItemStatType =
  | "POWER"
  | "SELL_PRICE"
  | "LEVEL"
  | "SATIETY"
  | "SATIETY_LEVEL"
  | "PET_POWER"
  | "DAMAGE_BOOST"
  | "POWER_BOOST"
  | "MONEY_BOOST"
  | "CRIT_CHANCE"
  | "MULTICRIT"

export type GuideSourceItemStat = {
  type: GuideSourceItemStatType
  value: string
}

export type GuideSourceMobSpawn = "ALWAYS" | "DARKNESS" | "BLOOD_MOON"

export type GuideSourceMobStats = {
  spawn: GuideSourceMobSpawn
  health: string
  moneyDrop: string
  killTime: string
  kills: string
}

type RawItemStat = [type: GuideSourceItemStatType, value: string]
type RawItemRow = [itemId: string, stats: RawItemStat[]]
type RawMobRow = [
  mobId: string,
  spawn: GuideSourceMobSpawn,
  health: string,
  moneyDrop: string,
  killTime: string,
  kills: string,
]

const rawItemStats = [
  ...(rawItemStats1 as RawItemRow[]),
  ...(rawItemStats2 as RawItemRow[]),
  ...(rawItemStats3 as RawItemRow[]),
  ...(rawItemStats4 as RawItemRow[]),
  ...(rawItemStats5 as RawItemRow[]),
]

const ITEM_STATS = new Map(
  rawItemStats.map(([itemId, stats]) => [
    itemId,
    stats.map(([type, value]) => ({ type, value })),
  ]),
)

const MOB_STATS = new Map(
  (rawMobStats as RawMobRow[]).map(([mobId, spawn, health, moneyDrop, killTime, kills]) => [
    mobId,
    { spawn, health, moneyDrop, killTime, kills },
  ]),
)

export function getGuideSourceItemStats(localizationKey: string): GuideSourceItemStat[] {
  const itemId = localizationKeyToId(localizationKey, "sao.item.")

  if (!itemId) {
    return []
  }

  return ITEM_STATS.get(itemId) ?? []
}

export function getGuideSourceMobStats(localizationKey: string): GuideSourceMobStats | undefined {
  const mobId = localizationKeyToId(localizationKey, "sao.mob.")

  if (!mobId) {
    return undefined
  }

  return MOB_STATS.get(mobId)
}

function localizationKeyToId(localizationKey: string, prefix: string) {
  if (!localizationKey.startsWith(prefix) || !localizationKey.endsWith(".name")) {
    return null
  }

  return localizationKey.slice(prefix.length, -".name".length)
}
