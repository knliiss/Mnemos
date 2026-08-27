// @vitest-environment jsdom

import { fireEvent, render, screen, waitFor, within } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { i18n } from "@/shared/i18n"
import { getCoinPurchaseCatalog, purchaseCoin } from "@/shared/api/walletApi"
import { notifications } from "@/shared/notifications/notificationCenter"
import { CoinPurchasePanel } from "./CoinPurchasePanel"

const productState = vi.hoisted(() => ({
  current: {
    user: {
      prime: 100,
    },
    refreshAccount: vi.fn().mockResolvedValue(undefined),
  },
}))

vi.mock("../../data/MnemosDataProvider", () => ({
  useMnemosData: () => productState.current,
}))

vi.mock("@/shared/api/walletApi", () => ({
  getCoinPurchaseCatalog: vi.fn(),
  purchaseCoin: vi.fn(),
}))

vi.mock("@/shared/notifications/notificationCenter", () => ({
  notifications: {
    error: vi.fn(),
    info: vi.fn(),
    success: vi.fn(),
    warning: vi.fn(),
  },
}))

describe("CoinPurchasePanel", () => {
  beforeEach(async () => {
    await i18n.changeLanguage("uk")

    vi.mocked(getCoinPurchaseCatalog).mockResolvedValue({
      coinPerPrime: 100,
      options: [
        { primeAmount: 5, coinAmount: 500 },
        { primeAmount: 10, coinAmount: 1_000 },
        { primeAmount: 25, coinAmount: 2_500 },
      ],
    })
    vi.mocked(purchaseCoin).mockReset()
    vi.mocked(purchaseCoin).mockRejectedValue(new Error("exchange unavailable"))
    vi.mocked(notifications.error).mockReset()
    productState.current.refreshAccount.mockReset()
    productState.current.refreshAccount.mockResolvedValue(undefined)

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

  it("keeps package selection on the page and renders exchange failures inside the purchase sheet", async () => {
    render(
      <CoinPurchasePanel
        accountId="account-id"
        accountNickname="knalis"
        coinBalance={1_000}
        locale="uk-UA"
        onPurchased={vi.fn().mockResolvedValue(undefined)}
      />,
    )

    await screen.findByText("1 000 COIN")

    expect(screen.queryByRole("dialog")).toBeNull()

    fireEvent.click(screen.getByRole("button", { name: /Купити за/i }))

    const dialog = screen.getByRole("dialog")

    expect(within(dialog).getByText("Обміняти PRIME на COIN?")).toBeTruthy()

    fireEvent.click(within(dialog).getByRole("button", { name: "Підтвердити обмін" }))

    await waitFor(() => {
      expect(within(dialog).getByRole("alert").textContent).toBe(
        "Не вдалося купити COIN. Спробуй ще раз.",
      )
    })

    expect(notifications.error).not.toHaveBeenCalled()
    expect(screen.getByRole("dialog")).toBe(dialog)
  })
})
