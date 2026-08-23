import { describe, expect, it } from "vitest"
import type { Account, AccountRole, Member, RolePermission } from "../model"
import {
  resolveBaseRoleAccess,
  resolveHierarchyBoundary,
  resolveKickMemberAccess,
  resolveRoleAssignmentAccess,
  resolveRoleManagementAccess,
  resolveRolePermissionMutationAccess,
} from "./roleAccessPolicy"

const permissions = (enabled: RolePermission[] = []): AccountRole["permissions"] =>
  Object.fromEntries(enabled.map((permission) => [permission, true])) as AccountRole["permissions"]

const account = (owner = false): Account => ({
  id: "account-1",
  currentMemberId: "member-actor",
  cristalixUuid: "cristalix-1",
  nickname: "Account",
  role: owner ? "OWNER" : "MEMBER",
  skinUrl: "skin",
  memberCount: 3,
  maxMembers: 5,
  owner,
  active: true,
  coin: 0,
})

const actor: Member = {
  id: "member-actor",
  name: "actor",
  owner: false,
  joinedAt: "2026-01-01T00:00:00Z",
  highestRoleId: "role-admin",
  roleIds: ["role-admin", "role-helper"],
}

const target: Member = {
  id: "member-target",
  name: "target",
  owner: false,
  joinedAt: "2026-01-01T00:00:00Z",
  highestRoleId: "role-helper",
  roleIds: ["role-helper"],
}

const roles: AccountRole[] = [
  {
    id: "role-admin",
    name: "Admin",
    color: "#FFFFFF",
    hierarchyRank: 100,
    permissions: permissions(["MANAGE_ROLES", "ASSIGN_MEMBER_ROLES"]),
  },
  {
    id: "role-peer",
    name: "Peer",
    color: "#FFFFFF",
    hierarchyRank: 100,
    permissions: permissions(),
  },
  {
    id: "role-helper",
    name: "Helper",
    color: "#FFFFFF",
    hierarchyRank: 200,
    permissions: permissions(["PLACE_BETS"]),
  },
  {
    id: "role-limited",
    name: "Limited",
    color: "#FFFFFF",
    hierarchyRank: 300,
    permissions: permissions(["MANAGE_BASE_ROLE"]),
  },
]

describe("roleAccessPolicy", () => {
  it("blocks protected roles for delegated role managers", () => {
    expect(
      resolveRoleManagementAccess(
        { account: account(), actorMember: actor, roles },
        true,
        roles[1],
      ),
    ).toBe("ROLE_NOT_LOWER")

    expect(
      resolveRoleManagementAccess(
        { account: account(), actorMember: actor, roles },
        true,
        roles[2],
      ),
    ).toBe("ALLOWED")
  })

  it("allows the owner to manage every role", () => {
    expect(
      resolveRoleManagementAccess(
        { account: account(true), actorMember: actor, roles },
        true,
        roles[0],
      ),
    ).toBe("ALLOWED")
  })

  it("blocks editing permissions on a role assigned to the actor", () => {
    expect(
      resolveRolePermissionMutationAccess(
        { account: account(), actorMember: actor, roles },
        true,
        roles[2],
        actor.roleIds,
      ),
    ).toBe("ROLE_ASSIGNED_TO_ACTOR")
  })

  it("blocks self role assignment for delegated managers", () => {
    expect(
      resolveRoleAssignmentAccess(
        {
          account: account(),
          actorMember: actor,
          targetMember: actor,
          role: roles[2],
          roles,
          hasPermission: () => true,
        },
        true,
        true,
      ),
    ).toBe("SELF_MEMBER")
  })

  it("blocks assigning a role when the actor cannot grant all of its permissions", () => {
    expect(
      resolveRoleAssignmentAccess(
        {
          account: account(),
          actorMember: actor,
          targetMember: { ...target, highestRoleId: null, roleIds: [] },
          role: roles[3],
          roles,
          hasPermission: (permission) => permission !== "MANAGE_BASE_ROLE",
        },
        true,
        true,
      ),
    ).toBe("UNGRANTABLE_PERMISSION")
  })

  it("still permits revoking the same lower role without grant permission", () => {
    expect(
      resolveRoleAssignmentAccess(
        {
          account: account(),
          actorMember: actor,
          targetMember: { ...target, highestRoleId: "role-limited", roleIds: ["role-limited"] },
          role: roles[3],
          roles,
          hasPermission: () => false,
        },
        true,
        false,
      ),
    ).toBe("ALLOWED")
  })

  it("blocks kicking a member at the actor hierarchy level", () => {
    expect(
      resolveKickMemberAccess(
        {
          account: account(),
          actorMember: actor,
          targetMember: { ...target, highestRoleId: "role-peer", roleIds: ["role-peer"] },
          roles,
        },
        true,
      ),
    ).toBe("TARGET_MEMBER_NOT_LOWER")
  })

  it("applies hierarchy and grant rules to base role selection", () => {
    expect(
      resolveBaseRoleAccess(
        { account: account(), actorMember: actor, roles },
        true,
        roles[1],
        () => true,
      ),
    ).toBe("ROLE_NOT_LOWER")

    expect(
      resolveBaseRoleAccess(
        { account: account(), actorMember: actor, roles },
        true,
        roles[3],
        () => false,
      ),
    ).toBe("UNGRANTABLE_PERMISSION")
  })

  it("returns the actor rank as the delegated hierarchy boundary", () => {
    expect(resolveHierarchyBoundary({ account: account(), actorMember: actor, roles }, true)).toBe(
      100,
    )
    expect(
      resolveHierarchyBoundary({ account: account(true), actorMember: actor, roles }, true),
    ).toBe(Number.NEGATIVE_INFINITY)
  })
})
