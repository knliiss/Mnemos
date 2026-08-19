export function formatOccurredAt(occurredAt: string, locale = "uk-UA") {
  const date = new Date(occurredAt)
  const diff = Date.now() - date.getTime()
  const minutes = Math.max(1, Math.floor(diff / 60_000))
  const relative = new Intl.RelativeTimeFormat(locale, { numeric: "always" })

  if (minutes < 60) {
    return relative.format(-minutes, "minute")
  }

  if (minutes < 24 * 60) {
    return relative.format(-Math.floor(minutes / 60), "hour")
  }

  return new Intl.DateTimeFormat(locale, {
    day: "numeric",
    month: "short",
    year: date.getFullYear() === new Date().getFullYear() ? undefined : "numeric",
  }).format(date)
}
