// @vitest-environment jsdom

import { act, renderHook, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import {
  listEventBetRounds,
  listItemRaces,
  type ItemRaceParticipantResponse,
  type ItemRaceResponse,
} from "@/shared/api/betApi"
import { useBetsData } from "./useBetsData"

vi.mock("@/shared/api/betApi", () => ({
  listEventBetRounds: vi.fn(),
  listItemRaces: vi.fn(),
}))

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

function position(accountId: string): ItemRaceParticipantResponse {
  return {
    id: `position-${accountId}`,
    raceId: "race",
    accountId,
    accountNickname: accountId,
    confirmedAmount: accountId === "account-a" ? 10 : 20,
    pendingAmount: 0,
    status: "CONFIRMED",
  }
}

function race(accountId: string): ItemRaceResponse {
  return {
    id: "race",
    itemName: "Test Item",
    itemType: "SWORD",
    registrationClosesAt: "2026-08-21T12:00:00Z",
    startsAt: "2026-08-21T13:00:00Z",
    expiresAt: "2026-08-21T14:00:00Z",
    entryAmount: 100,
    feePercent: 5,
    status: "REGISTRATION",
    winnerAccountId: null,
    participants: [position(accountId)],
  }
}

describe("useBetsData", () => {
  beforeEach(() => {
    vi.mocked(listEventBetRounds).mockReset()
    vi.mocked(listItemRaces).mockReset()
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

  it("hides account-specific positions immediately and ignores the previous account response", async () => {
    const accountARounds = createDeferred<Awaited<ReturnType<typeof listEventBetRounds>>>()
    const accountARaces = createDeferred<Awaited<ReturnType<typeof listItemRaces>>>()
    const accountBRounds = createDeferred<Awaited<ReturnType<typeof listEventBetRounds>>>()
    const accountBRaces = createDeferred<Awaited<ReturnType<typeof listItemRaces>>>()

    vi.mocked(listEventBetRounds)
      .mockReturnValueOnce(accountARounds.promise)
      .mockReturnValueOnce(accountBRounds.promise)
    vi.mocked(listItemRaces)
      .mockReturnValueOnce(accountARaces.promise)
      .mockReturnValueOnce(accountBRaces.promise)

    const refreshAccount = vi.fn().mockResolvedValue(undefined)
    const { result, rerender } = renderHook(
      ({ accountId }: { accountId: string }) => useBetsData(accountId, refreshAccount, true),
      { initialProps: { accountId: "account-a" } },
    )

    rerender({ accountId: "account-b" })
    expect(result.current.positions).toEqual([])

    await act(async () => {
      accountBRounds.resolve([])
      accountBRaces.resolve([race("account-b")])
      await Promise.all([accountBRounds.promise, accountBRaces.promise])
    })

    await waitFor(() => {
      expect(result.current.positions.map(({ accountId }) => accountId)).toEqual(["account-b"])
    })

    await act(async () => {
      accountARounds.resolve([])
      accountARaces.resolve([race("account-a")])
      await Promise.all([accountARounds.promise, accountARaces.promise])
    })

    expect(result.current.positions.map(({ accountId }) => accountId)).toEqual(["account-b"])
    expect(result.current.loading).toBe(false)
  })

  it("does not expose the previous account data when the new account load fails", async () => {
    vi.mocked(listEventBetRounds)
      .mockResolvedValueOnce([])
      .mockRejectedValueOnce(new Error("bets unavailable"))
    vi.mocked(listItemRaces)
      .mockResolvedValueOnce([race("account-a")])
      .mockResolvedValueOnce([race("account-b")])

    const refreshAccount = vi.fn().mockResolvedValue(undefined)
    const { result, rerender } = renderHook(
      ({ accountId }: { accountId: string }) => useBetsData(accountId, refreshAccount, true),
      { initialProps: { accountId: "account-a" } },
    )

    await waitFor(() => {
      expect(result.current.positions.map(({ accountId }) => accountId)).toEqual(["account-a"])
    })

    rerender({ accountId: "account-b" })

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.error).toBe(true)
    })

    expect(result.current.rounds).toEqual([])
    expect(result.current.races).toEqual([])
    expect(result.current.positions).toEqual([])
  })

  it("does not request bet data when the role cannot view bets", async () => {
    const refreshAccount = vi.fn().mockResolvedValue(undefined)
    const { result } = renderHook(() => useBetsData("account-a", refreshAccount, false))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(listEventBetRounds).not.toHaveBeenCalled()
    expect(listItemRaces).not.toHaveBeenCalled()
    expect(result.current.error).toBe(false)
    expect(result.current.rounds).toEqual([])
    expect(result.current.races).toEqual([])
    expect(result.current.positions).toEqual([])
  })
})
