import type { CSSProperties } from "react"

const WEAPON_KEY_PATTERN = /^sao\.item\.l(\d+)_(\d+)\.name$/
const FIRST_LOCATION = 1
const LAST_LOCATION = 28
const LOCATIONS_PER_SHEET = 4
const ATLAS_COLUMNS = 12
const ATLAS_ROWS = LOCATIONS_PER_SHEET
const DEFAULT_DISPLAY_CELL_SIZE = 56

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

  if (
    location < FIRST_LOCATION ||
    location > LAST_LOCATION ||
    itemIndex < 1 ||
    itemIndex > ATLAS_COLUMNS
  ) {
    return undefined
  }

  const sheetIndex = Math.floor((location - FIRST_LOCATION) / LOCATIONS_PER_SHEET) + 1
  const row = (location - FIRST_LOCATION) % LOCATIONS_PER_SHEET

  return {
    backgroundImage: `url(/guides/entities/weapon-atlas-${sheetIndex}.webp)`,
    backgroundPosition: `${-(itemIndex - 1) * displayCellSize}px ${-row * displayCellSize}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${ATLAS_COLUMNS * displayCellSize}px ${ATLAS_ROWS * displayCellSize}px`,
    imageRendering: "pixelated",
  }
}

export function guideEntityIndex(key: string, fallbackIndex: number) {
  const match = /_(\d+)\.name$/.exec(key)

  return match ? Number(match[1]) : fallbackIndex
}
