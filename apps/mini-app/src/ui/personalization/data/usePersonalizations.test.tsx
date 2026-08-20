// @vitest-environment jsdom

import { act, renderHook } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import "@/shared/i18n"
import {
  getPersonalization,
  getPurchaseOperation,
  listOwnedPersonalizations,
  listPersonalizations,
  purchasePersonalization,
  type PersonalizationResponse,
  type PurchasePersonalizationResponse,
} from "@/shared/api/personalizationApi"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import type { Personalization } from "../../model"
import { usePersonalizations } from "./usePersonalizations"

type ProductMock = {
  user: { id: string; username: string }
  activeAccount: { id: string } | null
  refreshAccount: () => Promise<void>
}

type Deferred<T> = {
  promise: Promise<T>
  resolve(value: T): void
}

const productState = vi.hoisted(() => ({ current: null as ProductMock | null }))
const showBackendError = vi.hoisted(() => vi.fn())

vi.mock("../../data/ProductDataProvider", () => ({
  useProductData: () => productState.current,
}))

vi.mock("../../components/errors/ErrorNotifications", () => ({
  useErrorNotifications: () => ({ showBackendError }),
}))

vi.mock("@/shared/api/personalizationApi", () => ({
  activatePersonalization: vi.fn(),
  deactivatePersonalization: vi.fn(),
  getPersonalization: vi.fn(),
  getPurchaseOperation: vi.fn(),
  listOwnedPersonalizations: vi.fn(),
  listPersonalizations: vi.fn(),
  purchasePersonalization: vi.fn(),
}))

const emptyPage = {
  items: [],
  page: 0,
  size: 100,
  totalItems: 0,
  totalPages: 0,
  hasNextPage: false,
}

const accountItem: Personalization = {
  id: "nick-style",
  type: "NICK_STYLE",
  target: "ACCOUNT",
  name: "Gradient",
  creator: "Creator",
  currency: "COIN",
  price: 10,
  status: "PUBLISHED",
  owned: false,
  active: false,
  preview: "Gradient",
}

const userItem: Personalization = {
  ...accountItem,
  id: "notification-pack",
  type: "NOTIFICATION_PACK",
  target: "USER",
  currency: "PRIME",
}

const accountResponse: PersonalizationResponse = {
  id: accountItem.id,
  creatorUserId: "creator-user",
  type: "NICK_STYLE",
  origin: "SYSTEM",
  visibility: "PUBLIC",
  name: accountItem.name,
  content: {
    colors: [
      { color: "#ffffff", position: 0 },
      { color: "#a0a0a0", position: 1 },
    ],
    angleDegrees: 90,
    glow: null,
  },
  price: {
    currency: "COIN",
    amount: 10,
  },
  status: "PUBLISHED",
  rejectionReason: null,
  createdAt: "2026-08-20T00:00:00Z",
  updatedAt: "2026-08-20T00:00:00Z",
}

function createDeferred<T>(): Deferred<T> {
  let resolve!: (value: T) => void
  const promise = new Promise<T>((promiseResolve) => {
    resolve = promiseResolve
  })

  return { promise, resolve }
}

async function flushPromises() {
  await Promise.resolve()
  await Promise.resolve()
  await Promise.resolve()
}

