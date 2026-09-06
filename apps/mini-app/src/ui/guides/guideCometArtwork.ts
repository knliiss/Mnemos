import type { CSSProperties } from "react"

const COMET_KEY_PATTERN = /^sao\.mob\.comet(\d+)_1\.name$/
const COMET_ATLAS_URL = "/guides/entities/comet-atlas.webp"
const FIRST_LOCATION = 11
const LAST_LOCATION = 28
const ATLAS_COLUMNS = 6
const ATLAS_ROWS = 3
const DISPLAY_CELL_SIZE = 92

export function guideCometArtworkStyle(key: string): CSSProperties | undefined {
  const match = COMET_KEY_PATTERN.exec(key)

  if (!match) {
    return undefined
  }

  const locationId = Number(match[1])

  if (locationId < FIRST_LOCATION || locationId > LAST_LOCATION) {
    return undefined
  }

  const itemIndex = locationId - FIRST_LOCATION
  const column = itemIndex % ATLAS_COLUMNS
  const row = Math.floor(itemIndex / ATLAS_COLUMNS)

  return {
    backgroundImage: `url(${COMET_ATLAS_URL})`,
    backgroundPosition: `${-column * DISPLAY_CELL_SIZE}px ${-row * DISPLAY_CELL_SIZE}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${ATLAS_COLUMNS * DISPLAY_CELL_SIZE}px ${ATLAS_ROWS * DISPLAY_CELL_SIZE}px`,
  }
}
