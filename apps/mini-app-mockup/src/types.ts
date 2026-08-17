export type RootPage = "home" | "events" | "services" | "profile"

export type Page =
  | RootPage
  | "event-detail"
  | "rewards"
  | "bets"
  | "bet-detail"
  | "wallet"
  | "personalization"
  | "subscriptions"
  | "notifications"
  | "social"
  | "settings"
  | "account"
  | "news"
  | "feedback"

export type Currency = "coin" | "prime"
export type Scope = "account" | "user"

export type MockAccount = {
  id: string
  nickname: string
  shortId: string
  skinUrl: string
  coinBalance: number
  primeBalance: number
  streak: number
  role: string
}

export type MockEvent = {
  id: string
  type: "Dungeon" | "Global" | "Raid" | "Booster" | "Item drop"
  title: string
  subtitle: string
  time: string
  relative: string
  status: "live" | "upcoming" | "quiet"
  meta?: string
}

export type MockTransaction = {
  id: string
  currency: Currency
  amount: number
  source: string
  time: string
}

export type MockBet = {
  id: string
  kind: "event" | "race"
  title: string
  subtitle: string
  closesAt: string
  yesPercent: number
  noPercent: number
  pot: number
  status: "open" | "locked" | "settled"
}

export type MockPack = {
  id: string
  name: string
  author: string
  scope: Scope
  price: number
  owned: boolean
  active: boolean
  preview: "sage" | "graphite" | "signal" | "paper"
}

export type MockNotification = {
  id: string
  title: string
  body: string
  time: string
  unread: boolean
  kind: "event" | "reward" | "wallet" | "social"
}
