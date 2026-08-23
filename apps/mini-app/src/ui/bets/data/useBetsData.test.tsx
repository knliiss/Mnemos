// @vitest-environment jsdom

import { act, renderHook, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import {
  getEventBetRound,
  getItemRace,
  listEventBetRounds,
  listItemRaces,
  listMyEventBets,
  listMyItemRaceHistory,
  type EventBetResponse,
  type EventBetRoundResponse,
  type ItemRaceParticipantResponse,
  type ItemRaceResponse,
} from "@/shared/api/betApi"
import { useBetsData } from "./useBetsData"

vi.mock("@/shared/api/betApi", () => ({
  getEventBetRound: vi.fn(),
  getItemRace: vi.fn(),
  listEventBetRounds: vi.fn(),
  listItemRaces: vi.fn(),
  listMyEventBets: vi.fn(),
  listMyItemRaceHistory: vi.fn(),
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

function eventBet(roundId: string, id = `bet-${roundId}`): EventBetResponse {
  return {
    id,
    roundId,
    side: "YES",
    confirmedAmount: 500,
    pendingAmount: 0,
    status: "SETTLED",
  }
}

function round(
  id: string,
  myBet: EventBetResponse | null = null,
  status: EventBetRoundResponse["status"] = "OPEN",
): EventBetRoundResponse {
  return {
    id,
    eventType: "MOON",
    scheduledAt: "2026-08-21T12:00:00Z",
    closesAt: "2026-08-21T11:55:00Z",
    resolvesAt: "2026-08-21T12:05:00Z",
    yesOdds: 2,
    noOdds: 2,
    status,
    outcome: status === "SETTLED" ? "YES" : null,
    myBet,
  }
}

function position(
  accountId: string,
  raceId = "race",
  id = `position-${accountId}-${raceId}`,
): ItemRaceParticipantResponse {
  return {
    id,
    raceId,
    accountId,
    accountNickname: accountId,
    confirmedAmount: accountId === "account-a" ? 10 : 20,
    pendingAmount: 0,
    status: "ACTIVE",
  }
}

function race(
  accountId: string,
  id = "race",
  status: ItemRaceResponse["status"] = "REGISTRATION",
): ItemRaceResponse {
  return {
    id,
    itemName: "Test Item",
    itemType: "SWORD",
    registrationClosesAt: "2026-08-21T12:00:00Z",
    startsAt: "2026-08-21T13:00:00Z",
    expiresAt: "2026-08-21T14:00:00Z",
    entryAmount: 100,
    feePercent: 5,
    status,
    winnerAccountId: null,
    participants: [position(accountId, id)],
  }
}

describe("useBetsData", () => {
  beforeEach(() => {
    vi.mocked(getEventBetRound).mockReset()
    vi.mocked(getItemRace).mockReset()
    vi.mocked(listEventBetRounds).mockReset()
    vi.mocked(listItemRaces).mockReset()
    vi.mocked(listMyEventBets).mockReset().mockResolvedValue([])
    vi.mocked(listMyItemRaceHistory).mockReset().mockResolvedValue([])

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
      ({ accountId }: { accountId: string }) => useBetsData(accountId, refreshAccount),
      { initialProps: { accountId: "account-a" } },
    )

    rerender({ accountId: "account-b" })
    expect(result.current.positions).toEqual([])
    expect(result.current.eventHistory).toEqual([])
    expect(result.current.raceHistory).toEqual([])

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
      ({ accountId }: { accountId: string }) => useBetsData(accountId, refreshAccount),
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
    expect(result.current.eventHistory).toEqual([])
    expect(result.current.raceHistory).toEqual([])
  })

  it("loads bet data for every active account member", async () => {
    vi.mocked(listEventBetRounds).mockResolvedValue([])
    vi.mocked(listItemRaces).mockResolvedValue([])

    const refreshAccount = vi.fn().mockResolvedValue(undefined)
    const { result } = renderHook(() => useBetsData("account-a", refreshAccount))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(listEventBetRounds).toHaveBeenCalledTimes(1)
    expect(listItemRaces).toHaveBeenCalledTimes(1)
    expect(listMyEventBets).toHaveBeenCalledTimes(1)
    expect(listMyItemRaceHistory).toHaveBeenCalledTimes(1)
    expect(result.current.error).toBe(false)
  })

  it("keeps a joined active race visible when the current race list omits it", async () => {
    const joinedPosition = position("account-a", "race-joined", "position-joined")
    const joinedRace = race("account-a", "race-joined", "ACTIVE")

    joinedRace.participants = [joinedPosition]

    vi.mocked(listEventBetRounds).mockResolvedValue([])
    vi.mocked(listItemRaces).mockResolvedValue([])
    vi.mocked(listMyItemRaceHistory).mockResolvedValue([joinedPosition])
    vi.mocked(getItemRace).mockResolvedValue(joinedRace)

    const refreshAccount = vi.fn().mockResolvedValue(undefined)
    const { result } = renderHook(() => useBetsData("account-a", refreshAccount))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(result.current.races.map(({ id }) => id)).toEqual(["race-joined"])
    expect(result.current.positions.map(({ id }) => id)).toEqual(["position-joined"])
    expect(result.current.raceHistory.map(({ position: entry }) => entry.id)).toEqual([
      "position-joined",
    ])
  })

  it("hydrates historical parents only when they are missing from current data", async () => {
    const currentBet = eventBet("round-current", "bet-current")
    const historicalBet = eventBet("round-history", "bet-history")
    const currentRacePosition = position("account-a", "race-current", "position-current")
    const historicalRacePosition = position("account-a", "race-history", "position-history")
    const currentRound = round("round-current")
    const historicalRound = round("round-history", null, "SETTLED")
    const currentRace = race("account-a", "race-current")
    const historicalRace = race("account-a", "race-history", "SETTLED")

    currentRace.participants = [currentRacePosition]
    historicalRace.participants = [historicalRacePosition]

    vi.mocked(listEventBetRounds).mockResolvedValue([currentRound])
    vi.mocked(listItemRaces).mockResolvedValue([currentRace])
    vi.mocked(listMyEventBets).mockResolvedValue([currentBet, historicalBet])
    vi.mocked(listMyItemRaceHistory).mockResolvedValue([
      currentRacePosition,
      historicalRacePosition,
    ])
    vi.mocked(getEventBetRound).mockResolvedValue(historicalRound)
    vi.mocked(getItemRace).mockResolvedValue(historicalRace)

    const refreshAccount = vi.fn().mockResolvedValue(undefined)
    const { result } = renderHook(() => useBetsData("account-a", refreshAccount))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
    })

    expect(getEventBetRound).toHaveBeenCalledTimes(1)
    expect(getEventBetRound).toHaveBeenCalledWith("round-history")
    expect(getItemRace).toHaveBeenCalledTimes(1)
    expect(getItemRace).toHaveBeenCalledWith("race-history")
    expect(result.current.rounds[0].myBet?.id).toBe("bet-current")
    expect(result.current.eventHistory.map(({ bet }) => bet.id)).toEqual([
      "bet-current",
      "bet-history",
    ])
    expect(result.current.raceHistory.map(({ position: entry }) => entry.id)).toEqual([
      "position-current",
      "position-history",
    ])
  })
})
