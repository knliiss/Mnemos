export function formatModerationDate(value: string, language: string) {
  const locale = language.startsWith("ru")
    ? "ru-RU"
    : language.startsWith("en")
      ? "en-US"
      : "uk-UA"

  return new Intl.DateTimeFormat(locale, {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value))
}
