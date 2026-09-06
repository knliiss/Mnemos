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
  weaponKeys: string[]
  petKeys: string[]
  donatePetKeys: string[]
  materialKeys: string[]
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

const WEAPON_INDEXES: Record<number, number[]> = {
  1: [1, 2, 4, 5, 6, 7, 8, 9],
  2: [1, 2, 3, 5, 6, 7, 8, 9, 10],
  3: range(10),
  4: range(10),
  5: range(10),
  6: range(10),
  7: range(11),
  8: range(11),
  9: range(12),
  10: range(12),
  11: range(12),
  12: range(12),
  13: range(12),
  14: range(12),
  15: range(12),
  16: range(12),
  17: range(12),
  18: range(12),
  19: range(12),
  20: range(12),
  21: range(12),
  22: range(12),
  23: range(12),
  24: range(12),
  25: range(12),
  26: range(12),
  27: range(12),
  28: range(12),
}

const PET_COUNTS: Record<number, number> = {
  1: 11,
  2: 6,
  3: 6,
  4: 6,
  5: 7,
  6: 6,
  7: 8,
  8: 8,
  9: 8,
  10: 8,
  11: 8,
  12: 14,
  13: 8,
  14: 8,
  15: 8,
  16: 8,
  17: 8,
  18: 8,
  19: 8,
  20: 8,
  21: 8,
  22: 9,
  23: 9,
  24: 9,
  25: 9,
  26: 9,
  27: 9,
  28: 9,
}

const DONATE_PET_COUNT = 5
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
  mobKeys: numberedKeys(id, MOB_COUNTS[id], (index) => `sao.mob.mob${id}_${index}.name`),
  cometMobKeys: id >= 11 ? [`sao.mob.comet${id}_1.name`] : [],
  weaponKeys: indexedKeys(WEAPON_INDEXES[id], (index) => `sao.item.l${id}_${index}.name`),
  petKeys: numberedKeys(id, PET_COUNTS[id], (index) => `sao.item.pet_l${id}_${index}.name`),
  donatePetKeys: numberedKeys(
    id,
    id > 0 ? DONATE_PET_COUNT : undefined,
    (index) => `sao.item.petd_l${id}_${index}.name`,
  ),
  materialKeys:
    id >= 11 ? [`sao.item.weapon_upgrade_material_${id}.name`, `sao.item.comet_shard_${id}_1.name`] : [],
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

function range(count: number) {
  return Array.from({ length: count }, (_, index) => index + 1)
}

function indexedKeys(indexes: number[] | undefined, key: (index: number) => string) {
  if (!indexes) {
    return []
  }

  return indexes.map(key)
}

function numberedKeys(
  locationId: number,
  count: number | undefined,
  key: (index: number) => string,
) {
  if (locationId <= 0 || !count) {
    return []
  }

  return range(count).map(key)
}
