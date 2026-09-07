import type { CSSProperties } from "react"

const MOB_KEY_PATTERN = /^sao\.mob\.mob(\d+)_(\d+)\.name$/
const ATLAS_COLUMNS = 14
const CELLS_PER_FULL_SHEET = 28
const DISPLAY_CELL_SIZE = 64
const FIRST_LOCATION = 1
const LAST_LOCATION = 25

const SOURCE_MOB_COUNTS: Record<number, number> = {
  1: 4,
  2: 4,
  3: 4,
  4: 4,
  5: 4,
  6: 4,
  7: 4,
  8: 7,
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

const TOTAL_SOURCE_MOBS = Object.values(SOURCE_MOB_COUNTS).reduce(
  (total, count) => total + count,
  0,
)

export function guideMobArtworkStyle(key: string): CSSProperties | undefined {
  const match = MOB_KEY_PATTERN.exec(key)

  if (!match) {
    return undefined
  }

  const locationId = Number(match[1])
  const mobIndex = Number(match[2])
  const locationMobCount = SOURCE_MOB_COUNTS[locationId]

  if (
    locationId < FIRST_LOCATION ||
    locationId > LAST_LOCATION ||
    !locationMobCount ||
    mobIndex < 1 ||
    mobIndex > locationMobCount
  ) {
    return undefined
  }

  const flatIndex = sourceMobOffset(locationId) + mobIndex - 1
  const sheetIndex = Math.floor(flatIndex / CELLS_PER_FULL_SHEET) + 1
  const sheetCellIndex = flatIndex % CELLS_PER_FULL_SHEET
  const column = sheetCellIndex % ATLAS_COLUMNS
  const row = Math.floor(sheetCellIndex / ATLAS_COLUMNS)
  const sheetRows = sheetIndex === Math.ceil(TOTAL_SOURCE_MOBS / CELLS_PER_FULL_SHEET) ? 1 : 2

  return {
    backgroundImage: `url(/guides/entities/mob-model-atlas-${sheetIndex}.webp)`,
    backgroundPosition: `${-column * DISPLAY_CELL_SIZE}px ${-row * DISPLAY_CELL_SIZE}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${ATLAS_COLUMNS * DISPLAY_CELL_SIZE}px ${sheetRows * DISPLAY_CELL_SIZE}px`,
    imageRendering: "pixelated",
  }
}

function sourceMobOffset(locationId: number) {
  let offset = 0

  for (let location = FIRST_LOCATION; location < locationId; location += 1) {
    offset += SOURCE_MOB_COUNTS[location] ?? 0
  }

  return offset
}
