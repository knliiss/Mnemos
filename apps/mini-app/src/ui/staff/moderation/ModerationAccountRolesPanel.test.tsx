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
const memberId = "019c11d1-bb2a-7000-8000-000000000402"
const roleId = "019c11d1-bb2a-7000-8000-000000000403"
const secondRoleId = "019c11d1-bb2a-7000-8000-000000000406"

const details: AdminAccountDetailsResponse = {
  accountId,
  cristalixUuid: "019c11d1-bb2a-7000-8000-000000000404",
  cristalixNickname: "AdminAccount",
  status: "ACTIVE",
  ownerMemberId: memberId,
  baseRoleId: null,
  maxMembers: 10,
  createdAt: "2026-09-03T18:00:00Z",
  deactivatedAt: null,
  deletedAt: null,
  members: [
    {
      memberId,
      userId: "019c11d1-bb2a-7000-8000-000000000405",
      username: "owner",
      owner: true,
      joinedAt: "2026-09-03T18:00:00Z",
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
  mocks.deleteAdminRole.mockResolvedValue(undefined)
  mocks.updateAdminBaseRole.mockResolvedValue(undefined)
  mocks.updateAdminRoleHierarchy.mockResolvedValue([])
  mocks.updateAdminRolePermissions.mockResolvedValue(role)
})

afterEach(() => {
  cleanup()
})

describe("ModerationAccountRolesPanel", () => {
  it("requires explicit confirmation and focuses the destructive role action", async () => {
    render(
      <ModerationAccountRolesPanel details={details} onChanged={vi.fn().mockResolvedValue(undefined)} />,
    )

    const deleteButton = await screen.findByText("roleEditor.delete")
    fireEvent.click(deleteButton)

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

    const deleteButton = await screen.findByText("roleEditor.delete")
    fireEvent.click(deleteButton)
    fireEvent.click(screen.getByText("Confirm"))

    await waitFor(() => {
      expect(mocks.deleteAdminRole).toHaveBeenCalledWith(accountId, roleId)
      expect(onChanged).toHaveBeenCalledTimes(1)
    })
  })

  it("requires explicit confirmation before setting the base role", async () => {
    render(
      <ModerationAccountRolesPanel details={details} onChanged={vi.fn().mockResolvedValue(undefined)} />,
    )

    const baseRoleButton = await screen.findByRole("button", { name: "BASE" })
    fireEvent.click(baseRoleButton)

    expect(mocks.updateAdminBaseRole).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", { name: "Set as base role?" })
    const confirm = screen.getByText("Set base role")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.keyDown(dialog, { key: "Escape" })

    expect(screen.queryByRole("alertdialog", { name: "Set as base role?" })).toBeNull()
    expect(mocks.updateAdminBaseRole).not.toHaveBeenCalled()
  })

  it("sets the base role only after confirmation", async () => {
    const onChanged = vi.fn().mockResolvedValue(undefined)

    render(<ModerationAccountRolesPanel details={details} onChanged={onChanged} />)

    fireEvent.click(await screen.findByRole("button", { name: "BASE" }))
    fireEvent.click(screen.getByText("Set base role"))

    await waitFor(() => {
      expect(mocks.updateAdminBaseRole).toHaveBeenCalledWith(accountId, roleId)
      expect(onChanged).toHaveBeenCalledTimes(1)
    })
  })

  it("clears the base role only after confirmation", async () => {
    const onChanged = vi.fn().mockResolvedValue(undefined)

    render(
      <ModerationAccountRolesPanel
        details={{ ...details, baseRoleId: roleId }}
        onChanged={onChanged}
      />,
    )

    fireEvent.click(await screen.findByRole("button", { name: "BASE" }))

    expect(mocks.clearAdminBaseRole).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", { name: "Clear base role?" })
    const confirm = screen.getByText("Clear base role")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.click(confirm)

    await waitFor(() => {
      expect(mocks.clearAdminBaseRole).toHaveBeenCalledWith(accountId)
      expect(onChanged).toHaveBeenCalledTimes(1)
    })

    expect(dialog).toBeDefined()
  })

  it("keeps permission toggles local until the draft is applied", async () => {
    const onChanged = vi.fn().mockResolvedValue(undefined)

    render(<ModerationAccountRolesPanel details={details} onChanged={onChanged} />)

    await screen.findByText("Moderator")

    const permissionButtons = screen.getAllByRole("button", {
      name: "roleEditor.permissions.MANAGE_MEMBERS",
    })
    fireEvent.click(permissionButtons[1]!)

    expect(mocks.updateAdminRolePermissions).not.toHaveBeenCalled()
    expect(screen.getByText("roleEditor.applyPermissionChanges")).toBeDefined()

    fireEvent.click(screen.getByText("roleEditor.applyPermissionChanges"))

    await waitFor(() => {
      expect(mocks.updateAdminRolePermissions).toHaveBeenCalledWith(accountId, roleId, {
        permissions: { MANAGE_MEMBERS: true },
      })
      expect(onChanged).toHaveBeenCalledTimes(1)
    })
  })

  it("requires confirmation before changing the role hierarchy", async () => {
    mocks.getAdminAccountRoles.mockResolvedValue([role, secondRole])
    const onChanged = vi.fn().mockResolvedValue(undefined)

    render(<ModerationAccountRolesPanel details={details} onChanged={onChanged} />)

    await screen.findByText("Observer")

    const downButtons = screen.getAllByRole("button", { name: "↓" })
    fireEvent.click(downButtons[0]!)

    expect(mocks.updateAdminRoleHierarchy).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", { name: "roleEditor.confirmHierarchyChange" })
    const confirm = screen.getByText("roleEditor.confirmHierarchyChangeAction")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.click(confirm)

    await waitFor(() => {
      expect(mocks.updateAdminRoleHierarchy).toHaveBeenCalledWith(accountId, [
        secondRoleId,
        roleId,
      ])
      expect(onChanged).toHaveBeenCalledTimes(1)
    })

    expect(dialog).toBeDefined()
  })

  it("requires confirmation before assigning a role to a member", async () => {
    const onChanged = vi.fn().mockResolvedValue(undefined)

    render(<ModerationAccountRolesPanel details={details} onChanged={onChanged} />)

    const memberRoleButton = await screen.findByRole("button", { name: "Moderator" })
    fireEvent.click(memberRoleButton)

    expect(mocks.assignAdminMemberRole).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", { name: "roleEditor.confirmMemberRoleChange" })
    const confirm = screen.getByText("roleEditor.assignMemberRole")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.click(confirm)

    await waitFor(() => {
      expect(mocks.assignAdminMemberRole).toHaveBeenCalledWith(accountId, memberId, roleId)
      expect(onChanged).toHaveBeenCalledTimes(1)
    })

    expect(dialog).toBeDefined()
  })
})
