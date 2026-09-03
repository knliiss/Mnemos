// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import type { WalletTransactionResponse } from "@/shared/api/backendContracts"
import { ModerationAccountTransactionPanel } from "./ModerationAccountTransactionPanel"

const mocks = vi.hoisted(() => ({
  getAdminAccountTransactions: vi.fn(),
}))

vi.mock("@/shared/api/adminWalletApi", () => ({
  getAdminAccountTransactions: mocks.getAdminAccountTransactions,
}))

vi.mock("../../account/wallet/WalletHistoryPanel", () => ({
  WalletHistoryPanel: ({ transactions }: { transactions: WalletTransactionResponse[] }) => (
    <div data-testid="transaction-count">{transactions.length}</div>
  ),
}))

const accountId = "019c1129-ef54-7000-8000-000000000501"

const transaction: WalletTransactionResponse = {
  id: "019c1129-ef54-7000-8000-000000000502",
  accountId,
  currency: "COIN",
  type: "CREDIT",
  source: "ADMIN_ADJUSTMENT",
  sourceReferenceId: null,
  amount: 25,
  requestId: "019c1129-ef54-7000-8000-000000000503",
  createdAt: "2026-09-03T19:00:00Z",
}

beforeEach(() => {
  mocks.getAdminAccountTransactions.mockReset()
})

afterEach(() => {
  cleanup()
})

describe("ModerationAccountTransactionPanel", () => {
  it("loads additional backend pages instead of truncating the history to the first page", async () => {
    mocks.getAdminAccountTransactions
      .mockResolvedValueOnce({
        items: [transaction],
        page: 0,
        size: 30,
        totalItems: 31,
        totalPages: 2,
        hasNextPage: true,
      })
      .mockResolvedValueOnce({
        items: [{ ...transaction, id: "019c1129-ef54-7000-8000-000000000504" }],
        page: 1,
        size: 30,
        totalItems: 31,
        totalPages: 2,
        hasNextPage: false,
      })

    render(<ModerationAccountTransactionPanel accountId={accountId} />)

    await waitFor(() => {
      expect(mocks.getAdminAccountTransactions).toHaveBeenCalledWith(accountId, 0, 30)
    })

    expect(screen.getByTestId("transaction-count").textContent).toBe("1")

    fireEvent.click(screen.getByRole("button", { name: /next/i }))

    await waitFor(() => {
      expect(mocks.getAdminAccountTransactions).toHaveBeenCalledWith(accountId, 1, 30)
    })

    expect(screen.getByText("2")).toBeDefined()
  })
})
