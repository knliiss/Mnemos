import type {
  CoinLeaderboardEntry,
  DropLeaderboardDirection,
  DropLeaderboardEntry,
  DropLeaderboardSort,
  PredictorLeaderboardEntry,
  PredictorLeaderboardSort,
} from "@/shared/api/leaderboardApi"

export type LeaderboardDisplayEntry = {
  rank: number
  accountId?: string
  nickname: string
  metric: string
  detail: string
}

function dropMetric(entry: DropLeaderboardEntry, sort: DropLeaderboardSort): number {
  if (sort === "MYTHICAL") {
    return entry.mythicalDrops
  }

  if (sort === "SECRET") {
    return entry.secretDrops
  }

  return entry.totalDrops
}

export function orderDropLeaderboardEntries(
  entries: DropLeaderboardEntry[],
  sort: DropLeaderboardSort,
  direction: DropLeaderboardDirection,
): DropLeaderboardEntry[] {
  const directionMultiplier = direction === "ASC" ? 1 : -1

  return [...entries]
    .sort((left, right) => {
      const metricDifference = dropMetric(left, sort) - dropMetric(right, sort)

      if (metricDifference !== 0) {
        return metricDifference * directionMultiplier
      }

      const totalDifference = right.totalDrops - left.totalDrops

      if (totalDifference !== 0) {
        return totalDifference
      }

      return left.nickname.localeCompare(right.nickname, undefined, { sensitivity: "base" })
    })
    .map((entry, index) => ({
      ...entry,
      rank: index + 1,
    }))
}

export function presentDropEntries(
  entries: DropLeaderboardEntry[],
  sort: DropLeaderboardSort,
  locale: string,
  labels: {
    total: string
    mythical: string
    secret: string
  },
): LeaderboardDisplayEntry[] {
  const number = new Intl.NumberFormat(locale)

  return entries.map((entry) => {
    const metric =
      sort === "MYTHICAL"
        ? `${number.format(entry.mythicalDrops)} ${labels.mythical}`
        : sort === "SECRET"
          ? `${number.format(entry.secretDrops)} ${labels.secret}`
          : `${number.format(entry.totalDrops)} ${labels.total}`

    return {
      rank: entry.rank,
      nickname: entry.nickname,
      metric,
      detail: `${number.format(entry.mythicalDrops)} ${labels.mythical} · ${number.format(entry.secretDrops)} ${labels.secret}`,
    }
  })
}

export function presentCoinEntries(
  entries: CoinLeaderboardEntry[],
  locale: string,
  coinLabel: string,
): LeaderboardDisplayEntry[] {
  const number = new Intl.NumberFormat(locale)

  return entries.map((entry) => ({
    rank: entry.rank,
    accountId: entry.accountId,
    nickname: entry.nickname,
    metric: `${number.format(entry.amount)} ${coinLabel}`,
    detail: coinLabel,
  }))
}

export function presentPredictorEntries(
  entries: PredictorLeaderboardEntry[],
  sort: PredictorLeaderboardSort,
  locale: string,
  labels: {
    bets: string
    wins: string
    profit: string
    turnover: string
  },
): LeaderboardDisplayEntry[] {
  const number = new Intl.NumberFormat(locale)
  const percent = new Intl.NumberFormat(locale, {
    style: "percent",
    maximumFractionDigits: 1,
  })

  return entries.map((entry) => {
    const metric =
      sort === "WIN_RATE"
        ? percent.format(entry.winRate)
        : sort === "WINS"
          ? `${number.format(entry.wins)} ${labels.wins}`
          : sort === "TURNOVER"
            ? `${number.format(entry.turnover)} ${labels.turnover}`
            : `${entry.profit >= 0 ? "+" : ""}${number.format(entry.profit)} ${labels.profit}`

    return {
      rank: entry.rank,
      accountId: entry.accountId,
      nickname: entry.nickname,
      metric,
      detail: `${number.format(entry.wins)}/${number.format(entry.bets)} ${labels.bets}`,
    }
  })
}
