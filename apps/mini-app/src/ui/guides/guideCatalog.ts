import { GUIDE_DUMP_LOCATION_BY_ID } from "./guideLocationDumpData"

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

const LOCATION_IDS = [-1, 0, ...Array.from({ length: 28 }, (_, index) => index + 1)]

const LOCATION_ARTWORK_URLS: Record<number, string> = {
  [-1]: "/guides/locations/location-dungeon-hall.webp",
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

export const GUIDE_LOCATIONS: GuideLocation[] = LOCATION_IDS.map(buildLocation)

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

function buildLocation(id: number): GuideLocation {
  const dump = GUIDE_DUMP_LOCATION_BY_ID.get(id)
  const dropKeys = [
    ...(dump?.cases.flatMap((caseData) => caseData.drops.map((drop) => drop.key)) ?? []),
    ...(dump?.mobs.flatMap((mob) => mob.drops.map((drop) => drop.key)) ?? []),
  ]

  return {
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
    mobKeys: dump?.mobs.map((mob) => mob.key) ?? [],
    cometMobKeys: id >= 11 ? [`sao.mob.comet${id}_1.name`] : [],
    weaponKeys: unique(dropKeys.filter((key) => key.startsWith(`sao.item.l${id}_`))),
    petKeys: unique(dropKeys.filter((key) => key.startsWith(`sao.item.pet_l${id}_`))),
    donatePetKeys: unique(dropKeys.filter((key) => key.startsWith(`sao.item.petd_l${id}_`))),
    materialKeys:
      id >= 11
        ? [`sao.item.weapon_upgrade_material_${id}.name`, `sao.item.comet_shard_${id}_1.name`]
        : [],
  }
}

function unique(values: string[]) {
  return [...new Set(values)]
}
