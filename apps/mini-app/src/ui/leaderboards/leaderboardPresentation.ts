import type {
  CoinLeaderboardEntry,
  DropLeaderboardEntry,
  DropLeaderboardSort,
  PredictorLeaderboardEntry,
  PredictorLeaderboardSort,
} from "@/shared/api/leaderboardApi"

export type LeaderboardDisplayEntry = {
  rank: number
  nickname: string
  metric: string
  detail: string
}

export function presentDropEntries(
  entries: DropLeaderboardEntry[],
  sort: DropLeaderboardSort,
  locale: string,
  labels: {
    total: string
    mythical: string
    secret: string
    recent: string
  },
): LeaderboardDisplayEntry[] {
  const number = new Intl.NumberFormat(locale)
  const date = new Intl.DateTimeFormat(locale, {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  })

  return entries.map((entry) => {
    const metric =
      sort === "MYTHICAL"
        ? `${number.format(entry.mythicalDrops)} ${labels.mythical}`
        : sort === "SECRET"
          ? `${number.format(entry.secretDrops)} ${labels.secret}`
          : sort === "RECENT"
            ? date.format(new Date(entry.lastDropAt))
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
      nickname: entry.nickname,
      metric,
      detail: `${number.format(entry.wins)}/${number.format(entry.bets)} ${labels.bets}`,
    }
  })
}
