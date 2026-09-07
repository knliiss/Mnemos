import type { CSSProperties } from "react"
import type { GuideNpcRole } from "./guideCatalog"

const NPC_ATLAS_URL = "/guides/entities/npc-atlas.webp"
const FIRST_LOCATION = 1
const LAST_LOCATION = 28
const ATLAS_COLUMNS = 2
const ATLAS_ROWS = LAST_LOCATION
const DISPLAY_CELL_SIZE = 76

export function guideNpcArtworkStyle(
  locationId: number,
  role: GuideNpcRole,
): CSSProperties | undefined {
  if (locationId < FIRST_LOCATION || locationId > LAST_LOCATION) {
    return undefined
  }

  const column = role === "RAID_JOIN" ? 0 : 1
  const row = locationId - FIRST_LOCATION

  return {
    backgroundImage: `url(${NPC_ATLAS_URL})`,
    backgroundPosition: `${-column * DISPLAY_CELL_SIZE}px ${-row * DISPLAY_CELL_SIZE}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${ATLAS_COLUMNS * DISPLAY_CELL_SIZE}px ${ATLAS_ROWS * DISPLAY_CELL_SIZE}px`,
    imageRendering: "pixelated",
  }
}
