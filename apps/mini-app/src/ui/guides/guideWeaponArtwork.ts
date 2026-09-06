import type { CSSProperties } from "react"

const WEAPON_KEY_PATTERN = /^sao\.item\.l(\d+)_(\d+)\.name$/
const WEAPON_ATLAS_URL = "/guides/entities/weapon-atlas.webp"
const ATLAS_COLUMNS = 12
const ATLAS_ROWS = 28
const DISPLAY_CELL_SIZE = 56

export function guideWeaponArtworkStyle(key: string): CSSProperties | undefined {
  const match = WEAPON_KEY_PATTERN.exec(key)

  if (!match) {
    return undefined
  }

  const location = Number(match[1])
  const itemIndex = Number(match[2])

  if (location < 1 || location > ATLAS_ROWS || itemIndex < 1 || itemIndex > ATLAS_COLUMNS) {
    return undefined
  }

  return {
    backgroundImage: `url(${WEAPON_ATLAS_URL})`,
    backgroundPosition: `${-(itemIndex - 1) * DISPLAY_CELL_SIZE}px ${-(location - 1) * DISPLAY_CELL_SIZE}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${ATLAS_COLUMNS * DISPLAY_CELL_SIZE}px ${ATLAS_ROWS * DISPLAY_CELL_SIZE}px`,
  }
}

export function guideEntityIndex(key: string, fallbackIndex: number) {
  const match = /_(\d+)\.name$/.exec(key)

  return match ? Number(match[1]) : fallbackIndex
}
