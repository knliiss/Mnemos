// @vitest-environment jsdom

import { act, renderHook, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import {
  getWalletBalance,
  listSanctions,
  listWalletAdjustments,
  searchIdentities,
  type IdentitySearchResponse,
} from "@/shared/api/moderationApi"
import { useModerationUser } from "./useModerationUser"

vi.mock("@/shared/api/moderationApi", () => ({
  createSanction: vi.fn(),
  createWalletAdjustment: vi.fn(),
  getWalletAdjustment: vi.fn(),
  getWalletBalance: vi.fn(),
  listSanctions: vi.fn(),
  listWalletAdjustments: vi.fn(),
  revokeSanction: vi.fn(),
  searchIdentities: vi.fn(),
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

function identity(userId: string): IdentitySearchResponse {
  return {
    userId,
    username: userId,
    telegramId: 1,
    accounts: [],
  }
}

function identityWithAccount(userId: string, accountId: string): IdentitySearchResponse {
  return {
    userId,
    username: userId,
    telegramId: 1,
    accounts: [
      {
        accountId,
        cristalixUuid: "019c1129-ef54-7000-8000-000000000401",
        cristalixNickname: "north",
        active: true,
      },
    ],
  }
}

describe("useModerationUser", () => {
  beforeEach(() => {
    vi.mocked(searchIdentities).mockReset()
    vi.mocked(listSanctions).mockReset()
    vi.mocked(listSanctions).mockResolvedValue([])
    vi.mocked(getWalletBalance).mockReset()
    vi.mocked(getWalletBalance).mockImplementation(async (targetType, targetId) => ({
      targetType,
      targetId,
      currency: targetType === "USER" ? "PRIME" : "COIN",
      amount: 0,
    }))
    vi.mocked(listWalletAdjustments).mockReset()
    vi.mocked(listWalletAdjustments).mockResolvedValue([])
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

  it("does not expose the previous identity when the next user load fails", async () => {
    vi.mocked(searchIdentities)
      .mockResolvedValueOnce([identity("user-a")])
      .mockRejectedValueOnce(new Error("network"))

    const { result, rerender } = renderHook(
      ({ userId }: { userId: string }) =>
        useModerationUser({
          userId,
          canAdjustWallet: false,
          canAdjustPrime: false,
        }),
      { initialProps: { userId: "user-a" } },
    )

    await waitFor(() => expect(result.current.identity?.userId).toBe("user-a"))

    rerender({ userId: "user-b" })

    await waitFor(() => expect(result.current.loadError).toBe(true))

    expect(result.current.identity).toBeNull()
    expect(result.current.sanctions).toEqual([])
    expect(result.current.loading).toBe(false)
  })

  it("ignores a late identity response from the previous user", async () => {
    const userA = createDeferred<IdentitySearchResponse[]>()

    vi.mocked(searchIdentities)
      .mockReturnValueOnce(userA.promise)
      .mockResolvedValueOnce([identity("user-b")])

    const { result, rerender } = renderHook(
      ({ userId }: { userId: string }) =>
        useModerationUser({
          userId,
          canAdjustWallet: false,
          canAdjustPrime: false,
        }),
      { initialProps: { userId: "user-a" } },
    )

    rerender({ userId: "user-b" })

    await waitFor(() => expect(result.current.identity?.userId).toBe("user-b"))

    await act(async () => {
      userA.resolve([identity("user-a")])
      await userA.promise
    })

    expect(result.current.identity?.userId).toBe("user-b")
    expect(result.current.loadError).toBe(false)
  })

  it("never requests the user PRIME wallet for an ADMIN", async () => {
    const accountId = "019c1129-ef54-7000-8000-000000000402"

    vi.mocked(searchIdentities).mockResolvedValueOnce([identityWithAccount("user-a", accountId)])
    vi.mocked(getWalletBalance).mockResolvedValueOnce({
      targetType: "ACCOUNT",
      targetId: accountId,
      currency: "COIN",
      amount: 725,
    })

    const { result } = renderHook(() =>
      useModerationUser({
        userId: "user-a",
        canAdjustWallet: true,
        canAdjustPrime: false,
      }),
    )

    await waitFor(() => expect(result.current.identity?.userId).toBe("user-a"))
    await waitFor(() => expect(getWalletBalance).toHaveBeenCalledWith("ACCOUNT", accountId))
    await waitFor(() => expect(result.current.balance?.amount).toBe(725))

    expect(result.current.walletTarget).toBe("ACCOUNT")
    expect(result.current.currency).toBe("COIN")
    expect(getWalletBalance).not.toHaveBeenCalledWith("USER", "user-a")
    expect(listWalletAdjustments).not.toHaveBeenCalledWith("USER", "user-a")
  })
})
