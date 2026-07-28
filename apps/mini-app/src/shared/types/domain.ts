export type Language = "RU" | "UK" | "EN"
export type ThemeMode = "dark" | "light" | "system"
export type BetStatus = "ACTIVE" | "WON" | "LOST" | "CANCELLED" | "PENDING" | "REFUND"

export interface User {
  id: string
  username: string
  createdAt: string
  updatedAt: string
  activeAccountId: string | null
  localizationLanguage: Language
}

export interface Role {
  id: string
  name: string
  color: string
  permissions: string[]
}

export interface Account {
  id: string
  ownerUserId: string
  cristalixUuid: string
  cristalixNickname: string
  createdAt?: string
  updatedAt?: string
  role: Role
  online: boolean
  streak: number
  coins: number
  primes: number
  likes: number
  comments: number
  members: number
  about: string
  personalization: Personalization
}

export interface AvailableAccount {
  id: string
  ownerUserId: string
  cristalixUuid: string
  cristalixNickname: string
}

export interface Personalization {
  prefix: string
  suffix: string
  nicknameColor: string
  badge: string
}

export interface ItemEvent {
  id: string
  name: string
  type: "item" | "event"
  accountName: string
  happenedAt: string
  description: string
}

export type ItemKind = "weapon" | "wing" | "orb" | "totem" | "artifact"

export interface AccountProfile {
  id: string
  ownerUserId: string
  cristalixUuid: string
  cristalixNickname: string
  online: boolean
  role: Role
  likes: number
  members: number
  comments: number
  about: string
  balance: {
    coins: number
    primes: number
  }
  streak: number
  dailyRewardAvailable: boolean
  personalization: {
    prefix: string
    emoji?: string
    nicknameColor: string
    plaque?: string
  }
}

export interface ItemActivity {
  id: string
  title: string
  kind: ItemKind
  winner: string
  accountId: string
  createdAt: string
}

export interface EventCard {
  id: string
  title: string
  type: "EVENT" | "RACE"
  status: BetStatus
  chance?: number
  endsAt: string
  pool: number
  participants: number
  myBet?: number
  multiplier?: number
}

export interface Page<T> {
  items: T[]
  page: number
  size: number
  total: number
  hasMore: boolean
}

export interface Transaction {
  id: string
  label: string
  amount: number
  currency: "COINS" | "PRIMES"
  createdAt: string
}

export interface Comment {
  id: string
  author: string
  text: string
  likes: number
  createdAt: string
  replies?: Comment[]
}

export interface Bet {
  id: string
  title: string
  mode: "event" | "race"
  status: BetStatus
  chance?: number
  pool: number
  participants: number
  endsAt: string
  myBet?: number
  multiplier?: number
}

export interface Notification {
  id: string
  title: string
  body: string
  read: boolean
  createdAt?: string
}

export interface NewsItem {
  id: string
  title: string
  summary: string
  createdAt: string
}

export interface Proposal {
  id: string
  title: string
  description: string
  author: string
  votes: number
}
