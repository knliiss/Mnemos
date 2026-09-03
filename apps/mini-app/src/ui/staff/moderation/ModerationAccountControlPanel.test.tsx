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
  transferAccountOwnership: vi.fn(),
}))

vi.mock("@/shared/api/moderationApi", () => ({
  addAccountMember: mocks.addAccountMember,
  deleteAccountPermanently: mocks.deleteAccountPermanently,
  getAdminAccount: mocks.getAdminAccount,
  removeAccountMember: mocks.removeAccountMember,
  transferAccountOwnership: mocks.transferAccountOwnership,
}))

const accountId = "019c1129-ef54-7000-8000-000000000431"
const ownerMemberId = "019c1129-ef54-7000-8000-000000000432"
const memberId = "019c1129-ef54-7000-8000-000000000433"

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
      userId: "019c1129-ef54-7000-8000-000000000436",
      username: "member",
      owner: false,
      joinedAt: "2026-09-03T00:01:00Z",
    },
  ],
}

const copy = resolveModerationDirectoryCopy("en")

beforeEach(() => {
  Object.values(mocks).forEach((mock) => mock.mockReset())
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

    expect(screen.queryByText(copy.addMember)).not.toBeInTheDocument()
    expect(screen.queryByText(copy.transferOwnership)).not.toBeInTheDocument()
    expect(screen.queryByText(copy.deleteAccount)).not.toBeInTheDocument()
  })

  it("transfers ownership using user id instead of account member id", async () => {
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

    const targetUserId = "019c1129-ef54-7000-8000-000000000437"
    const transferInput = screen.getByPlaceholderText(copy.transferOwnershipPlaceholder)

    fireEvent.change(transferInput, { target: { value: targetUserId } })
    fireEvent.click(screen.getByText(copy.transferOwnership))

    await waitFor(() => {
      expect(mocks.transferAccountOwnership).toHaveBeenCalledWith(accountId, targetUserId)
    })
  })
})
