import rawItemStats from "./data/guideSourceItemStats.generated.json"
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

const ITEM_STATS = new Map(
  (rawItemStats as RawItemRow[]).map(([itemId, stats]) => [
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
