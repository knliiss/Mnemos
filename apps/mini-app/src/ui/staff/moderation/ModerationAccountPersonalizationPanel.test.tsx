// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import type { OwnedPersonalizationResponse } from "@/shared/api/personalizationApi"
import { ModerationAccountPersonalizationPanel } from "./ModerationAccountPersonalizationPanel"

const mocks = vi.hoisted(() => ({
  activateAdminAccountPersonalization: vi.fn(),
  deactivateAdminAccountPersonalization: vi.fn(),
  getAdminAccountPersonalizations: vi.fn(),
  grantAdminAccountPersonalization: vi.fn(),
  revokeAdminAccountPersonalization: vi.fn(),
  handleError: vi.fn(),
}))

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string, options?: string | { defaultValue?: string }) =>
      typeof options === "string" ? options : options?.defaultValue ?? key,
  }),
}))

vi.mock("@/shared/api/adminPersonalizationApi", () => ({
  activateAdminAccountPersonalization: mocks.activateAdminAccountPersonalization,
  deactivateAdminAccountPersonalization: mocks.deactivateAdminAccountPersonalization,
  getAdminAccountPersonalizations: mocks.getAdminAccountPersonalizations,
  grantAdminAccountPersonalization: mocks.grantAdminAccountPersonalization,
  revokeAdminAccountPersonalization: mocks.revokeAdminAccountPersonalization,
}))

vi.mock("@/shared/errors/errorHandler", () => ({
  handleError: mocks.handleError,
}))

vi.mock("./AdminPersonalizationPicker", () => ({
  AdminPersonalizationPicker: () => <div data-testid="personalization-picker" />,
}))

vi.mock("../../shared/MnemosIcon", () => ({
  Glyph: () => <span aria-hidden="true" />,
}))

const accountId = "019c11d1-bb2a-7000-8000-000000000501"
const personalizationId = "019c11d1-bb2a-7000-8000-000000000502"

const owned: OwnedPersonalizationResponse = {
  personalizationId,
  ownerType: "ACCOUNT",
  ownerId: accountId,
  type: "PROFILE_THEME",
  acquiredAt: "2026-09-03T18:00:00Z",
  active: false,
}

beforeEach(() => {
  Object.values(mocks).forEach((mock) => mock.mockReset())
  mocks.getAdminAccountPersonalizations.mockResolvedValue([owned])
  mocks.revokeAdminAccountPersonalization.mockResolvedValue(undefined)
})

afterEach(() => {
  cleanup()
})

describe("ModerationAccountPersonalizationPanel", () => {
  it("requires confirmation and focuses revoke before removing ownership", async () => {
    render(<ModerationAccountPersonalizationPanel accountId={accountId} deleted={false} />)

    await screen.findByText("PROFILE THEME")
    fireEvent.click(screen.getByText("Revoke"))

    expect(mocks.revokeAdminAccountPersonalization).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", { name: "Revoke personalization?" })
    const confirm = screen.getByText("Confirm")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.keyDown(dialog, { key: "Escape" })

    expect(screen.queryByRole("alertdialog", { name: "Revoke personalization?" })).toBeNull()
    expect(mocks.revokeAdminAccountPersonalization).not.toHaveBeenCalled()
  })

  it("revokes ownership only after confirmation", async () => {
    render(<ModerationAccountPersonalizationPanel accountId={accountId} deleted={false} />)

    await screen.findByText("PROFILE THEME")
    fireEvent.click(screen.getByText("Revoke"))
    fireEvent.click(screen.getByText("Confirm"))

    await waitFor(() => {
      expect(mocks.revokeAdminAccountPersonalization).toHaveBeenCalledWith(
        accountId,
        personalizationId,
      )
    })
  })
})
