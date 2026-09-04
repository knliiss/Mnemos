// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import type {
  AdminAccountDetailsResponse,
  AdminAccountRoleResponse,
} from "@/shared/api/moderationApi"
import { ModerationAccountRolesPanel } from "./ModerationAccountRolesPanel"

const mocks = vi.hoisted(() => ({
  assignAdminMemberRole: vi.fn(),
  clearAdminBaseRole: vi.fn(),
  createAdminRole: vi.fn(),
  deleteAdminRole: vi.fn(),
  revokeAdminMemberRole: vi.fn(),
  updateAdminBaseRole: vi.fn(),
  updateAdminRoleDetails: vi.fn(),
  updateAdminRoleHierarchy: vi.fn(),
  updateAdminRolePermissions: vi.fn(),
  getAdminAccountRoles: vi.fn(),
  getAdminMemberRoles: vi.fn(),
  handleError: vi.fn(),
}))

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string, fallback?: string | { defaultValue?: string }) => {
      if (typeof fallback === "string") {
        return fallback
      }

      return fallback?.defaultValue ?? key
    },
  }),
}))

vi.mock("@/shared/api/adminRoleApi", () => ({
  assignAdminMemberRole: mocks.assignAdminMemberRole,
  clearAdminBaseRole: mocks.clearAdminBaseRole,
  createAdminRole: mocks.createAdminRole,
  deleteAdminRole: mocks.deleteAdminRole,
  revokeAdminMemberRole: mocks.revokeAdminMemberRole,
  updateAdminBaseRole: mocks.updateAdminBaseRole,
  updateAdminRoleDetails: mocks.updateAdminRoleDetails,
  updateAdminRoleHierarchy: mocks.updateAdminRoleHierarchy,
  updateAdminRolePermissions: mocks.updateAdminRolePermissions,
}))

vi.mock("@/shared/api/moderationApi", () => ({
  getAdminAccountRoles: mocks.getAdminAccountRoles,
  getAdminMemberRoles: mocks.getAdminMemberRoles,
}))

vi.mock("@/shared/errors/errorHandler", () => ({
  handleError: mocks.handleError,
}))

vi.mock("./AdminRoleColorControl", () => ({
  AdminRoleColorControl: () => <div data-testid="role-color-control" />,
}))

const accountId = "019c11d1-bb2a-7000-8000-000000000401"
const ownerMemberId = "019c11d1-bb2a-7000-8000-000000000402"
const memberId = "019c11d1-bb2a-7000-8000-000000000407"
const roleId = "019c11d1-bb2a-7000-8000-000000000403"
const secondRoleId = "019c11d1-bb2a-7000-8000-000000000406"

const details: AdminAccountDetailsResponse = {
  accountId,
  cristalixUuid: "019c11d1-bb2a-7000-8000-000000000404",
  cristalixNickname: "AdminAccount",
  status: "ACTIVE",
  ownerMemberId,
  baseRoleId: null,
  maxMembers: 10,
  createdAt: "2026-09-03T18:00:00Z",
  deactivatedAt: null,
  deletedAt: null,
  members: [
    {
      memberId: ownerMemberId,
      userId: "019c11d1-bb2a-7000-8000-000000000405",
      username: "owner",
      owner: true,
      joinedAt: "2026-09-03T18:00:00Z",
    },
    {
      memberId,
      userId: "019c11d1-bb2a-7000-8000-000000000408",
      username: "member",
      owner: false,
      joinedAt: "2026-09-03T18:01:00Z",
    },
  ],
}

const role: AdminAccountRoleResponse = {
  id: roleId,
  name: "Moderator",
  color: "#4BAF8A",
  hierarchyRank: 1,
  permissions: {},
  createdAt: "2026-09-03T18:00:00Z",
  updatedAt: "2026-09-03T18:00:00Z",
}

const secondRole: AdminAccountRoleResponse = {
  ...role,
  id: secondRoleId,
  name: "Observer",
  hierarchyRank: 2,
}

beforeEach(() => {
  Object.values(mocks).forEach((mock) => mock.mockReset())
  mocks.getAdminAccountRoles.mockResolvedValue([role])
  mocks.getAdminMemberRoles.mockResolvedValue([])
  mocks.assignAdminMemberRole.mockResolvedValue(undefined)
  mocks.clearAdminBaseRole.mockResolvedValue(undefined)
  mocks.createAdminRole.mockResolvedValue(role)
  mocks.deleteAdminRole.mockResolvedValue(undefined)
  mocks.revokeAdminMemberRole.mockResolvedValue(undefined)
  mocks.updateAdminBaseRole.mockResolvedValue(undefined)
  mocks.updateAdminRoleDetails.mockResolvedValue(role)
  mocks.updateAdminRoleHierarchy.mockResolvedValue([])
  mocks.updateAdminRolePermissions.mockResolvedValue(role)
})

afterEach(() => {
  cleanup()
})

