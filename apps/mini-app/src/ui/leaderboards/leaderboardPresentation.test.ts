import { describe, expect, it } from "vitest"
import {
  orderDropLeaderboardEntries,
  presentCoinEntries,
  presentDropEntries,
  presentPredictorEntries,
} from "./leaderboardPresentation"

describe("leaderboard presentation", () => {
  it("formats coin leaderboard entries without changing ranking", () => {
    const entries = presentCoinEntries(
      [
        {
          rank: 2,
          accountId: "account-id",
          nickname: "Alpha",
          amount: 1234,
        },
      ],
      "en-US",
      "COIN",
    )

    expect(entries).toEqual([
      {
        rank: 2,
        accountId: "account-id",
        nickname: "Alpha",
        metric: "1,234 COIN",
        detail: "COIN",
      },
    ])
  })

  it("formats predictor win rate and keeps the profile account id", () => {
    const entries = presentPredictorEntries(
      [
        {
          rank: 1,
          accountId: "account-id",
          nickname: "Predictor",
          bets: 8,
          wins: 5,
          winRate: 0.625,
          profit: 240,
          turnover: 1200,
        },
      ],
      "WIN_RATE",
      "en-US",
      {
        bets: "bets",
        wins: "wins",
        profit: "COIN",
        turnover: "COIN",
      },
    )

    expect(entries[0]).toMatchObject({
      accountId: "account-id",
      metric: "62.5%",
      detail: "5/8 bets",
    })
  })

  it("formats drop totals with rarity breakdown", () => {
    const entries = presentDropEntries(
      [
        {
          rank: 3,
          playerId: "player-dropper",
          nickname: "Dropper",
          totalDrops: 12,
          mythicalDrops: 7,
          secretDrops: 5,
          lastDropAt: "2026-08-24T10:00:00Z",
        },
      ],
      "TOTAL",
      "en-US",
      {
        total: "drops",
        mythical: "mythical",
        secret: "secret",
      },
    )

    expect(entries[0]).toMatchObject({
      metric: "12 drops",
      detail: "7 mythical · 5 secret",
    })
  })

  it("orders drop entries locally in both directions and rebuilds display ranks", () => {
    const source = [
      {
        rank: 1,
        playerId: "player-middle",
        nickname: "Middle",
        totalDrops: 10,
        mythicalDrops: 4,
        secretDrops: 6,
        lastDropAt: "2026-08-24T10:00:00Z",
      },
      {
        rank: 2,
        playerId: "player-high",
        nickname: "High",
        totalDrops: 13,
        mythicalDrops: 8,
        secretDrops: 5,
        lastDropAt: "2026-08-24T10:01:00Z",
      },
      {
        rank: 3,
        playerId: "player-low",
        nickname: "Low",
        totalDrops: 7,
        mythicalDrops: 2,
        secretDrops: 5,
        lastDropAt: "2026-08-24T10:02:00Z",
      },
    ]

    const descending = orderDropLeaderboardEntries(source, "MYTHICAL", "DESC")
    const ascending = orderDropLeaderboardEntries(source, "MYTHICAL", "ASC")

    expect(descending.map((entry) => [entry.rank, entry.nickname])).toEqual([
      [1, "High"],
      [2, "Middle"],
      [3, "Low"],
    ])
    expect(ascending.map((entry) => [entry.rank, entry.nickname])).toEqual([
      [1, "Low"],
      [2, "Middle"],
      [3, "High"],
    ])
  })
})
