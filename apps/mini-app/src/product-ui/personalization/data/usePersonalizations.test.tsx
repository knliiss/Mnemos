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
} from "@/shared/api/personalizationApi"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import type { Personalization } from "../../model"
import { usePersonalizations } from "./usePersonalizations"

type ProductMock = {
  user: { id: string; username: string }
  activeAccount: { id: string } | null
  refreshAccount: () => Promise<void>
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
})
