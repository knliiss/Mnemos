// @vitest-environment jsdom

import { act, renderHook, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import type { BalanceResponse, DailyRewardStateResponse } from "@/shared/api/backendContracts"
import {
  claimDailyReward,
  getAccountBalances,
  getAccountMembers,
  getAccountRoles,
  getCurrentUserBalances,
  getDailyRewardState,
  restoreDailyReward,
} from "@/shared/api/productApi"
import { useAccountData } from "./useAccountData"

vi.mock("@/shared/api/productApi", () => ({
  claimDailyReward: vi.fn(),
  getAccountBalances: vi.fn(),
  getAccountMembers: vi.fn(),
  getAccountRoles: vi.fn(),
  getCurrentUserBalances: vi.fn(),
  getDailyRewardState: vi.fn(),
  restoreDailyReward: vi.fn(),
}))

const rewardState: DailyRewardStateResponse = {
  accountId: "account",
  currentStreak: 1,
  claimedToday: false,
  recoverable: false,
  recoverableStreak: 0,
  restoresRemaining: 2,
  nextCoinMin: 10,
  nextCoinMax: 20,
  primeChance: 0,
  rewardDate: "2026-08-20",
}

type Deferred<T> = {
  promise: Promise<T>
  resolve(value: T): void
}

function createDeferred<T>(): Deferred<T> {
  let resolve!: (value: T) => void

  const promise = new Promise<T>((promiseResolve) => {
    resolve = promiseResolve
  })

  return { promise, resolve }
}

describe("useAccountData", () => {
  beforeEach(() => {
    vi.mocked(getAccountMembers).mockResolvedValue([])
    vi.mocked(getAccountRoles).mockResolvedValue([])
    vi.mocked(getCurrentUserBalances).mockResolvedValue([])
    vi.mocked(getDailyRewardState).mockResolvedValue(rewardState)
    vi.mocked(claimDailyReward).mockReset()
    vi.mocked(restoreDailyReward).mockReset()

    vi.stubGlobal("requestAnimationFrame", (callback: FrameRequestCallback) => {
      callback(0)
      return 1
    })
    vi.stubGlobal("cancelAnimationFrame", vi.fn())
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  it("ignores a stale account response after the active account changes", async () => {
    const firstBalances = createDeferred<BalanceResponse[]>()
    const secondBalances = createDeferred<BalanceResponse[]>()

    vi.mocked(getAccountBalances).mockImplementation((accountId) => {
      return accountId === "account-a" ? firstBalances.promise : secondBalances.promise
    })

    const { result, rerender } = renderHook(
      ({ accountId }: { accountId: string }) => useAccountData(accountId),
      {
        initialProps: { accountId: "account-a" },
      },
    )

    rerender({ accountId: "account-b" })

    await act(async () => {
      secondBalances.resolve([{ currency: "COIN", amount: 20 }])
      await secondBalances.promise
    })

    await waitFor(() => {
      expect(result.current.accountCoin).toBe(20)
      expect(result.current.loadingAccount).toBe(false)
    })

    await act(async () => {
      firstBalances.resolve([{ currency: "COIN", amount: 10 }])
      await firstBalances.promise
    })

    expect(result.current.accountCoin).toBe(20)
    expect(result.current.loadingAccount).toBe(false)
  })
})
