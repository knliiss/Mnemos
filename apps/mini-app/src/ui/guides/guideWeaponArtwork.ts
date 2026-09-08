import type { SaoItemAtlas, SaoItemAtlasSprite } from "../components/events/saoTrackedItemArtwork"

const WEAPON_KEY_PATTERN = /^sao\.item\.l(\d+)_(\d+)\.name$/
export const GUIDE_WEAPON_ATLAS_PATH = "/guides/entities/weapon-inventory-atlas.webp"
export const GUIDE_WEAPON_ATLAS_COLUMNS = 12
export const GUIDE_WEAPON_ATLAS_ROWS = 28

const GUIDE_WEAPON_ATLAS: SaoItemAtlas = {
  path: GUIDE_WEAPON_ATLAS_PATH,
  columns: GUIDE_WEAPON_ATLAS_COLUMNS,
  rows: GUIDE_WEAPON_ATLAS_ROWS,
}

const EARLY_LOCATION_WEAPON_INDEXES: Record<number, readonly number[]> = {
  1: [1, 2, 4, 5, 6, 7, 8, 9],
  2: [1, 2, 3, 5, 6, 7, 8, 9, 10],
  3: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
  4: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
  5: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
  6: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
  7: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11],
  8: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11],
}

export function resolveGuideWeaponSprite(key: string): SaoItemAtlasSprite | null {
  const match = WEAPON_KEY_PATTERN.exec(key)

  if (!match) {
    return null
  }

  const location = Number(match[1])
  const itemIndex = Number(match[2])

  if (!hasVerifiedWeaponArtwork(location, itemIndex)) {
    return null
  }

  return {
    column: itemIndex - 1,
    row: location - 1,
    atlas: GUIDE_WEAPON_ATLAS,
  }
}

export function guideEntityIndex(key: string, fallbackIndex: number) {
  const match = /_(\d+)\.name$/.exec(key)

  return match ? Number(match[1]) : fallbackIndex
}

function hasVerifiedWeaponArtwork(location: number, itemIndex: number) {
  if (
    location < 1 ||
    location > GUIDE_WEAPON_ATLAS_ROWS ||
    itemIndex < 1 ||
    itemIndex > GUIDE_WEAPON_ATLAS_COLUMNS
  ) {
    return false
  }

  if (location >= 9) {
    return true
  }

  return EARLY_LOCATION_WEAPON_INDEXES[location]?.includes(itemIndex) ?? false
}