describe("ModerationAccountRolesPanel", () => {
  it("shows one compact role workspace instead of expanding every role", async () => {
    mocks.getAdminAccountRoles.mockResolvedValue([role, secondRole])

    render(
      <ModerationAccountRolesPanel
        details={details}
        onChanged={vi.fn().mockResolvedValue(undefined)}
      />,
    )

    await screen.findByText("Moderator")

    expect(screen.getByText("Observer")).toBeDefined()
    expect(screen.getAllByText("roleEditor.name")).toHaveLength(1)
    expect(mocks.getAdminMemberRoles).not.toHaveBeenCalled()
  })

  it("keeps destructive delete behind an explicit focused confirmation", async () => {
    render(
      <ModerationAccountRolesPanel
        details={details}
        onChanged={vi.fn().mockResolvedValue(undefined)}
      />,
    )

    fireEvent.click(await screen.findByText("roleEditor.deleteTitle"))

    expect(mocks.deleteAdminRole).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", { name: "Delete this role?" })
    const confirm = screen.getByText("Confirm")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.keyDown(dialog, { key: "Escape" })

    expect(screen.queryByRole("alertdialog", { name: "Delete this role?" })).toBeNull()
    expect(mocks.deleteAdminRole).not.toHaveBeenCalled()
  })

  it("deletes a role only after confirmation", async () => {
    const onChanged = vi.fn().mockResolvedValue(undefined)

    render(<ModerationAccountRolesPanel details={details} onChanged={onChanged} />)

    fireEvent.click(await screen.findByText("roleEditor.deleteTitle"))
    fireEvent.click(screen.getByText("Confirm"))

    await waitFor(() => {
      expect(mocks.deleteAdminRole).toHaveBeenCalledWith(accountId, roleId)
      expect(onChanged).toHaveBeenCalledTimes(1)
    })
  })

  it("keeps base role changes behind confirmation", async () => {
    const onChanged = vi.fn().mockResolvedValue(undefined)

    render(<ModerationAccountRolesPanel details={details} onChanged={onChanged} />)

    fireEvent.click(await screen.findByText("BASE"))

    expect(mocks.updateAdminBaseRole).not.toHaveBeenCalled()

    const confirm = screen.getByText("Set base role")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.click(confirm)

    await waitFor(() => {
      expect(mocks.updateAdminBaseRole).toHaveBeenCalledWith(accountId, roleId)
      expect(onChanged).toHaveBeenCalledTimes(1)
    })
  })

  it("keeps permission toggles local until the draft is applied", async () => {
    const onChanged = vi.fn().mockResolvedValue(undefined)

    render(<ModerationAccountRolesPanel details={details} onChanged={onChanged} />)

    await screen.findByText("Moderator")

    fireEvent.click(
      screen.getByRole("button", {
        name: "roleEditor.permissions.MANAGE_MEMBERS",
      }),
    )

    expect(mocks.updateAdminRolePermissions).not.toHaveBeenCalled()

    fireEvent.click(screen.getByText("roleEditor.applyPermissionChanges"))

    await waitFor(() => {
      expect(mocks.updateAdminRolePermissions).toHaveBeenCalledWith(accountId, roleId, {
        permissions: { MANAGE_MEMBERS: true },
      })
      expect(onChanged).toHaveBeenCalledTimes(1)
    })
  })

  it("changes hierarchy directly without a confirmation dialog", async () => {
    mocks.getAdminAccountRoles.mockResolvedValue([role, secondRole])
    const onChanged = vi.fn().mockResolvedValue(undefined)

    render(<ModerationAccountRolesPanel details={details} onChanged={onChanged} />)

    await screen.findByText("Observer")
    fireEvent.click(screen.getByRole("button", { name: "↓" }))

    await waitFor(() => {
      expect(mocks.updateAdminRoleHierarchy).toHaveBeenCalledWith(accountId, [secondRoleId, roleId])
      expect(onChanged).toHaveBeenCalledTimes(1)
    })

    expect(
      screen.queryByRole("alertdialog", { name: "roleEditor.confirmHierarchyChange" }),
    ).toBeNull()
  })

  it("loads roles only for the selected member and assigns them directly", async () => {
    const onChanged = vi.fn().mockResolvedValue(undefined)

    render(<ModerationAccountRolesPanel details={details} onChanged={onChanged} />)

    await screen.findByText("Moderator")
    fireEvent.click(screen.getByText("roleEditor.groups.members"))

    await waitFor(() => {
      expect(mocks.getAdminMemberRoles).toHaveBeenCalledWith(accountId, ownerMemberId)
    })

    fireEvent.click(screen.getByText("@member"))

    await waitFor(() => {
      expect(mocks.getAdminMemberRoles).toHaveBeenCalledWith(accountId, memberId)
    })

    fireEvent.click(screen.getByRole("button", { name: /Moderator/ }))

    await waitFor(() => {
      expect(mocks.assignAdminMemberRole).toHaveBeenCalledWith(accountId, memberId, roleId)
      expect(onChanged).toHaveBeenCalledTimes(1)
    })

    expect(
      screen.queryByRole("alertdialog", { name: "roleEditor.confirmMemberRoleChange" }),
    ).toBeNull()
  })
})
