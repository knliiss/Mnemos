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
const NORMAL_CASES_PER_MINUTE = FIFTEEN_MINUTE_MULTIPLIER / 15
const EXPLOSION_NORMAL_MINUTES = 5
const EXPLOSION_EXPENSIVE_MINUTES = 20
const EXPLOSION_PRICE_MULTIPLIER = 1.3

export function guideCaseEconomy(price: string): GuideCaseEconomy | null {
  const parsed = parseCompactNumber(price)

  if (!parsed) {
    return null
  }

  const fifteenMinutesAndDarkness = parsed.value * FIFTEEN_MINUTE_MULTIPLIER
  const explosion =
    parsed.value *
    NORMAL_CASES_PER_MINUTE *
    (EXPLOSION_NORMAL_MINUTES + EXPLOSION_EXPENSIVE_MINUTES * EXPLOSION_PRICE_MULTIPLIER)

  return {
    fifteenMinutesAndDarkness: formatCompactNumber(fifteenMinutesAndDarkness),
    explosion: formatCompactNumber(explosion),
    oneHour: formatCompactNumber(parsed.value * 36_000),
    eightHours: formatCompactNumber(parsed.value * 288_000),
    twelveHours: formatCompactNumber(parsed.value * 432_000),
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

  const decimals = normalized >= 100 ? 0 : normalized >= 10 ? 1 : 2
  const rounded = normalized.toFixed(decimals).replace(/\.0+$|(?<=\.[0-9])0+$/, "")

  return `${rounded}${SUFFIXES[suffixIndex]}`
}
