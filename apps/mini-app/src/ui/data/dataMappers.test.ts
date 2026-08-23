import { describe, expect, it } from "vitest"
import type { AccountRoleResponse, AccountSummaryResponse } from "@/shared/api/backendContracts"
import { toAccount, toRole } from "./dataMappers"

describe("dataMappers", () => {
  it("preserves the current account member identity", () => {
    const response: AccountSummaryResponse = {
      id: "account-1",
      cristalixUuid: "cristalix-1",
      cristalixNickname: "Account",
      currentMemberId: "member-actor",
      memberCount: 3,
      maxMembers: 5,
      owner: false,
      active: true,
      status: "ACTIVE",
    }

    const account = toAccount(response)

    expect(account.currentMemberId).toBe("member-actor")
  })

  it("normalizes a partial role permission response into the full frontend contract", () => {
    const response: AccountRoleResponse = {
      id: "role-1",
      name: "Helper",
      color: "#FFFFFF",
      hierarchyRank: 100,
      permissions: {
        PLACE_BETS: true,
      },
      createdAt: "2026-01-01T00:00:00Z",
      updatedAt: "2026-01-01T00:00:00Z",
    }

    const role = toRole(response)

    expect(role.permissions.PLACE_BETS).toBe(true)
    expect(role.permissions.MANAGE_MEMBERS).toBe(false)
    expect(Object.keys(role.permissions)).toHaveLength(10)
  })
})
