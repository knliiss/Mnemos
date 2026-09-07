import type { CSSProperties } from "react"
import type { GuideNpcRole } from "./guideCatalog"

const FIRST_LOCATION = 1
const LAST_LOCATION = 28
const LOCATIONS_PER_SHEET = 4
const ATLAS_COLUMNS = 2
const ATLAS_ROWS = LOCATIONS_PER_SHEET
const DISPLAY_CELL_SIZE = 76

export function guideNpcArtworkStyle(
  locationId: number,
  role: GuideNpcRole,
): CSSProperties | undefined {
  if (locationId < FIRST_LOCATION || locationId > LAST_LOCATION) {
    return undefined
  }

  const sheetIndex = Math.floor((locationId - FIRST_LOCATION) / LOCATIONS_PER_SHEET) + 1
  const row = (locationId - FIRST_LOCATION) % LOCATIONS_PER_SHEET
  const column = role === "RAID_JOIN" ? 0 : 1

  return {
    backgroundImage: `url(/guides/entities/npc-atlas-${sheetIndex}.webp)`,
    backgroundPosition: `${-column * DISPLAY_CELL_SIZE}px ${-row * DISPLAY_CELL_SIZE}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${ATLAS_COLUMNS * DISPLAY_CELL_SIZE}px ${ATLAS_ROWS * DISPLAY_CELL_SIZE}px`,
    imageRendering: "pixelated",
  }
}
