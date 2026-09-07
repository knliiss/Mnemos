import type { CSSProperties } from "react"

const LOCATION_ATLAS_URL = "/guides/locations/location-atlas.webp"
const LOCATION_IDS = [-1, 0, ...Array.from({ length: 28 }, (_, index) => index + 1)]
const ATLAS_COLUMNS = 5
const ATLAS_ROWS = 6

export function guideLocationArtworkStyle(locationId: number): CSSProperties | undefined {
  const index = LOCATION_IDS.indexOf(locationId)

  if (index < 0) {
    return undefined
  }

  const column = index % ATLAS_COLUMNS
  const row = Math.floor(index / ATLAS_COLUMNS)
  const horizontalPosition = (column / (ATLAS_COLUMNS - 1)) * 100
  const verticalPosition = (row / (ATLAS_ROWS - 1)) * 100

  return {
    backgroundImage: `url(${LOCATION_ATLAS_URL})`,
    backgroundPosition: `${horizontalPosition}% ${verticalPosition}%`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${ATLAS_COLUMNS * 100}% ${ATLAS_ROWS * 100}%`,
  }
}
