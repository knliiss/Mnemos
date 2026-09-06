export type GuideDungeonRewardType =
  | "AURA_KEY"
  | "SPECIAL_SHARD"
  | "ATTACK_SPEED"
  | "POWER"
  | "DOUBLE_CASE"
  | "DAMAGE"
  | "PET_SLOT"
  | "LUCK"
  | "JEWELRY_SLOT"

export type GuideDungeonType = "EASY" | "NORMAL" | "HARD" | "INSANE"

export const GUIDE_DUNGEON_MILESTONES: Record<
  GuideDungeonType,
  Array<{ stage: number; reward: GuideDungeonRewardType; value: string }>
> = {
  EASY: [
    { stage: 4, reward: "AURA_KEY", value: "1" },
    { stage: 8, reward: "SPECIAL_SHARD", value: "1" },
    { stage: 12, reward: "ATTACK_SPEED", value: "15%" },
    { stage: 16, reward: "AURA_KEY", value: "1" },
    { stage: 20, reward: "SPECIAL_SHARD", value: "1" },
    { stage: 24, reward: "POWER", value: "50%" },
    { stage: 28, reward: "AURA_KEY", value: "1" },
    { stage: 32, reward: "SPECIAL_SHARD", value: "1" },
    { stage: 36, reward: "DOUBLE_CASE", value: "1" },
    { stage: 40, reward: "DAMAGE", value: "50%" },
    { stage: 44, reward: "POWER", value: "50%" },
    { stage: 48, reward: "DAMAGE", value: "50%" },
    { stage: 50, reward: "PET_SLOT", value: "1" },
  ],
  NORMAL: [
    { stage: 4, reward: "AURA_KEY", value: "1" },
    { stage: 8, reward: "LUCK", value: "5%" },
    { stage: 12, reward: "SPECIAL_SHARD", value: "1" },
    { stage: 16, reward: "ATTACK_SPEED", value: "20%" },
    { stage: 20, reward: "AURA_KEY", value: "1" },
    { stage: 24, reward: "POWER", value: "55%" },
    { stage: 28, reward: "SPECIAL_SHARD", value: "1" },
    { stage: 32, reward: "POWER", value: "170%" },
    { stage: 36, reward: "AURA_KEY", value: "1" },
    { stage: 40, reward: "PET_SLOT", value: "1" },
    { stage: 44, reward: "SPECIAL_SHARD", value: "1" },
    { stage: 48, reward: "JEWELRY_SLOT", value: "1" },
    { stage: 50, reward: "DAMAGE", value: "170%" },
  ],
  HARD: [
    { stage: 4, reward: "ATTACK_SPEED", value: "20%" },
    { stage: 8, reward: "POWER", value: "250%" },
    { stage: 12, reward: "PET_SLOT", value: "1" },
    { stage: 16, reward: "LUCK", value: "5%" },
    { stage: 20, reward: "POWER", value: "330%" },
    { stage: 24, reward: "DAMAGE", value: "200%" },
    { stage: 28, reward: "LUCK", value: "7.5%" },
    { stage: 32, reward: "POWER", value: "450%" },
    { stage: 36, reward: "LUCK", value: "10%" },
    { stage: 40, reward: "DAMAGE", value: "300%" },
    { stage: 44, reward: "POWER", value: "420%" },
    { stage: 48, reward: "DAMAGE", value: "450%" },
    { stage: 50, reward: "POWER", value: "650%" },
  ],
  INSANE: [
    { stage: 4, reward: "ATTACK_SPEED", value: "22%" },
    { stage: 8, reward: "POWER", value: "650%" },
    { stage: 12, reward: "DAMAGE", value: "475%" },
    { stage: 16, reward: "LUCK", value: "15%" },
    { stage: 20, reward: "POWER", value: "675%" },
    { stage: 24, reward: "DAMAGE", value: "500%" },
    { stage: 28, reward: "LUCK", value: "10%" },
    { stage: 32, reward: "POWER", value: "700%" },
    { stage: 36, reward: "LUCK", value: "15%" },
  ],
}

export type GuideCaseRewardType = "LUCK" | "PET_SLOT" | "AUTO_PET_CASE"

export const GUIDE_CASE_MILESTONES: Array<{
  opened: number
  reward: GuideCaseRewardType
  value: string
}> = [
  { opened: 25, reward: "LUCK", value: "5%" },
  { opened: 125, reward: "PET_SLOT", value: "1" },
  { opened: 250, reward: "LUCK", value: "5%" },
  { opened: 1_500, reward: "LUCK", value: "5%" },
  { opened: 3_250, reward: "AUTO_PET_CASE", value: "1" },
  { opened: 5_000, reward: "LUCK", value: "5%" },
  { opened: 25_000, reward: "LUCK", value: "5%" },
  { opened: 75_000, reward: "LUCK", value: "5.5%" },
  { opened: 100_000, reward: "LUCK", value: "5.5%" },
  { opened: 250_000, reward: "LUCK", value: "6%" },
  { opened: 500_000, reward: "LUCK", value: "6%" },
  { opened: 750_000, reward: "LUCK", value: "6.5%" },
  { opened: 1_000_000, reward: "LUCK", value: "6.5%" },
  { opened: 2_500_000, reward: "LUCK", value: "7%" },
  { opened: 5_000_000, reward: "LUCK", value: "7%" },
]
