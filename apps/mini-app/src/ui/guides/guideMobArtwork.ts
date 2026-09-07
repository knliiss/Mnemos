import type { CSSProperties } from "react"

const MOB_KEY_PATTERN = /^sao\.mob\.mob(\d+)_(\d+)\.name$/
const MOB_SOURCE_BASE_URL = "https://storage.c7x.dev/butowka/sao/global/img/mob"
const MISSING_SOURCE_ARTWORK = new Set(["17_7"])
const SOURCE_URL_OVERRIDES: Record<string, string> = {
  "13_2": `${MOB_SOURCE_BASE_URL}/mob12_2.png`,
}

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
  26: 7,
  27: 7,
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

  const sourceId = `${locationId}_${mobIndex}`

  if (MISSING_SOURCE_ARTWORK.has(sourceId)) {
    return undefined
  }

  const sourceUrl =
    SOURCE_URL_OVERRIDES[sourceId] ?? `${MOB_SOURCE_BASE_URL}/mob${locationId}_${mobIndex}.png`

  return {
    backgroundImage: `url(${sourceUrl})`,
    backgroundPosition: "center",
    backgroundRepeat: "no-repeat",
    backgroundSize: "contain",
  }
}
