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

export function getConfiguredMaximumAgeSeconds(): number {
  const configured = Number(import.meta.env.VITE_TELEGRAM_INIT_DATA_MAX_AGE_SECONDS)

  if (Number.isFinite(configured) && configured > 0) {
    return Math.floor(configured)
  }

  return DEFAULT_MAXIMUM_AGE_SECONDS
}
