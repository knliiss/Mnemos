import type { CSSProperties } from "react"
import type { GuideNpcRole } from "./guideCatalog"

const FIRST_LOCATION = 1
const LAST_LOCATION = 28
const LAST_LOCATION_WITH_SPECIFIC_QUEST_ICON = 26
const ATLAS_COLUMNS = 2
const ROWS_PER_STRIP = 7
const DISPLAY_CELL_SIZE = 76

export function guideNpcArtworkStyle(
  locationId: number,
  role: GuideNpcRole,
): CSSProperties | undefined {
  if (locationId < FIRST_LOCATION || locationId > LAST_LOCATION) {
    return undefined
  }

  if (role === "QUEST" && locationId > LAST_LOCATION_WITH_SPECIFIC_QUEST_ICON) {
    return undefined
  }

  const locationOffset = locationId - FIRST_LOCATION
  const stripIndex = Math.floor(locationOffset / ROWS_PER_STRIP) + 1
  const stripRow = locationOffset % ROWS_PER_STRIP
  const column = role === "RAID_JOIN" ? 0 : 1

  return {
    backgroundImage: `url(/guides/entities/npc-atlas-${stripIndex}.webp)`,
    backgroundPosition: `${-column * DISPLAY_CELL_SIZE}px ${-stripRow * DISPLAY_CELL_SIZE}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${ATLAS_COLUMNS * DISPLAY_CELL_SIZE}px ${ROWS_PER_STRIP * DISPLAY_CELL_SIZE}px`,
  }
}
