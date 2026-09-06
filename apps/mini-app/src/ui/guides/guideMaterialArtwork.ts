import type { CSSProperties } from "react"

const MATERIAL_KEY_PATTERN =
  /^sao\.item\.(weapon_upgrade_material|comet_shard)_(\d+)(?:_1)?\.name$/
const MATERIAL_ATLAS_URL = "/guides/entities/material-atlas.webp"
const FIRST_LOCATION = 11
const LAST_LOCATION = 28
const ATLAS_COLUMNS = 2
const ATLAS_ROWS = LAST_LOCATION - FIRST_LOCATION + 1
const DISPLAY_CELL_SIZE = 38

export function guideMaterialArtworkStyle(key: string): CSSProperties | undefined {
  const match = MATERIAL_KEY_PATTERN.exec(key)

  if (!match) {
    return undefined
  }

  const materialType = match[1]
  const location = Number(match[2])

  if (location < FIRST_LOCATION || location > LAST_LOCATION) {
    return undefined
  }

  const column = materialType === "weapon_upgrade_material" ? 0 : 1
  const row = location - FIRST_LOCATION

  return {
    backgroundImage: `url(${MATERIAL_ATLAS_URL})`,
    backgroundPosition: `${-column * DISPLAY_CELL_SIZE}px ${-row * DISPLAY_CELL_SIZE}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${ATLAS_COLUMNS * DISPLAY_CELL_SIZE}px ${ATLAS_ROWS * DISPLAY_CELL_SIZE}px`,
  }
}
