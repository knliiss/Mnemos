export type GuideNpcRole = "RAID_JOIN" | "QUEST"

export type GuideNpc = {
  nameKey: string
  role: GuideNpcRole
}

export type GuideLocation = {
  id: number
  artworkUrl: string
  nameKey: string
  descriptionKey: string
  npcs: GuideNpc[]
  mobKeys: string[]
  cometMobKeys: string[]
}

const LOCATION_ARTWORK_URLS: Record<number, string> = {
  [-1]: "/guides/locations/location-special-dungeon-hall.webp",
  0: "/guides/locations/location-00-starter-village.webp",
  1: "/guides/locations/location-01-dark-forest.webp",
  2: "/guides/locations/location-02-pirate-ship.webp",
  3: "/guides/locations/location-03-shinobi.webp",
  4: "/guides/locations/location-04-polar-tundra.webp",
  5: "/guides/locations/location-05-aether-city.webp",
  6: "/guides/locations/location-06-hell.webp",
  7: "/guides/locations/location-07-ancient-sands.webp",
  8: "/guides/locations/location-08-enchanted-forest.webp",
  9: "/guides/locations/location-09-magic-mine.webp",
  10: "/guides/locations/location-10-secret-land.webp",
  11: "/guides/locations/location-11-container-yard.webp",
  12: "/guides/locations/location-12-viking-valley.webp",
  13: "/guides/locations/location-13-abandoned-prison.webp",
  14: "/guides/locations/location-14-oasis.webp",
  15: "/guides/locations/location-15-canyon.webp",
  16: "/guides/locations/location-16-obelisk-meadows.webp",
  17: "/guides/locations/location-17-sunken-temple.webp",
  18: "/guides/locations/location-18-port.webp",
  19: "/guides/locations/location-19-blazing-abyss.webp",
  20: "/guides/locations/location-20-sea-castle.webp",
  21: "/guides/locations/location-21-taiping-tower.webp",
  22: "/guides/locations/location-22-mushroom-world.webp",
  23: "/guides/locations/location-23-palisade.webp",
  24: "/guides/locations/location-24-robot-village.webp",
  25: "/guides/locations/location-25-cosmodriver.webp",
  26: "/guides/locations/location-26-cursed-mall.webp",
  27: "/guides/locations/location-27-bio-lab.webp",
  28: "/guides/locations/location-28-waterfall.webp",
}

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

export const GUIDE_LOCATIONS: GuideLocation[] = LOCATION_IDS.map((id) => ({
  id,
  artworkUrl: LOCATION_ARTWORK_URLS[id],
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
