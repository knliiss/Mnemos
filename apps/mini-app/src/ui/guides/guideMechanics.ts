import { localizationKeyMatchesQuery, type SaoCatalogs } from "./guideLocalization"

export type GuideMechanicChapter =
  | "FIRST_STEPS"
  | "GROWTH"
  | "GEAR"
  | "QUESTS"
  | "RAIDS"
  | "DUNGEONS"
  | "ENDGAME"

export type GuideMechanic = {
  id: string
  chapter: GuideMechanicChapter
  titleKey: string
  textKey: string
  hintKey: string
  whereKey: string
}

const MECHANIC_DEFINITIONS: Array<[string, GuideMechanicChapter]> = [
  ["first_hit", "FIRST_STEPS"],
  ["daily_reward", "FIRST_STEPS"],
  ["reach_location", "FIRST_STEPS"],
  ["first_loot", "FIRST_STEPS"],
  ["open_inventory", "FIRST_STEPS"],
  ["equip_weapon", "FIRST_STEPS"],
  ["open_case", "FIRST_STEPS"],
  ["equip_pet", "FIRST_STEPS"],
  ["stat_upgrade", "GROWTH"],
  ["buy_location", "GROWTH"],
  ["rebirth", "GROWTH"],
  ["use_booster", "GROWTH"],
  ["auto_click", "GROWTH"],
  ["delete_item", "GEAR"],
  ["save_asset", "GEAR"],
  ["upgrade_pet", "GEAR"],
  ["feed_pet", "GEAR"],
  ["upgrade_weapon", "GEAR"],
  ["enchant_weapon", "GEAR"],
  ["enchant_pet", "GEAR"],
  ["equip_book", "GEAR"],
  ["upgrade_item", "GEAR"],
  ["equip_relic", "GEAR"],
  ["upgrade_relic", "GEAR"],
  ["quest_done", "QUESTS"],
  ["battlepass_reward", "QUESTS"],
  ["join_gang", "QUESTS"],
  ["raid_visit", "RAIDS"],
  ["dungeon_pass", "DUNGEONS"],
  ["dungeon_shop", "DUNGEONS"],
] as const

export const GUIDE_MECHANICS: GuideMechanic[] = MECHANIC_DEFINITIONS.map(([id, chapter]) => ({
  id,
  chapter,
  titleKey: `sao.guide.${id}.title`,
  textKey: `sao.guide.${id}.text`,
  hintKey: `sao.guide.${id}.hint`,
  whereKey: `sao.guide.${id}.where`,
}))

export const GUIDE_MECHANIC_CHAPTER_KEYS: Record<GuideMechanicChapter, string> = {
  FIRST_STEPS: "sao.guide.chapter.first_steps",
  GROWTH: "sao.guide.chapter.growth",
  GEAR: "sao.guide.chapter.gear",
  QUESTS: "sao.guide.chapter.quests",
  RAIDS: "sao.guide.chapter.raids",
  DUNGEONS: "sao.guide.chapter.dungeons",
  ENDGAME: "sao.guide.chapter.endgame",
}

export function mechanicMatchesQuery(
  mechanic: GuideMechanic,
  query: string,
  catalogs: SaoCatalogs,
) {
  const keys = [mechanic.titleKey, mechanic.textKey, mechanic.hintKey, mechanic.whereKey]

  return keys.some((key) => localizationKeyMatchesQuery(key, query, catalogs, [mechanic.id]))
}
