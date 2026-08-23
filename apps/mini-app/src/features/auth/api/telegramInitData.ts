const DEFAULT_MAXIMUM_AGE_SECONDS = 300
const ALLOWED_FUTURE_SKEW_SECONDS = 30

export type TelegramInitDataState = "fresh" | "missing-auth-date" | "invalid-auth-date" | "expired"

export function getTelegramInitDataState(
  initData: string,
  maximumAgeSeconds = getConfiguredMaximumAgeSeconds(),
  nowMilliseconds = Date.now(),
): TelegramInitDataState {
  const authDate = new URLSearchParams(initData).get("auth_date")

  if (!authDate) {
    return "missing-auth-date"
  }

  if (!/^\d+$/.test(authDate)) {
    return "invalid-auth-date"
  }

  const authTimestampSeconds = Number(authDate)

  if (!Number.isSafeInteger(authTimestampSeconds)) {
    return "invalid-auth-date"
  }

  const nowSeconds = Math.floor(nowMilliseconds / 1000)
  const ageSeconds = nowSeconds - authTimestampSeconds

  if (ageSeconds < -ALLOWED_FUTURE_SKEW_SECONDS || ageSeconds > maximumAgeSeconds) {
    return "expired"
  }

  return "fresh"
}

export function getTelegramUserId(initData: string): number | null {
  const rawUser = new URLSearchParams(initData).get("user")

  if (!rawUser) {
    return null
  }

  try {
    const user: unknown = JSON.parse(rawUser)

    if (!isRecord(user)) {
      return null
    }

    const userId = user.id

    if (typeof userId !== "number" || !Number.isSafeInteger(userId) || userId <= 0) {
      return null
    }

    return userId
  } catch {
    return null
  }
}

export function getConfiguredMaximumAgeSeconds(): number {
  const configured = Number(import.meta.env.VITE_TELEGRAM_INIT_DATA_MAX_AGE_SECONDS)

  if (Number.isFinite(configured) && configured > 0) {
    return Math.floor(configured)
  }

  return DEFAULT_MAXIMUM_AGE_SECONDS
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value)
}
