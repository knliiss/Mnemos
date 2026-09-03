// @vitest-environment jsdom

import type { ReactNode } from "react"
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, describe, expect, it, vi } from "vitest"
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
})
