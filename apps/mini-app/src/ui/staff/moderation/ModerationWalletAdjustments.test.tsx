// @vitest-environment jsdom

import type { ReactNode } from "react"
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, describe, expect, it, vi } from "vitest"
import type { IdentitySearchResponse } from "@/shared/api/moderationApi"
import { ModerationWalletAdjustments } from "./ModerationWalletAdjustments"
import type { ModerationUserController } from "./useModerationUser"

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}))

vi.mock("../../shared/MnemosIcon", () => ({
  CurrencyMark: () => <span aria-hidden="true" />,
  Glyph: () => <span aria-hidden="true" />,
}))

vi.mock("../../shared/Primitives", () => ({
  ChoiceGrid: () => <div data-testid="choice-grid" />,
  Field: ({ children }: { children: ReactNode }) => <div>{children}</div>,
  Segmented: () => <div data-testid="segmented" />,
}))

const identity = {
  userId: "019c11d1-bb2a-7000-8000-000000000601",
  username: "wallet-admin-target",
  telegramId: 123456,
  accounts: [
    {
      accountId: "019c11d1-bb2a-7000-8000-000000000602",
      cristalixNickname: "TargetAccount",
      cristalixUuid: "019c11d1-bb2a-7000-8000-000000000603",
      status: "ACTIVE",
    },
  ],
} as unknown as IdentitySearchResponse

function createController(overrides: Partial<ModerationUserController> = {}): ModerationUserController {
  return {
    walletTarget: "ACCOUNT",
    accountTargetId: identity.accounts[0]?.accountId ?? "",
    currency: "COIN",
    balance: {
      ownerType: "ACCOUNT",
      ownerId: identity.accounts[0]?.accountId ?? "",
      currency: "COIN",
      amount: 5000,
    },
    targetId: identity.accounts[0]?.accountId ?? "",
    operation: "CREDIT",
    amount: "750",
    amountValid: true,
    walletReason: "Manual correction",
    busy: false,
    adjustments: [],
    setWalletTarget: vi.fn(),
    setAccountTargetId: vi.fn(),
    setOperation: vi.fn(),
    setAmount: vi.fn(),
    setWalletReason: vi.fn(),
    submitAdjustment: vi.fn().mockResolvedValue(undefined),
    ...overrides,
  } as unknown as ModerationUserController
}

afterEach(() => {
  cleanup()
})

describe("ModerationWalletAdjustments", () => {
  it("requires explicit confirmation before changing a wallet balance", async () => {
    const controller = createController()

    render(
      <ModerationWalletAdjustments
        controller={controller}
        identity={identity}
        language="en"
        canAdjustPrime
      />,
    )

    fireEvent.click(screen.getByText("moderation.apply"))

    expect(controller.submitAdjustment).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", { name: "moderation.walletAdjustment" })
    const confirm = screen.getByText("common.confirm")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.click(confirm)

    await waitFor(() => {
      expect(controller.submitAdjustment).toHaveBeenCalledTimes(1)
    })

    expect(dialog).toBeDefined()
  })

  it("dismisses wallet confirmation with Escape", () => {
    const controller = createController()

    render(
      <ModerationWalletAdjustments
        controller={controller}
        identity={identity}
        language="en"
        canAdjustPrime
      />,
    )

    fireEvent.click(screen.getByText("moderation.apply"))

    const dialog = screen.getByRole("alertdialog", { name: "moderation.walletAdjustment" })
    fireEvent.keyDown(dialog, { key: "Escape" })

    expect(screen.queryByRole("alertdialog", { name: "moderation.walletAdjustment" })).toBeNull()
    expect(controller.submitAdjustment).not.toHaveBeenCalled()
  })
})
