// @vitest-environment jsdom

import type { ReactNode } from "react"
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import type { EventBetRoundResponse } from "@/shared/api/betApi"
import { BetAdminRoundDetail } from "./BetAdminRoundDetail"

const mocks = vi.hoisted(() => ({
  listAdminEventBets: vi.fn(),
  resolveEventBetRound: vi.fn(),
  handleError: vi.fn(),
}))

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string, options?: { defaultValue?: string }) => options?.defaultValue ?? key,
    i18n: { language: "en" },
  }),
}))

vi.mock("@/shared/api/betAdminApi", () => ({
  listAdminEventBets: mocks.listAdminEventBets,
  resolveEventBetRound: mocks.resolveEventBetRound,
}))

vi.mock("@/shared/errors/errorHandler", () => ({
  handleError: mocks.handleError,
}))

vi.mock("../../components/events/EventArt", () => ({
  GlobalEventArt: () => <span data-testid="event-art" />,
}))

vi.mock("../../shared/Primitives", () => ({
  CurrencyValue: ({ value }: { value: number }) => <span>{value}</span>,
  Field: ({ label, children }: { label: string; children: ReactNode }) => (
    <label>
      <span>{label}</span>
      {children}
    </label>
  ),
  PageHeader: ({ title }: { title: string }) => <header>{title}</header>,
}))

vi.mock("../../system/FeatureLoading", () => ({
  FeatureLoading: () => <div>loading</div>,
}))

const round: EventBetRoundResponse = {
  id: "019c11d1-bb2a-7000-8000-000000000601",
  eventType: "MOON",
  scheduledAt: "2026-09-03T18:00:00Z",
  closesAt: "2026-09-03T17:55:00Z",
  resolvesAt: "2026-09-03T18:05:00Z",
  yesOdds: 2,
  noOdds: 2,
  status: "AWAITING_ADMIN_CONFIRMATION",
  outcome: null,
  myBet: null,
}

beforeEach(() => {
  Object.values(mocks).forEach((mock) => mock.mockReset())
  mocks.listAdminEventBets.mockResolvedValue([])
  mocks.resolveEventBetRound.mockResolvedValue(undefined)
})

afterEach(() => {
  cleanup()
})

describe("BetAdminRoundDetail", () => {
  it("requires explicit confirmation and focuses the settlement action", async () => {
    render(
      <BetAdminRoundDetail
        round={round}
        loading={false}
        back={vi.fn()}
        onResolved={vi.fn().mockResolvedValue(undefined)}
      />,
    )

    fireEvent.change(screen.getByPlaceholderText("betAdmin.reasonPlaceholder"), {
      target: { value: "Verified event result" },
    })
    fireEvent.click(screen.getByText("betAdmin.confirmResult"))

    expect(mocks.resolveEventBetRound).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", {
      name: "betAdmin.resolutionConfirmTitle",
    })
    const confirm = screen.getByText("betAdmin.resolutionConfirmAction")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.keyDown(dialog, { key: "Escape" })

    expect(
      screen.queryByRole("alertdialog", {
        name: "betAdmin.resolutionConfirmTitle",
      }),
    ).toBeNull()
    expect(mocks.resolveEventBetRound).not.toHaveBeenCalled()
  })

  it("resolves the round only after the second confirmation", async () => {
    const back = vi.fn()
    const onResolved = vi.fn().mockResolvedValue(undefined)

    render(
      <BetAdminRoundDetail
        round={round}
        loading={false}
        back={back}
        onResolved={onResolved}
      />,
    )

    fireEvent.change(screen.getByPlaceholderText("betAdmin.reasonPlaceholder"), {
      target: { value: "  Verified event result  " },
    })
    fireEvent.click(screen.getByText("betAdmin.confirmResult"))
    fireEvent.click(screen.getByText("betAdmin.resolutionConfirmAction"))

    await waitFor(() => {
      expect(mocks.resolveEventBetRound).toHaveBeenCalledWith(
        round.id,
        "YES",
        "Verified event result",
      )
      expect(onResolved).toHaveBeenCalledTimes(1)
      expect(back).toHaveBeenCalledTimes(1)
    })
  })

  it("shows refund-specific impact before confirming a refund", async () => {
    render(
      <BetAdminRoundDetail
        round={round}
        loading={false}
        back={vi.fn()}
        onResolved={vi.fn().mockResolvedValue(undefined)}
      />,
    )

    fireEvent.click(screen.getByText("betAdmin.refund"))
    fireEvent.change(screen.getByPlaceholderText("betAdmin.reasonPlaceholder"), {
      target: { value: "Collector outage" },
    })
    fireEvent.click(screen.getByText("betAdmin.confirmResult"))

    expect(screen.getByText("betAdmin.resolutionRefundWarning")).toBeDefined()
    expect(mocks.resolveEventBetRound).not.toHaveBeenCalled()
  })
})
