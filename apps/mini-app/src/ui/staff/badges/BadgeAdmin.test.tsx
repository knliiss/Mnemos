// @vitest-environment jsdom

import type { ReactNode } from "react"
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import type { BadgeResponse } from "@/shared/api/badgeAdminApi"
import { BadgeAdmin } from "./BadgeAdmin"

const mocks = vi.hoisted(() => ({
  setQuery: vi.fn(),
  setSelectedUser: vi.fn(),
  toggleAssignment: vi.fn(),
  create: vi.fn(),
  refresh: vi.fn(),
}))

const testData = vi.hoisted(() => ({
  badge: {
    id: "019c11d1-bb2a-7000-8000-000000000501",
    code: "EVENT_MASTER",
    name: "Event Master",
    imageUrl: "/badge.png",
    color: "#CBE86E",
  },
  selectedUser: {
    userId: "019c11d1-bb2a-7000-8000-000000000502",
    username: "knalis",
    telegramId: 123456,
    accounts: [],
  },
}))

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}))

vi.mock("./useBadgeCatalog", () => ({
  useBadgeCatalog: () => ({
    badges: [testData.badge],
    loading: false,
    loadError: false,
    creating: false,
    create: mocks.create,
    refresh: mocks.refresh,
  }),
}))

vi.mock("./useBadgeAssignments", () => ({
  useBadgeAssignments: () => ({
    query: "",
    setQuery: mocks.setQuery,
    normalizedQuery: "",
    visibleResults: [],
    searchFailed: false,
    selectedUser: testData.selectedUser,
    setSelectedUser: mocks.setSelectedUser,
    userBadges: [],
    assignedIds: new Set<string>(),
    busy: false,
    toggleAssignment: mocks.toggleAssignment,
  }),
}))

vi.mock("./BadgeAssignmentGrid", () => ({
  BadgeAssignmentGrid: ({
    badges,
    onToggleAssignment,
  }: {
    badges: BadgeResponse[]
    onToggleAssignment: (badge: BadgeResponse) => void
  }) => (
    <button type="button" onClick={() => onToggleAssignment(badges[0]!)}>
      toggle badge
    </button>
  ),
}))

vi.mock("./BadgeAssignmentSearch", () => ({
  BadgeAssignmentSearch: () => <div data-testid="badge-search" />,
}))

vi.mock("./BadgeCreateForm", () => ({
  BadgeCreateForm: () => <div data-testid="badge-create" />,
}))

vi.mock("../../shared/MnemosIcon", () => ({
  Glyph: () => <span aria-hidden="true" />,
}))

vi.mock("../../shared/Primitives", () => ({
  PageHeader: ({ title, actions }: { title: string; actions?: ReactNode }) => (
    <header>
      {title}
      {actions}
    </header>
  ),
}))

vi.mock("../../system/FeatureEmpty", () => ({
  FeatureEmpty: ({ children }: { children?: ReactNode }) => <div>{children}</div>,
}))

vi.mock("../../system/FeatureLoading", () => ({
  FeatureLoading: () => <div>loading</div>,
}))

beforeEach(() => {
  Object.values(mocks).forEach((mock) => mock.mockReset())
  mocks.toggleAssignment.mockResolvedValue(undefined)
})

afterEach(() => {
  cleanup()
})

describe("BadgeAdmin", () => {
  it("requires confirmation before assigning a badge", async () => {
    render(<BadgeAdmin back={vi.fn()} />)

    fireEvent.click(screen.getByText("toggle badge"))

    expect(mocks.toggleAssignment).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", {
      name: "badgeAdmin.confirmAssignmentChange",
    })
    const confirm = screen.getByText("badgeAdmin.confirmAssignBadge")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.click(confirm)

    await waitFor(() => {
      expect(mocks.toggleAssignment).toHaveBeenCalledWith(testData.badge)
    })

    expect(dialog).toBeDefined()
  })

  it("dismisses badge assignment confirmation with Escape", () => {
    render(<BadgeAdmin back={vi.fn()} />)

    fireEvent.click(screen.getByText("toggle badge"))

    const dialog = screen.getByRole("alertdialog", {
      name: "badgeAdmin.confirmAssignmentChange",
    })
    fireEvent.keyDown(dialog, { key: "Escape" })

    expect(
      screen.queryByRole("alertdialog", { name: "badgeAdmin.confirmAssignmentChange" }),
    ).toBeNull()
    expect(mocks.toggleAssignment).not.toHaveBeenCalled()
  })
})
