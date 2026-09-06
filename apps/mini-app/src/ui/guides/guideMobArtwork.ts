import type { CSSProperties } from "react"

const MOB_KEY_PATTERN = /^sao\.mob\.mob(\d+)_(\d+)\.name$/
const ATLAS_COLUMNS = 14
const DISPLAY_CELL_SIZE = 64
const ROWS_PER_STRIP = 2

const MOB_COUNTS: Record<number, number> = {
  1: 4,
  2: 4,
  3: 4,
  4: 4,
  5: 4,
  6: 4,
  7: 4,
  8: 5,
  9: 6,
  10: 7,
  11: 7,
  12: 7,
  13: 8,
  14: 7,
  15: 7,
  16: 7,
  17: 7,
  18: 7,
  19: 7,
  20: 7,
  21: 7,
  22: 7,
  23: 7,
  24: 7,
  25: 7,
}

export function guideMobArtworkStyle(key: string): CSSProperties | undefined {
  const match = MOB_KEY_PATTERN.exec(key)

  if (!match) {
    return undefined
  }

  const locationId = Number(match[1])
  const mobIndex = Number(match[2])
  const locationMobCount = MOB_COUNTS[locationId]

  if (!locationMobCount || mobIndex < 1 || mobIndex > locationMobCount) {
    return undefined
  }

  const atlasIndex = mobAtlasIndex(locationId, mobIndex)
  const globalRow = Math.floor(atlasIndex / ATLAS_COLUMNS)
  const column = atlasIndex % ATLAS_COLUMNS
  const stripIndex = Math.floor(globalRow / ROWS_PER_STRIP) + 1
  const stripRow = globalRow % ROWS_PER_STRIP
  const stripRows = stripIndex === 6 ? 1 : ROWS_PER_STRIP

  return {
    backgroundImage: `url(/guides/entities/mob-model-atlas-${stripIndex}.webp)`,
    backgroundPosition: `${-column * DISPLAY_CELL_SIZE}px ${-stripRow * DISPLAY_CELL_SIZE}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${ATLAS_COLUMNS * DISPLAY_CELL_SIZE}px ${stripRows * DISPLAY_CELL_SIZE}px`,
  }
}

function mobAtlasIndex(locationId: number, mobIndex: number) {
  let offset = 0

  for (let id = 1; id < locationId; id += 1) {
    offset += MOB_COUNTS[id] ?? 0
  }

  return offset + mobIndex - 1
}
