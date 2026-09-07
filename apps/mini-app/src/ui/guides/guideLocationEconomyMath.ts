export type GuideCaseEconomy = {
  fifteenMinutesAndDarkness: string
  explosion: string
  oneHour: string
  eightHours: string
  twelveHours: string
}

const SUFFIXES = [
  "",
  "K",
  "M",
  "B",
  "T",
  "Qa",
  "Qi",
  "Sx",
  "Sp",
  "Oc",
  "No",
  "Dc",
  "Ud",
  "Dd",
  "Td",
  "Qad",
  "Qid",
  "Sxd",
  "Spd",
  "Ocd",
  "Nod",
  "Vg",
  "Uvg",
  "Dvg",
  "Tvg",
  "Qavg",
  "Qivg",
  "Sxvg",
  "Spvg",
  "Ocvg",
  "Nov",
  "Tt",
  "Unt",
  "Dtt",
  "Ttt",
  "Qut",
] as const

const FIFTEEN_MINUTE_MULTIPLIER = 9_000
const ONE_HOUR_MULTIPLIER = 36_000
const EIGHT_HOUR_MULTIPLIER = 288_000
const TWELVE_HOUR_MULTIPLIER = 432_000

// The detailed legacy SAO reference consistently used 2,250x for Darkness and
// 3,675x for Darkness + Explosion. Preserve that 49/30 relation after moving
// the current 15-minute/Darkness baseline to 9,000x instead of inventing a new
// event-price formula from tooltip wording.
const LEGACY_DARKNESS_MULTIPLIER = 2_250
const LEGACY_DARKNESS_AND_EXPLOSION_MULTIPLIER = 3_675
const EXPLOSION_MULTIPLIER =
  FIFTEEN_MINUTE_MULTIPLIER *
  (LEGACY_DARKNESS_AND_EXPLOSION_MULTIPLIER / LEGACY_DARKNESS_MULTIPLIER)

export function guideCaseEconomy(price: string): GuideCaseEconomy | null {
  const parsed = parseCompactNumber(price)

  if (!parsed) {
    return null
  }

  return {
    fifteenMinutesAndDarkness: formatCompactNumber(parsed.value * FIFTEEN_MINUTE_MULTIPLIER),
    explosion: formatCompactNumber(parsed.value * EXPLOSION_MULTIPLIER),
    oneHour: formatCompactNumber(parsed.value * ONE_HOUR_MULTIPLIER),
    eightHours: formatCompactNumber(parsed.value * EIGHT_HOUR_MULTIPLIER),
    twelveHours: formatCompactNumber(parsed.value * TWELVE_HOUR_MULTIPLIER),
  }
}

function parseCompactNumber(value: string) {
  const match = /^([0-9]+(?:\.[0-9]+)?)([A-Za-z]*)$/.exec(value.trim())

  if (!match) {
    return null
  }

  const suffixIndex = SUFFIXES.indexOf(match[2] as (typeof SUFFIXES)[number])

  if (suffixIndex < 0) {
    return null
  }

  return {
    value: Number(match[1]) * 1000 ** suffixIndex,
  }
}

function formatCompactNumber(value: number) {
  if (!Number.isFinite(value) || value <= 0) {
    return "0"
  }

  let suffixIndex = 0
  let normalized = value

  while (normalized >= 1000 && suffixIndex < SUFFIXES.length - 1) {
    normalized /= 1000
    suffixIndex += 1
  }

  const decimals = normalized >= 10 ? 1 : 2
  const rounded = normalized.toFixed(decimals).replace(/\.0+$|(?<=\.[0-9])0+$/, "")

  return `${rounded}${SUFFIXES[suffixIndex]}`
}
