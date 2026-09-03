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
    t: (key: string, options?: { defaultValue?: string }) => options?.defaultValue ?? key,
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

beforeEach(() => {
  Object.values(mocks).forEach((mock) => mock.mockReset())
  mocks.getAdminAccountRoles.mockResolvedValue([role])
  mocks.getAdminMemberRoles.mockResolvedValue([])
  mocks.deleteAdminRole.mockResolvedValue(undefined)
})

afterEach(() => {
  cleanup()
})

describe("ModerationAccountRolesPanel", () => {
  it("requires explicit confirmation and focuses the destructive role action", async () => {
    render(
      <ModerationAccountRolesPanel details={details} onChanged={vi.fn().mockResolvedValue(undefined)} />,
    )

    await screen.findByText("Moderator")
    fireEvent.click(screen.getByText("roleEditor.delete"))

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

    await screen.findByText("Moderator")
    fireEvent.click(screen.getByText("roleEditor.delete"))
    fireEvent.click(screen.getByText("Confirm"))

    await waitFor(() => {
      expect(mocks.deleteAdminRole).toHaveBeenCalledWith(accountId, roleId)
      expect(onChanged).toHaveBeenCalledTimes(1)
    })
  })
})
