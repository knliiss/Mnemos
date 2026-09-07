import type { CSSProperties } from "react"

const WEAPON_KEY_PATTERN = /^sao\.item\.l(\d+)_(\d+)\.name$/
const WEAPON_ATLAS_URL = "/guides/entities/weapon-atlas.webp"
const FIRST_LOCATION = 1
const LAST_LOCATION = 28
const ATLAS_COLUMNS = 12
const ATLAS_ROWS = LAST_LOCATION
const DEFAULT_DISPLAY_CELL_SIZE = 56

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

export function guideWeaponArtworkStyle(
  key: string,
  displayCellSize = DEFAULT_DISPLAY_CELL_SIZE,
): CSSProperties | undefined {
  const match = WEAPON_KEY_PATTERN.exec(key)

  if (!match) {
    return undefined
  }

  const location = Number(match[1])
  const itemIndex = Number(match[2])

  if (!hasVerifiedWeaponArtwork(location, itemIndex)) {
    return undefined
  }

  return {
    backgroundImage: `url(${WEAPON_ATLAS_URL})`,
    backgroundPosition: `${-(itemIndex - 1) * displayCellSize}px ${-(location - 1) * displayCellSize}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${ATLAS_COLUMNS * displayCellSize}px ${ATLAS_ROWS * displayCellSize}px`,
    imageRendering: "pixelated",
  }
}

export function guideEntityIndex(key: string, fallbackIndex: number) {
  const match = /_(\d+)\.name$/.exec(key)

  return match ? Number(match[1]) : fallbackIndex
}

function hasVerifiedWeaponArtwork(location: number, itemIndex: number) {
  if (
    location < FIRST_LOCATION ||
    location > LAST_LOCATION ||
    itemIndex < 1 ||
    itemIndex > ATLAS_COLUMNS
  ) {
    return false
  }

  if (location >= 9) {
    return true
  }

  return EARLY_LOCATION_WEAPON_INDEXES[location]?.includes(itemIndex) ?? false
}
