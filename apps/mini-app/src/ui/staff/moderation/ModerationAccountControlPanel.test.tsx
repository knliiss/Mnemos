// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import type { AdminAccountDetailsResponse } from "@/shared/api/moderationApi"
import { ModerationAccountControlPanel } from "./ModerationAccountControlPanel"
import { resolveModerationDirectoryCopy } from "./moderationDirectoryTranslations"

const mocks = vi.hoisted(() => ({
  addAccountMember: vi.fn(),
  deleteAccountPermanently: vi.fn(),
  getAdminAccount: vi.fn(),
  removeAccountMember: vi.fn(),
  searchUserDirectory: vi.fn(),
  transferAccountOwnership: vi.fn(),
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

vi.mock("@/shared/api/moderationApi", () => ({
  addAccountMember: mocks.addAccountMember,
  deleteAccountPermanently: mocks.deleteAccountPermanently,
  getAdminAccount: mocks.getAdminAccount,
  removeAccountMember: mocks.removeAccountMember,
  searchUserDirectory: mocks.searchUserDirectory,
  transferAccountOwnership: mocks.transferAccountOwnership,
}))

const accountId = "019c1129-ef54-7000-8000-000000000431"
const ownerMemberId = "019c1129-ef54-7000-8000-000000000432"
const memberId = "019c1129-ef54-7000-8000-000000000433"
const memberUserId = "019c1129-ef54-7000-8000-000000000436"

const activeAccount: AdminAccountDetailsResponse = {
  accountId,
  cristalixUuid: "019c1129-ef54-7000-8000-000000000434",
  cristalixNickname: "knaliz",
  status: "ACTIVE",
  ownerMemberId,
  baseRoleId: null,
  maxMembers: 10,
  createdAt: "2026-09-03T00:00:00Z",
  deactivatedAt: null,
  deletedAt: null,
  members: [
    {
      memberId: ownerMemberId,
      userId: "019c1129-ef54-7000-8000-000000000435",
      username: "owner",
      owner: true,
      joinedAt: "2026-09-03T00:00:00Z",
    },
    {
      memberId,
      userId: memberUserId,
      username: "member",
      owner: false,
      joinedAt: "2026-09-03T00:01:00Z",
    },
  ],
}

const copy = resolveModerationDirectoryCopy("en")

beforeEach(() => {
  Object.values(mocks).forEach((mock) => mock.mockReset())
  mocks.searchUserDirectory.mockResolvedValue({
    content: [],
    page: 0,
    size: 6,
    totalElements: 0,
    totalPages: 0,
  })
})

afterEach(() => {
  cleanup()
})

describe("ModerationAccountControlPanel", () => {
  it("does not expose mutations for a deleted account", async () => {
    mocks.getAdminAccount.mockResolvedValue({
      ...activeAccount,
      status: "DELETED",
      deletedAt: "2026-09-03T00:02:00Z",
    })

    render(
      <ModerationAccountControlPanel
        accountId={accountId}
        canDelete
        copy={copy}
        onChanged={vi.fn()}
        onOpenUser={vi.fn()}
      />,
    )

    await waitFor(() => expect(mocks.getAdminAccount).toHaveBeenCalledWith(accountId))

    expect(screen.queryByText(copy.addMember)).toBeNull()
    expect(screen.queryByText(copy.transferOwner)).toBeNull()
    expect(screen.queryByText(copy.irreversibleDelete)).toBeNull()
  })

  it("requires confirmation before transferring ownership and sends the user id", async () => {
    mocks.getAdminAccount.mockResolvedValue(activeAccount)
    mocks.transferAccountOwnership.mockResolvedValue(undefined)

    render(
      <ModerationAccountControlPanel
        accountId={accountId}
        canDelete
        copy={copy}
        onChanged={vi.fn().mockResolvedValue(undefined)}
        onOpenUser={vi.fn()}
      />,
    )

    await waitFor(() => expect(mocks.getAdminAccount).toHaveBeenCalledWith(accountId))

    fireEvent.click(screen.getAllByText(copy.transferOwner)[0]!)

    expect(mocks.transferAccountOwnership).not.toHaveBeenCalled()
    expect(screen.getAllByText("@member").length).toBeGreaterThan(0)

    const dialog = screen.getByRole("alertdialog", { name: "Transfer account ownership?" })
    const confirm = screen.getByText("Confirm")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.click(confirm)

    await waitFor(() => {
      expect(mocks.transferAccountOwnership).toHaveBeenCalledWith(accountId, memberUserId)
    })

    expect(dialog).toBeDefined()
  })

  it("requires confirmation before removing an account member", async () => {
    mocks.getAdminAccount.mockResolvedValue(activeAccount)
    mocks.removeAccountMember.mockResolvedValue(undefined)

    render(
      <ModerationAccountControlPanel
        accountId={accountId}
        canDelete
        copy={copy}
        onChanged={vi.fn().mockResolvedValue(undefined)}
      />,
    )

    await waitFor(() => expect(mocks.getAdminAccount).toHaveBeenCalledWith(accountId))

    fireEvent.click(screen.getByText(copy.removeMember))

    expect(mocks.removeAccountMember).not.toHaveBeenCalled()

    fireEvent.click(screen.getByText("Confirm"))

    await waitFor(() => {
      expect(mocks.removeAccountMember).toHaveBeenCalledWith(accountId, memberId)
    })
  })

  it("dismisses a member mutation confirmation with Escape", async () => {
    mocks.getAdminAccount.mockResolvedValue(activeAccount)

    render(
      <ModerationAccountControlPanel
        accountId={accountId}
        canDelete
        copy={copy}
        onChanged={vi.fn().mockResolvedValue(undefined)}
      />,
    )

    await waitFor(() => expect(mocks.getAdminAccount).toHaveBeenCalledWith(accountId))

    fireEvent.click(screen.getByText(copy.removeMember))

    const dialog = screen.getByRole("alertdialog", { name: "Remove this account member?" })
    fireEvent.keyDown(dialog, { key: "Escape" })

    expect(
      screen.queryByRole("alertdialog", { name: "Remove this account member?" }),
    ).toBeNull()
    expect(mocks.removeAccountMember).not.toHaveBeenCalled()
  })

  it("requires confirmation before permanently deleting an account", async () => {
    mocks.getAdminAccount.mockResolvedValue(activeAccount)
    mocks.deleteAccountPermanently.mockResolvedValue(undefined)

    render(
      <ModerationAccountControlPanel
        accountId={accountId}
        canDelete
        copy={copy}
        onChanged={vi.fn().mockResolvedValue(undefined)}
      />,
    )

    await waitFor(() => expect(mocks.getAdminAccount).toHaveBeenCalledWith(accountId))

    fireEvent.click(screen.getByText(copy.irreversibleDelete))

    expect(mocks.deleteAccountPermanently).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", { name: copy.confirmDelete })
    const confirm = screen.getByText(copy.confirmDelete)

    await waitFor(() => {
      expect(document.activeElement).toBe(confirm)
    })

    fireEvent.click(confirm)

    await waitFor(() => {
      expect(mocks.deleteAccountPermanently).toHaveBeenCalledWith(accountId)
    })

    expect(dialog).toBeDefined()
  })
})
