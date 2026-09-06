export type GuideNpcRole = "RAID_JOIN" | "QUEST"

export type GuideNpc = {
  nameKey: string
  role: GuideNpcRole
}

export type GuideLocation = {
  id: number
  artworkPosition: string
  nameKey: string
  descriptionKey: string
  npcs: GuideNpc[]
  mobKeys: string[]
  cometMobKeys: string[]
}

export const GUIDE_LOCATION_ATLAS_PARTS = [
  "/guides/locations/sao-location-atlas.part-00.b64",
  "/guides/locations/sao-location-atlas.part-01.b64",
  "/guides/locations/sao-location-atlas.part-02.b64",
  "/guides/locations/sao-location-atlas.part-03.b64",
  "/guides/locations/sao-location-atlas.part-04.b64",
]

const MOB_COUNTS: Record<number, number> = {
  1: 4,
  2: 4,
  3: 4,
  4: 4,
  5: 4,
  6: 4,
  7: 4,
  8: 5,
  9: 6,
  10: 7,
  11: 7,
  12: 7,
  13: 8,
  14: 7,
  15: 7,
  16: 7,
  17: 7,
  18: 7,
  19: 7,
  20: 7,
  21: 7,
  22: 7,
  23: 7,
  24: 7,
  25: 7,
  26: 7,
  27: 7,
  28: 7,
}

const LOCATION_IDS = [0, ...Array.from({ length: 28 }, (_, index) => index + 1), -1]

export const GUIDE_LOCATIONS: GuideLocation[] = LOCATION_IDS.map((id, atlasIndex) => ({
  id,
  artworkPosition: `${(atlasIndex % 5) * 25}% ${Math.floor(atlasIndex / 5) * 20}%`,
  nameKey: `sao.location.${id}.name`,
  descriptionKey: `sao.location.${id}.description`,
  npcs:
    id > 0
      ? [
          { nameKey: `sao.npc.${id}_RAID_JOIN.name`, role: "RAID_JOIN" },
          { nameKey: `sao.npc.${id}_QUEST.name`, role: "QUEST" },
        ]
      : [],
  mobKeys:
    id > 0
      ? Array.from(
          { length: MOB_COUNTS[id] ?? 0 },
          (_, index) => `sao.mob.mob${id}_${index + 1}.name`,
        )
      : [],
  cometMobKeys: id >= 11 ? [`sao.mob.comet${id}_1.name`] : [],
}))

export const GUIDE_NPC_ROLE_KEYS: Record<GuideNpcRole, { role: string; description: string }> = {
  RAID_JOIN: {
    role: "sao.enum.npc.raid_join.role",
    description: "sao.enum.npc.raid_join.description",
  },
  QUEST: {
    role: "sao.enum.npc.quest.role",
    description: "sao.enum.npc.quest.description",
  },
}
