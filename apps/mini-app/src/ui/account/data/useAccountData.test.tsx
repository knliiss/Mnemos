// @vitest-environment jsdom

import { act, cleanup, renderHook, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import type { BalanceResponse, DailyRewardStateResponse } from "@/shared/api/backendContracts"
import { getAccountMembers, getAccountRoles } from "@/shared/api/accountApi"
import { claimDailyReward, getDailyRewardState, restoreDailyReward } from "@/shared/api/rewardApi"
import { getAccountBalances, getCurrentUserBalances } from "@/shared/api/walletApi"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import { useAccountData } from "./useAccountData"

vi.mock("@/shared/api/accountApi", () => ({
  getAccountMembers: vi.fn(),
  getAccountRoles: vi.fn(),
}))

vi.mock("@/shared/api/rewardApi", () => ({
  claimDailyReward: vi.fn(),
  getDailyRewardState: vi.fn(),
  restoreDailyReward: vi.fn(),
}))

vi.mock("@/shared/api/walletApi", () => ({
  getAccountBalances: vi.fn(),
  getCurrentUserBalances: vi.fn(),
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
    vi.mocked(getCurrentUserBalances).mockReset()
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
    cleanup()
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
      ({ accountId }: { accountId: string }) => useAccountData(accountId, undefined),
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
      ({ accountId }: { accountId: string }) => useAccountData(accountId, undefined),
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
      ({ accountId }: { accountId: string }) => useAccountData(accountId, undefined),
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
      ({ accountId }: { accountId: string }) => useAccountData(accountId, undefined),
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

  it("refreshes PRIME when a completed user wallet transaction arrives", async () => {
    vi.mocked(getCurrentUserBalances)
      .mockResolvedValueOnce([{ currency: "PRIME", amount: 10 }])
      .mockResolvedValueOnce([{ currency: "PRIME", amount: 35 }])

    const { result } = renderHook(() => useAccountData(undefined, undefined))

    await waitFor(() => expect(result.current.prime).toBe(10))

    act(() => {
      realtimeEvents.publish({
        id: "019c1129-ef54-7000-8000-000000000060",
        type: "wallet.transaction.completed",
        occurredAt: "2026-08-22T17:00:00Z",
        operationId: null,
        payload: {
          requestId: "019c1129-ef54-7000-8000-000000000061",
          transactionId: "019c1129-ef54-7000-8000-000000000062",
          ownerType: "USER",
          ownerId: "019c1129-ef54-7000-8000-000000000063",
          currency: "PRIME",
          source: "ADMIN_ADJUSTMENT",
          sourceReferenceId: "019c1129-ef54-7000-8000-000000000064",
          amount: 25,
        },
      })
    })

    await waitFor(() => expect(result.current.prime).toBe(35))
    expect(getCurrentUserBalances).toHaveBeenCalledTimes(2)
  })

  it("refreshes COIN when a completed transaction arrives for the active account", async () => {
    vi.mocked(getAccountBalances)
      .mockResolvedValueOnce([{ currency: "COIN", amount: 100 }])
      .mockResolvedValueOnce([{ currency: "COIN", amount: 160 }])

    const { result } = renderHook(() => useAccountData("account-a", undefined))

    await waitFor(() => expect(result.current.accountCoin).toBe(100))

    act(() => {
      realtimeEvents.publish({
        id: "019c1129-ef54-7000-8000-000000000070",
        type: "wallet.transaction.completed",
        occurredAt: "2026-08-22T17:01:00Z",
        operationId: null,
        payload: {
          requestId: "019c1129-ef54-7000-8000-000000000071",
          transactionId: "019c1129-ef54-7000-8000-000000000072",
          ownerType: "ACCOUNT",
          ownerId: "account-a",
          currency: "COIN",
          source: "DAILY_REWARD",
          sourceReferenceId: "019c1129-ef54-7000-8000-000000000073",
          amount: 60,
        },
      })
    })

    await waitFor(() => expect(result.current.accountCoin).toBe(160))
    expect(getAccountBalances).toHaveBeenCalledTimes(2)
  })

  it("ignores completed wallet transactions for another account", async () => {
    vi.mocked(getAccountBalances).mockResolvedValue([{ currency: "COIN", amount: 100 }])

    const { result } = renderHook(() => useAccountData("account-a", undefined))

    await waitFor(() => expect(result.current.accountCoin).toBe(100))

    act(() => {
      realtimeEvents.publish({
        id: "019c1129-ef54-7000-8000-000000000080",
        type: "wallet.transaction.completed",
        occurredAt: "2026-08-22T17:02:00Z",
        operationId: null,
        payload: {
          requestId: "019c1129-ef54-7000-8000-000000000081",
          transactionId: "019c1129-ef54-7000-8000-000000000082",
          ownerType: "ACCOUNT",
          ownerId: "account-b",
          currency: "COIN",
          source: "ADMIN_ADJUSTMENT",
          sourceReferenceId: "019c1129-ef54-7000-8000-000000000083",
          amount: 60,
        },
      })
    })

    await act(async () => {
      await Promise.resolve()
    })

    expect(result.current.accountCoin).toBe(100)
    expect(getAccountBalances).toHaveBeenCalledTimes(1)
  })
})