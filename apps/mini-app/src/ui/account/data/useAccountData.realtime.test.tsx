// @vitest-environment jsdom

import { act, cleanup, renderHook, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import {
  getAccountMembers,
  getAccountRoles,
  getMemberRoles,
} from "@/shared/api/accountApi"
import { getDailyRewardState } from "@/shared/api/rewardApi"
import { getAccountBalances, getCurrentUserBalances } from "@/shared/api/walletApi"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import { useAccountData } from "./useAccountData"

vi.mock("@/shared/api/accountApi", () => ({
  getAccountMembers: vi.fn(),
  getAccountRoles: vi.fn(),
  getMemberRoles: vi.fn(),
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

const ACCOUNT_ID = "019c1129-ef54-7000-8000-000000000801"
const MEMBER_ID = "019c1129-ef54-7000-8000-000000000802"

beforeEach(() => {
  vi.mocked(getAccountMembers).mockReset().mockResolvedValue([])
  vi.mocked(getAccountRoles).mockReset().mockResolvedValue([])
  vi.mocked(getMemberRoles).mockReset().mockResolvedValue([])
  vi.mocked(getDailyRewardState).mockReset().mockResolvedValue({
    accountId: ACCOUNT_ID,
    currentStreak: 1,
    claimedToday: false,
    recoverable: false,
    recoverableStreak: 0,
    restoresRemaining: 2,
    nextCoinMin: 10,
    nextCoinMax: 20,
    primeChance: 0,
    rewardDate: "2026-09-05",
  })
  vi.mocked(getAccountBalances).mockReset().mockResolvedValue([])
  vi.mocked(getCurrentUserBalances).mockReset().mockResolvedValue([])

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

describe("useAccountData realtime lifecycle", () => {
  it("reloads active account data when an account lifecycle event arrives", async () => {
    const { result } = renderHook(() => useAccountData(ACCOUNT_ID, MEMBER_ID))

    await waitFor(() => {
      expect(result.current.loadingAccount).toBe(false)
      expect(getAccountMembers).toHaveBeenCalledTimes(1)
    })

    act(() => {
      realtimeEvents.publish({
        id: "019c1129-ef54-7000-8000-000000000803",
        type: "account.member.joined",
        occurredAt: "2026-09-05T00:00:00Z",
        operationId: null,
        payload: {
          accountId: ACCOUNT_ID,
          memberId: "019c1129-ef54-7000-8000-000000000804",
          userId: "019c1129-ef54-7000-8000-000000000805",
          change: "JOINED",
        },
      })
    })

    await waitFor(() => {
      expect(getAccountMembers).toHaveBeenCalledTimes(2)
      expect(getAccountRoles).toHaveBeenCalledTimes(2)
      expect(getMemberRoles).toHaveBeenCalledTimes(2)
    })
  })
})
