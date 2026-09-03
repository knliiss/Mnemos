// @vitest-environment jsdom

import type { ReactNode } from "react"
import { cleanup, fireEvent, render, screen, waitFor, within } from "@testing-library/react"
import { afterEach, describe, expect, it, vi } from "vitest"
import type { SanctionResponse } from "@/shared/api/moderationApi"
import { ModerationSanctions } from "./ModerationSanctions"
import type { ModerationUserController } from "./useModerationUser"

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}))

vi.mock("../../shared/MnemosIcon", () => ({
  Glyph: () => <span aria-hidden="true" />,
}))

vi.mock("../../shared/Primitives", () => ({
  ChoiceGrid: () => <div data-testid="choice-grid" />,
  Field: ({ children }: { children: ReactNode }) => <div>{children}</div>,
  Section: ({ children }: { children: ReactNode }) => <section>{children}</section>,
}))

const activeBan: SanctionResponse = {
  id: "019c11d1-bb2a-7000-8000-000000000701",
  userId: "019c11d1-bb2a-7000-8000-000000000702",
  type: "BAN",
  reason: "Repeated abuse",
  internalNote: null,
  createdByUserId: "019c11d1-bb2a-7000-8000-000000000703",
  createdAt: "2026-09-03T18:00:00Z",
  expiresAt: null,
  status: "ACTIVE",
}

function createController(overrides: Partial<ModerationUserController> = {}): ModerationUserController {
  return {
    activeBan: undefined,
    busy: false,
    sanctions: [],
    sanctionFormOpen: true,
    sanctionType: "BAN",
    reason: "Repeated abuse",
    internalNote: "",
    duration: "24",
    durationValid: true,
    setSanctionFormOpen: vi.fn(),
    setSanctionType: vi.fn(),
    setReason: vi.fn(),
    setInternalNote: vi.fn(),
    setDuration: vi.fn(),
    submitSanction: vi.fn().mockResolvedValue(undefined),
    revoke: vi.fn().mockResolvedValue(undefined),
    ...overrides,
  } as unknown as ModerationUserController
}

afterEach(() => {
  cleanup()
})

describe("ModerationSanctions", () => {
  it("requires explicit confirmation before creating a sanction", async () => {
    const controller = createController()

    render(<ModerationSanctions controller={controller} language="en" canSanction />)

    fireEvent.click(screen.getByText("moderation.createSanction"))

    expect(controller.submitSanction).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", { name: "moderation.createSanction" })
    const confirm = screen.getByText("common.confirm")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.click(confirm)

    await waitFor(() => {
      expect(controller.submitSanction).toHaveBeenCalledTimes(1)
    })

    expect(dialog).toBeDefined()
  })

  it("dismisses sanction confirmation with Escape", async () => {
    const controller = createController()

    render(<ModerationSanctions controller={controller} language="en" canSanction />)

    fireEvent.click(screen.getByText("moderation.createSanction"))

    const dialog = screen.getByRole("alertdialog", { name: "moderation.createSanction" })
    fireEvent.keyDown(dialog, { key: "Escape" })

    expect(screen.queryByRole("alertdialog", { name: "moderation.createSanction" })).toBeNull()
    expect(controller.submitSanction).not.toHaveBeenCalled()
  })

  it("requires explicit confirmation and focuses unban before revoking a ban", async () => {
    const controller = createController({
      activeBan,
      sanctions: [activeBan],
      sanctionFormOpen: false,
    })

    render(<ModerationSanctions controller={controller} language="en" canSanction />)

    fireEvent.click(screen.getByRole("button", { name: /moderation\.unban/ }))

    expect(controller.revoke).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", { name: "moderation.unban" })
    const confirm = within(dialog).getByRole("button", { name: "moderation.unban" })

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.keyDown(dialog, { key: "Escape" })

    expect(screen.queryByRole("alertdialog", { name: "moderation.unban" })).toBeNull()
    expect(controller.revoke).not.toHaveBeenCalled()
  })

  it("revokes a ban only after final confirmation", async () => {
    const controller = createController({
      activeBan,
      sanctions: [activeBan],
      sanctionFormOpen: false,
    })

    render(<ModerationSanctions controller={controller} language="en" canSanction />)

    fireEvent.click(screen.getByRole("button", { name: /moderation\.unban/ }))

    const dialog = screen.getByRole("alertdialog", { name: "moderation.unban" })
    fireEvent.click(within(dialog).getByRole("button", { name: "moderation.unban" }))

    await waitFor(() => {
      expect(controller.revoke).toHaveBeenCalledWith(activeBan.id)
    })
  })
})
