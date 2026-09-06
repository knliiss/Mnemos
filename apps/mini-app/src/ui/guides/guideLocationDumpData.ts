import rawLocations1 from "./data/locationGuideData1.generated.json"
import rawLocations2 from "./data/locationGuideData2.generated.json"
import rawLocations3 from "./data/locationGuideData3.generated.json"
import rawLocations4 from "./data/locationGuideData4.generated.json"

export type GuideDumpRarity =
  | "COMMON"
  | "RARE"
  | "EPIC"
  | "LEGENDARY"
  | "MYTHIC"
  | "SECRET"
  | "OTHER"
  | "UNKNOWN"

export type GuideDumpDrop = {
  key: string
  rarity: GuideDumpRarity
  quantity: number
}

export type GuideDumpCase = {
  key: string
  kind: "NORMAL" | "DONATE" | "SPECIAL"
  price: string
  drops: GuideDumpDrop[]
}

export type GuideDumpMob = {
  key: string
  artworkId: string
  drops: GuideDumpDrop[]
}

export type GuideDumpLocation = {
  id: number
  purchasePrice: string
  detailsAvailable: boolean
  cases: GuideDumpCase[]
  mobs: GuideDumpMob[]
}

type RawRarity = "C" | "R" | "E" | "L" | "M" | "S" | "O" | "U"
type RawCaseKind = "N" | "D" | "S"
type RawDrop = [itemId: string, rarity: RawRarity, quantity: number]
type RawCase = [caseId: string, kind: RawCaseKind, price: string, drops: RawDrop[]]
type RawMob = [mobId: string, artworkId: string, drops: RawDrop[]]
type RawLocation = [
  id: number,
  purchasePrice: string,
  detailsAvailable: boolean,
  cases: RawCase[],
  mobs: RawMob[],
]

const RARITY: Record<RawRarity, GuideDumpRarity> = {
  C: "COMMON",
  R: "RARE",
  E: "EPIC",
  L: "LEGENDARY",
  M: "MYTHIC",
  S: "SECRET",
  O: "OTHER",
  U: "UNKNOWN",
}

const CASE_KIND: Record<RawCaseKind, GuideDumpCase["kind"]> = {
  N: "NORMAL",
  D: "DONATE",
  S: "SPECIAL",
}

const rawLocations = [
  ...(rawLocations1 as RawLocation[]),
  ...(rawLocations2 as RawLocation[]),
  ...(rawLocations3 as RawLocation[]),
  ...(rawLocations4 as RawLocation[]),
]

export const GUIDE_DUMP_LOCATIONS: GuideDumpLocation[] = rawLocations.map(decodeLocation)

export const GUIDE_DUMP_LOCATION_BY_ID = new Map(
  GUIDE_DUMP_LOCATIONS.map((location) => [location.id, location]),
)

function decodeLocation(raw: RawLocation): GuideDumpLocation {
  return {
    id: raw[0],
    purchasePrice: raw[1],
    detailsAvailable: raw[2],
    cases: raw[3].map(decodeCase),
    mobs: raw[4].map(decodeMob),
  }
}

function decodeCase(raw: RawCase): GuideDumpCase {
  return {
    key: `sao.lootbox.${raw[0]}.name`,
    kind: CASE_KIND[raw[1]],
    price: raw[2],
    drops: raw[3].map(decodeDrop),
  }
}

function decodeMob(raw: RawMob): GuideDumpMob {
  return {
    key: `sao.mob.${raw[0]}.name`,
    artworkId: raw[1],
    drops: raw[2].map(decodeDrop),
  }
}

function decodeDrop(raw: RawDrop): GuideDumpDrop {
  return {
    key: `sao.item.${raw[0]}.name`,
    rarity: RARITY[raw[1]],
    quantity: raw[2],
  }
}