describe("usePersonalizations", () => {
  beforeEach(() => {
    vi.useFakeTimers()
    showBackendError.mockReset()
    productState.current = {
      user: { id: "user", username: "mnemos" },
      activeAccount: { id: "account-a" },
      refreshAccount: vi.fn().mockResolvedValue(undefined),
    }
    vi.mocked(listOwnedPersonalizations).mockResolvedValue(emptyPage)
    vi.mocked(listPersonalizations).mockResolvedValue(emptyPage)
    vi.mocked(getPersonalization).mockReset()
    vi.mocked(getPurchaseOperation).mockReset()
    vi.mocked(purchasePersonalization).mockReset()
    vi.stubGlobal("requestAnimationFrame", (callback: FrameRequestCallback) => {
      callback(0)
      return 1
    })
    vi.stubGlobal("cancelAnimationFrame", vi.fn())
  })

  afterEach(() => {
    vi.clearAllTimers()
    vi.useRealTimers()
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  it("scopes pending account purchases and reconciles only the exact operation id", async () => {
    vi.mocked(purchasePersonalization)
      .mockResolvedValueOnce({
        personalizationId: accountItem.id,
        operationId: "operation-a",
      })
      .mockResolvedValueOnce({
        personalizationId: accountItem.id,
        operationId: "operation-b",
      })

    const { result, rerender } = renderHook(() => usePersonalizations())

    await act(async () => {
      await result.current.purchase(accountItem)
    })
    expect(result.current.pendingPurchases).toEqual({
      [accountItem.id]: "operation-a",
    })

    const refreshAccountB = vi.fn().mockResolvedValue(undefined)
    productState.current = {
      ...productState.current!,
      activeAccount: { id: "account-b" },
      refreshAccount: refreshAccountB,
    }
    rerender()

    expect(result.current.pendingPurchases).toEqual({})

    await act(async () => {
      await result.current.purchase(accountItem)
    })
    expect(result.current.pendingPurchases).toEqual({
      [accountItem.id]: "operation-b",
    })

    await act(async () => {
      realtimeEvents.publish({
        id: "event-a",
        type: "personalization.purchase.completed",
        occurredAt: "2026-08-20T00:00:00Z",
        operationId: "operation-a",
        payload: {
          personalizationId: accountItem.id,
          status: "CONFIRMED",
          walletTransactionId: "transaction-a",
          failureReason: null,
        },
      })
      await Promise.resolve()
    })

    expect(result.current.pendingPurchases).toEqual({
      [accountItem.id]: "operation-b",
    })
    expect(refreshAccountB).not.toHaveBeenCalled()

    await act(async () => {
      realtimeEvents.publish({
        id: "event-b",
        type: "personalization.purchase.completed",
        occurredAt: "2026-08-20T00:00:01Z",
        operationId: "operation-b",
        payload: {
          personalizationId: accountItem.id,
          status: "CONFIRMED",
          walletTransactionId: "transaction-b",
          failureReason: null,
        },
      })
      await Promise.resolve()
    })

    expect(result.current.pendingPurchases).toEqual({})
    expect(refreshAccountB).toHaveBeenCalledTimes(1)
    expect(showBackendError).not.toHaveBeenCalled()
  })

  it("keeps a user purchase busy across account switches before the operation id arrives", async () => {
    const purchaseOperation = createDeferred<PurchasePersonalizationResponse>()

    vi.mocked(purchasePersonalization).mockReturnValueOnce(purchaseOperation.promise)

    const { result, rerender } = renderHook(() => usePersonalizations())
    let firstPurchase!: Promise<void>

    act(() => {
      firstPurchase = result.current.purchase(userItem)
    })

    expect(result.current.busyId).toBe(userItem.id)

    productState.current = {
      ...productState.current!,
      activeAccount: { id: "account-b" },
    }
    rerender()

    expect(result.current.busyId).toBe(userItem.id)

    await act(async () => {
      await result.current.purchase(userItem)
    })

    expect(purchasePersonalization).toHaveBeenCalledTimes(1)

    await act(async () => {
      purchaseOperation.resolve({
        personalizationId: userItem.id,
        operationId: "user-operation",
      })
      await firstPurchase
    })

    expect(result.current.pendingPurchases).toEqual({
      [userItem.id]: "user-operation",
    })
  })

  it("does not expose the previous account catalog when the new scope fails to load", async () => {
    vi.mocked(listPersonalizations).mockImplementation(async (type) =>
      type === "NICK_STYLE"
        ? {
            ...emptyPage,
            items: [accountResponse],
            totalItems: 1,
            totalPages: 1,
          }
        : emptyPage,
    )

    const { result, rerender } = renderHook(() => usePersonalizations())

    await act(async () => {
      await flushPromises()
    })

    expect(result.current.items.map(({ id }) => id)).toEqual([accountItem.id])

    vi.mocked(listOwnedPersonalizations).mockRejectedValueOnce(
      new Error("ownership unavailable"),
    )
    productState.current = {
      ...productState.current!,
      activeAccount: { id: "account-b" },
    }
    rerender()

    await act(async () => {
      await flushPromises()
    })

    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBe(true)
    expect(result.current.items).toEqual([])
  })
})
