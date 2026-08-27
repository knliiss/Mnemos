import { api } from "@/shared/api/httpClient"

export type DropLeaderboardSort = "TOTAL" | "MYTHICAL" | "SECRET"
export type DropLeaderboardDirection = "ASC" | "DESC"
export type PredictorLeaderboardSort = "PROFIT" | "WIN_RATE" | "WINS" | "TURNOVER"
export type LeaderboardItemType = "SWORD" | "AURA" | "PET" | "BOOK" | "JEWELRY"
export type LeaderboardItemRarity = "MYTHICAL" | "SECRET"

export type DropLeaderboardEntry = {
  rank: number
  playerId: string
  nickname: string
  totalDrops: number
  mythicalDrops: number
  secretDrops: number
  lastDropAt: string
}

export type CoinLeaderboardEntry = {
  rank: number
  accountId: string
  nickname: string
  amount: number
}

export type BoosterLeaderboardEntry = {
  rank: number
  playerId: string
  nickname: string
  totalBoosters: number
  lastActivatedAt: string
}

export type PredictorLeaderboardEntry = {
  rank: number
  accountId: string
  nickname: string
  bets: number
  wins: number
  winRate: number
  profit: number
  turnover: number
}

export type DropLeaderboardQuery = {
  wipeId?: string | null
  rarity?: LeaderboardItemRarity | null
  itemType?: LeaderboardItemType | null
  itemName?: string
  nickname?: string
  sort?: DropLeaderboardSort
  direction?: DropLeaderboardDirection
  limit?: number
}

export type BoosterLeaderboardQuery = {
  wipeId?: string | null
  limit?: number
}

export type PredictorLeaderboardQuery = {
  wipeId?: string | null
  sort?: PredictorLeaderboardSort
  minimumSample?: number
  limit?: number
}

export async function getDropLeaderboard(
  query: DropLeaderboardQuery,
): Promise<DropLeaderboardEntry[]> {
  const params = new URLSearchParams()

  if (query.wipeId) {
    params.set("wipeId", query.wipeId)
  }

  if (query.rarity) {
    params.set("rarity", query.rarity)
  }

  if (query.itemType) {
    params.set("itemType", query.itemType)
  }

  if (query.itemName?.trim()) {
    params.set("itemName", query.itemName.trim())
  }

  if (query.nickname?.trim()) {
    params.set("nickname", query.nickname.trim())
  }

  params.set("sort", query.sort ?? "TOTAL")
  params.set("direction", query.direction ?? "DESC")
  params.set("limit", String(query.limit ?? 50))

  return api<DropLeaderboardEntry[]>(`/api/v1/events/leaderboards/drops?${params}`)
}

export async function getCoinLeaderboard(limit = 50): Promise<CoinLeaderboardEntry[]> {
  const params = new URLSearchParams({ limit: String(limit) })

  return api<CoinLeaderboardEntry[]>(`/api/v1/wallet/leaderboards/coins?${params}`)
}

export async function getBoosterLeaderboard(
  query: BoosterLeaderboardQuery,
): Promise<BoosterLeaderboardEntry[]> {
  const params = new URLSearchParams({ limit: String(query.limit ?? 50) })

  if (query.wipeId) {
    params.set("wipeId", query.wipeId)
  }

  return api<BoosterLeaderboardEntry[]>(`/api/v1/events/leaderboards/boosters?${params}`)
}

export async function getPredictorLeaderboard(
  query: PredictorLeaderboardQuery,
): Promise<PredictorLeaderboardEntry[]> {
  const params = new URLSearchParams()

  if (query.wipeId) {
    params.set("wipeId", query.wipeId)
  }

  params.set("sort", query.sort ?? "PROFIT")
  params.set("minimumSample", String(query.minimumSample ?? 10))
  params.set("limit", String(query.limit ?? 50))

  return api<PredictorLeaderboardEntry[]>(`/api/v1/bets/leaderboards/predictors?${params}`)
}
