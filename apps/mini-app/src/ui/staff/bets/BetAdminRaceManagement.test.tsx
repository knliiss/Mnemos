// @vitest-environment jsdom

import type { ReactNode } from "react"
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import type { ItemRaceResponse } from "@/shared/api/betApi"
import { BetAdminRaceManagement } from "./BetAdminRaceManagement"

const mocks = vi.hoisted(() => ({
  cancelItemRace: vi.fn(),
  createItemRace: vi.fn(),
  handleError: vi.fn(),
}))

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string, options?: { defaultValue?: string }) => options?.defaultValue ?? key,
  }),
}))

vi.mock("@/shared/api/betAdminApi", () => ({
  cancelItemRace: mocks.cancelItemRace,
  createItemRace: mocks.createItemRace,
}))

vi.mock("@/shared/errors/errorHandler", () => ({
  handleError: mocks.handleError,
}))

vi.mock("../../components/events/EventArt", () => ({
  ItemTypeArt: () => <span data-testid="item-art" />,
}))

vi.mock("../../shared/Primitives", () => ({
  CurrencyValue: ({ value }: { value: number }) => <span>{value}</span>,
  Field: ({ label, children }: { label: string; children: ReactNode }) => (
    <label>
      <span>{label}</span>
      {children}
    </label>
  ),
  PageHeader: ({ title, actions }: { title: string; actions?: ReactNode }) => (
    <>
      <header>{title}</header>
      {actions}
    </>
  ),
}))

const race: ItemRaceResponse = {
  id: "019c11d1-bb2a-7000-8000-000000000101",
  itemName: "Admin Blade",
  itemType: "SWORD",
  registrationClosesAt: "2026-09-03T20:00:00Z",
  startsAt: "2026-09-03T20:05:00Z",
  expiresAt: "2026-09-04T20:05:00Z",
  entryAmount: 1000,
  feePercent: 5,
  status: "ACTIVE",
  winnerAccountId: null,
  participants: [],
}

beforeEach(() => {
  Object.values(mocks).forEach((mock) => mock.mockReset())
  mocks.cancelItemRace.mockResolvedValue(undefined)
  mocks.createItemRace.mockResolvedValue(undefined)
})

afterEach(() => {
  cleanup()
})

describe("BetAdminRaceManagement", () => {
  it("requires explicit review before creating an item race", async () => {
    render(
      <BetAdminRaceManagement
        races={[]}
        loading={false}
        back={vi.fn()}
        onChanged={vi.fn().mockResolvedValue(undefined)}
      />,
    )

    fireEvent.click(screen.getByText("betAdmin.new"))
    fireEvent.change(screen.getByLabelText("betAdmin.item"), {
      target: { value: "Admin Blade" },
    })
    fireEvent.click(screen.getByText("betAdmin.reviewRace"))

    expect(mocks.createItemRace).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", {
      name: "betAdmin.createRaceConfirmTitle",
    })
    const confirm = screen.getByText("betAdmin.createRaceConfirmAction")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.keyDown(dialog, { key: "Escape" })

    expect(
      screen.queryByRole("alertdialog", {
        name: "betAdmin.createRaceConfirmTitle",
      }),
    ).toBeNull()
    expect(mocks.createItemRace).not.toHaveBeenCalled()
  })

  it("creates the reviewed race only after final confirmation", async () => {
    const onChanged = vi.fn().mockResolvedValue(undefined)

    render(
      <BetAdminRaceManagement races={[]} loading={false} back={vi.fn()} onChanged={onChanged} />,
    )

    fireEvent.click(screen.getByText("betAdmin.new"))
    fireEvent.change(screen.getByLabelText("betAdmin.item"), {
      target: { value: "  Admin Blade  " },
    })
    fireEvent.change(screen.getByLabelText("betAdmin.durationHours"), {
      target: { value: "48" },
    })
    fireEvent.change(screen.getByLabelText("betAdmin.entry"), {
      target: { value: "2500" },
    })

    fireEvent.click(screen.getByText("betAdmin.reviewRace"))
    fireEvent.click(screen.getByText("betAdmin.createRaceConfirmAction"))

    await waitFor(() => {
      expect(mocks.createItemRace).toHaveBeenCalledWith({
        itemName: "Admin Blade",
        itemType: "SWORD",
        durationHours: 48,
        entryAmount: 2500,
      })
      expect(onChanged).toHaveBeenCalledTimes(1)
    })
  })

  it("requires explicit confirmation before cancelling an active race", async () => {
    const onChanged = vi.fn().mockResolvedValue(undefined)

    render(
      <BetAdminRaceManagement
        races={[race]}
        loading={false}
        back={vi.fn()}
        onChanged={onChanged}
      />,
    )

    fireEvent.click(screen.getByText("betAdmin.cancel"))

    expect(mocks.cancelItemRace).not.toHaveBeenCalled()
    expect(screen.getByText("betAdmin.cancelRaceTitle")).toBeDefined()

    fireEvent.click(screen.getByText("betAdmin.confirmCancelRace"))

    await waitFor(() => {
      expect(mocks.cancelItemRace).toHaveBeenCalledWith(race.id)
      expect(onChanged).toHaveBeenCalledTimes(1)
    })
  })

  it("dismisses the cancellation confirmation with Escape", () => {
    render(
      <BetAdminRaceManagement
        races={[race]}
        loading={false}
        back={vi.fn()}
        onChanged={vi.fn().mockResolvedValue(undefined)}
      />,
    )

    fireEvent.click(screen.getByText("betAdmin.cancel"))

    const confirmation = screen.getByRole("alertdialog", {
      name: "betAdmin.cancelRaceTitle",
    })
    fireEvent.keyDown(confirmation, { key: "Escape" })

    expect(screen.queryByText("betAdmin.cancelRaceTitle")).toBeNull()
    expect(mocks.cancelItemRace).not.toHaveBeenCalled()
  })
})
