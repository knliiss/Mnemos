export function formatGlobalEventRemainingTime(endsAt: string, now: number): string {
  const remainingSeconds = Math.max(0, Math.ceil((Date.parse(endsAt) - now) / 1_000))
  const hours = Math.floor(remainingSeconds / 3_600)
  const minutes = Math.floor((remainingSeconds % 3_600) / 60)
  const seconds = remainingSeconds % 60

  if (hours > 0) {
    return `${hours}:${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`
  }

  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`
}
