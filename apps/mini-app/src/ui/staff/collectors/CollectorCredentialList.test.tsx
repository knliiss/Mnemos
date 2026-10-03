// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, describe, expect, it, vi } from "vitest"
import type { CollectorCredentialResponse } from "@/shared/api/collectorAdminApi"
import { CollectorCredentialList } from "./CollectorCredentialList"

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}))

vi.mock("../../shared/MnemosIcon", () => ({
  Glyph: () => <span aria-hidden="true" />,
}))

const credential: CollectorCredentialResponse = {
  id: "019c11d1-bb2a-7000-8000-000000000201",
  name: "Collector Alpha",
  enabled: true,
  createdAt: "2026-09-03T18:00:00Z",
  updatedAt: "2026-09-03T19:00:00Z",
  revokedAt: null,
  accessKey: null,
}

const dateFormatter = new Intl.DateTimeFormat("en-US", {
  dateStyle: "medium",
  timeStyle: "short",
})

afterEach(() => {
  cleanup()
})

describe("CollectorCredentialList", () => {
  it("shows whether the collector is actively observing", () => {
    const props = {
      credentials: [credential],
      busyId: null,
      disableCandidate: null,
      revokeCandidate: null,
      dateFormatter,
      onEnable: vi.fn(),
      onRequestDisable: vi.fn(),
      onCancelDisable: vi.fn(),
      onConfirmDisable: vi.fn(),
      onRequestRevoke: vi.fn(),
      onCancelRevoke: vi.fn(),
      onConfirmRevoke: vi.fn(),
    }

    const view = render(
      <CollectorCredentialList {...props} activeCollectorIds={new Set([credential.id])} />,
    )

    expect(screen.getByText("collectorAdmin.presenceStatus.active")).toBeDefined()

    view.rerender(<CollectorCredentialList {...props} activeCollectorIds={new Set()} />)

    expect(screen.getByText("collectorAdmin.presenceStatus.inactive")).toBeDefined()
  })

  it("requires explicit confirmation before disabling an enabled collector", async () => {
    const onRequestDisable = vi.fn()
    const onConfirmDisable = vi.fn()
    const props = {
      credentials: [credential],
      activeCollectorIds: new Set([credential.id]),
      busyId: null,
      revokeCandidate: null,
      dateFormatter,
      onEnable: vi.fn(),
      onRequestDisable,
      onCancelDisable: vi.fn(),
      onConfirmDisable,
      onRequestRevoke: vi.fn(),
      onCancelRevoke: vi.fn(),
      onConfirmRevoke: vi.fn(),
    }
    const view = render(<CollectorCredentialList {...props} disableCandidate={null} />)

    fireEvent.click(screen.getByText("collectorAdmin.disable"))

    expect(onRequestDisable).toHaveBeenCalledWith(credential.id)
    expect(onConfirmDisable).not.toHaveBeenCalled()

    view.rerender(<CollectorCredentialList {...props} disableCandidate={credential.id} />)

    const confirmation = screen.getByRole("alertdialog", {
      name: "collectorAdmin.disableConfirmTitle",
    })
    const confirmButton = screen.getByText("collectorAdmin.disable")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirmButton)
    })

    fireEvent.click(confirmButton)

    expect(onConfirmDisable).toHaveBeenCalledWith(credential.id)
    expect(confirmation).toBeDefined()
  })

  it("dismisses credential confirmations with Escape", () => {
    const onCancelRevoke = vi.fn()

    render(
      <CollectorCredentialList
        credentials={[credential]}
        activeCollectorIds={new Set()}
        busyId={null}
        disableCandidate={null}
        revokeCandidate={credential.id}
        dateFormatter={dateFormatter}
        onEnable={vi.fn()}
        onRequestDisable={vi.fn()}
        onCancelDisable={vi.fn()}
        onConfirmDisable={vi.fn()}
        onRequestRevoke={vi.fn()}
        onCancelRevoke={onCancelRevoke}
        onConfirmRevoke={vi.fn()}
      />,
    )

    fireEvent.keyDown(
      screen.getByRole("alertdialog", { name: "collectorAdmin.revokeConfirmTitle" }),
      { key: "Escape" },
    )

    expect(onCancelRevoke).toHaveBeenCalledTimes(1)
  })
})
