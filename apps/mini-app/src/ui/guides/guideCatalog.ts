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
    artworkUrl: locationArtworkUrl(id),
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

function locationArtworkUrl(id: number) {
  if (id === -1) {
    return "/guides/locations-v2/location-dungeon-hall.webp"
  }

  if (id === 0) {
    return "/guides/locations-v2/location-starter-village.webp"
  }

  return `/guides/locations-v2/location-${String(id).padStart(2, "0")}.webp`
}

function unique(values: string[]) {
  return [...new Set(values)]
}
