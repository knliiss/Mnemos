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
      deactivatedAt: "2026-09-03T00:05:00Z",
      deletedAt: "2026-09-03T00:06:00Z",
    })

    render(
      <ModerationAccountControlPanel
        accountId={accountId}
        canDelete
        copy={copy}
        onChanged={vi.fn()}
      />,
    )

    await screen.findByText("DELETED")

    expect(screen.getByText("Owner")).toBeTruthy()
    expect(screen.queryByPlaceholderText("Mnemos User ID")).toBeNull()
    expect(screen.queryByText("Transfer owner")).toBeNull()
    expect(screen.queryByText("Remove")).toBeNull()
    expect(screen.queryByText("Delete account permanently")).toBeNull()
  })

  it("requires explicit confirmation before permanent deletion", async () => {
    const onChanged = vi.fn().mockResolvedValue(undefined)
    const deletedAccount: AdminAccountDetailsResponse = {
      ...activeAccount,
      status: "DELETED",
      deactivatedAt: "2026-09-03T00:05:00Z",
      deletedAt: "2026-09-03T00:06:00Z",
    }

    mocks.getAdminAccount
      .mockResolvedValueOnce(activeAccount)
      .mockResolvedValueOnce(deletedAccount)
    mocks.deleteAccountPermanently.mockResolvedValue(undefined)

    render(
      <ModerationAccountControlPanel
        accountId={accountId}
        canDelete
        copy={copy}
        onChanged={onChanged}
      />,
    )

    const deleteButton = await screen.findByText("Delete account permanently")

    fireEvent.click(deleteButton)

    expect(mocks.deleteAccountPermanently).not.toHaveBeenCalled()
    expect(screen.getByText("Confirm permanent deletion")).toBeTruthy()

    fireEvent.click(screen.getByText("Confirm permanent deletion"))

    await waitFor(() => {
      expect(mocks.deleteAccountPermanently).toHaveBeenCalledWith(accountId)
      expect(onChanged).toHaveBeenCalledOnce()
      expect(screen.getByText("DELETED")).toBeTruthy()
    })
  })
})
