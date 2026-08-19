export function formatOccurredAt(occurredAt: string) {
  const date = new Date(occurredAt)
  const diff = Date.now() - date.getTime()
  const minutes = Math.max(1, Math.floor(diff / 60_000))

  if (minutes < 60) {
    return `${minutes} хв тому`
  }

  if (minutes < 24 * 60) {
    return `${Math.floor(minutes / 60)} год тому`
  }

  return new Intl.DateTimeFormat("uk-UA", {
    day: "numeric",
    month: "short",
    year: date.getFullYear() === new Date().getFullYear() ? undefined : "numeric",
  }).format(date)
}
