import type { LocalizationLanguage, RootRoute, Route } from "../model"

export const rootItems: RootRoute[] = [
  "bets",
  "events",
  "account",
  "search",
  "personalization-store",
]

export const rootNavigationKeys: Record<RootRoute, string> = {
  bets: "appShell.navigation.bets",
  events: "appShell.navigation.events",
  account: "appShell.navigation.home",
  search: "appShell.navigation.search",
  "personalization-store": "appShell.navigation.store",
}

export const localizationOptions: Array<{
  value: LocalizationLanguage
  short: string
  label: string
  htmlLang: string
  numberLocale: string
}> = [
  {
    value: "UK",
    short: "UA",
    label: "Українська",
    htmlLang: "uk",
    numberLocale: "uk-UA",
  },
  {
    value: "RU",
    short: "RU",
    label: "Русский",
    htmlLang: "ru",
    numberLocale: "ru-RU",
  },
  {
    value: "EN",
    short: "EN",
    label: "English",
    htmlLang: "en",
    numberLocale: "en-US",
  },
]

export function routeKey(route: Route) {
  return `${route.id}:${JSON.stringify(route.params ?? {})}`
}

export function canRenderWithoutActiveAccount(route: Route) {
  return (
    route.id === "leaderboards" ||
    route.id === "sao-realms" ||
    route.id === "user-wallet" ||
    route.id === "user-personalization" ||
    route.id === "personalization-store" ||
    route.id === "personalization-store-category" ||
    route.id === "personalization-detail" ||
    (route.id === "personalization-owned" && route.params?.target !== "ACCOUNT")
  )
}
