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
    vi.mocked(getAccountBalances).mockReset()
    vi.mocked(getAccountBalances).mockResolvedValue([])
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

  it("does not expose the previous account after the new account load fails", async () => {
    vi.mocked(getAccountBalances)
      .mockResolvedValueOnce([{ currency: "COIN", amount: 10 }])
      .mockRejectedValueOnce(new Error("wallet unavailable"))

    const { result, rerender } = renderHook(
      ({ accountId }: { accountId: string }) => useAccountData(accountId),
      {
        initialProps: { accountId: "account-a" },
      },
    )

    await waitFor(() => {
      expect(result.current.accountCoin).toBe(10)
      expect(result.current.dailyReward).toEqual(rewardState)
    })

    rerender({ accountId: "account-b" })

    await waitFor(() => {
      expect(result.current.loadingAccount).toBe(false)
      expect(result.current.accountError).toBe(true)
    })

    expect(result.current.accountCoin).toBe(0)
    expect(result.current.members).toEqual([])
    expect(result.current.roles).toEqual([])
    expect(result.current.dailyReward).toBeNull()
  })

  it("does not refresh the previous account when a claim completes after a switch", async () => {
    const claim = createDeferred<Awaited<ReturnType<typeof claimDailyReward>>>()

    vi.mocked(getAccountBalances).mockImplementation(async (accountId) => [
      { currency: "COIN", amount: accountId === "account-a" ? 10 : 20 },
    ])
    vi.mocked(claimDailyReward).mockReturnValue(claim.promise)

    const { result, rerender } = renderHook(
      ({ accountId }: { accountId: string }) => useAccountData(accountId),
      { initialProps: { accountId: "account-a" } },
    )

    await waitFor(() => expect(result.current.accountCoin).toBe(10))

    let claimPromise!: ReturnType<typeof result.current.claimReward>
    act(() => {
      claimPromise = result.current.claimReward()
    })

    rerender({ accountId: "account-b" })
    await waitFor(() => expect(result.current.accountCoin).toBe(20))

    await act(async () => {
      claim.resolve({ streak: 2, coins: 99, primes: 1, primeChance: 0.1 })
      await expect(claimPromise).resolves.toBeNull()
    })

    expect(result.current.accountCoin).toBe(20)
    expect(getAccountBalances).toHaveBeenCalledTimes(2)
  })

  it("does not refresh the previous account when a restore completes after a switch", async () => {
    const restore = createDeferred<Awaited<ReturnType<typeof restoreDailyReward>>>()

    vi.mocked(getAccountBalances).mockImplementation(async (accountId) => [
      { currency: "COIN", amount: accountId === "account-a" ? 10 : 20 },
    ])
    vi.mocked(restoreDailyReward).mockReturnValue(restore.promise)

    const { result, rerender } = renderHook(
      ({ accountId }: { accountId: string }) => useAccountData(accountId),
      { initialProps: { accountId: "account-a" } },
    )

    await waitFor(() => expect(result.current.accountCoin).toBe(10))

    let restorePromise!: ReturnType<typeof result.current.restoreReward>
    act(() => {
      restorePromise = result.current.restoreReward()
    })

    rerender({ accountId: "account-b" })
    await waitFor(() => expect(result.current.accountCoin).toBe(20))

    await act(async () => {
      restore.resolve({ currentStreak: 1 })
      await restorePromise
    })

    expect(result.current.accountCoin).toBe(20)
    expect(getAccountBalances).toHaveBeenCalledTimes(2)
  })
})
