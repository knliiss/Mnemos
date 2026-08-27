// @vitest-environment jsdom

import { fireEvent, render, screen, waitFor, within } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { i18n } from "@/shared/i18n"
import { createStarPrimePurchase, getStarPrimePackages } from "@/shared/api/paymentApi"
import { notifications } from "@/shared/notifications/notificationCenter"
import { PrimeTopUpPanel } from "./PrimeTopUpPanel"

const productState = vi.hoisted(() => ({
  current: {
    user: {
      id: "user-id",
      prime: 20,
    },
    refreshAccount: vi.fn().mockResolvedValue(undefined),
  },
}))

vi.mock("../../data/MnemosDataProvider", () => ({
  useMnemosData: () => productState.current,
}))

vi.mock("@/shared/api/paymentApi", () => ({
  createStarPrimePurchase: vi.fn(),
  getStarPrimePackages: vi.fn(),
  getStarPrimePurchase: vi.fn(),
}))

vi.mock("@/shared/notifications/notificationCenter", () => ({
  notifications: {
    error: vi.fn(),
    info: vi.fn(),
    success: vi.fn(),
    warning: vi.fn(),
  },
}))

describe("PrimeTopUpPanel", () => {
  beforeEach(async () => {
    await i18n.changeLanguage("uk")

    vi.mocked(getStarPrimePackages).mockResolvedValue([
      { id: "prime-25-stars-25", primeAmount: 25, starsAmount: 25 },
      { id: "prime-50-stars-50", primeAmount: 50, starsAmount: 50 },
      { id: "prime-100-stars-100", primeAmount: 100, starsAmount: 100 },
    ])
    vi.mocked(createStarPrimePurchase).mockReset()
    vi.mocked(notifications.error).mockReset()
    productState.current.refreshAccount.mockReset()
    productState.current.refreshAccount.mockResolvedValue(undefined)

    vi.stubGlobal("requestAnimationFrame", (callback: FrameRequestCallback) => {
      callback(0)
      return 1
    })
    vi.stubGlobal("cancelAnimationFrame", vi.fn())
    Object.defineProperty(window, "Telegram", {
      configurable: true,
      value: undefined,
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  it("keeps package selection on the page and renders checkout errors inside the purchase sheet", async () => {
    render(<PrimeTopUpPanel onPurchased={vi.fn().mockResolvedValue(undefined)} />)

    await screen.findByText("50 PRIME")

    expect(screen.queryByRole("dialog")).toBeNull()

    fireEvent.click(screen.getByRole("button", { name: /Купити за 50 Stars/i }))

    const dialog = screen.getByRole("dialog")

    expect(within(dialog).getByText("Купити PRIME за Stars?")).toBeTruthy()

    fireEvent.click(within(dialog).getByRole("button", { name: "Перейти до оплати" }))

    await waitFor(() => {
      expect(within(dialog).getByRole("alert")).toHaveTextContent(
        "Оплата Telegram Stars доступна лише всередині Telegram.",
      )
    })

    expect(notifications.error).not.toHaveBeenCalled()
    expect(createStarPrimePurchase).not.toHaveBeenCalled()
  })
})
