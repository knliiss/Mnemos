import { describe, expect, it } from "vitest"
import {
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
        nickname: "Alpha",
        metric: "1,234 COIN",
        detail: "COIN",
      },
    ])
  })

  it("formats predictor win rate and bet detail", () => {
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
      metric: "62.5%",
      detail: "5/8 bets",
    })
  })

  it("formats drop totals with rarity breakdown", () => {
    const entries = presentDropEntries(
      [
        {
          rank: 3,
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
        recent: "latest",
      },
    )

    expect(entries[0]).toMatchObject({
      metric: "12 drops",
      detail: "7 mythical · 5 secret",
    })
  })
})
