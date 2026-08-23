import type { Account, AccountRole, Member, RolePermission } from "../model"

export type RoleAccessReason =
  | "ALLOWED"
  | "MISSING_PERMISSION"
  | "ACTOR_ROLE_UNAVAILABLE"
  | "SELF_MEMBER"
  | "TARGET_OWNER"
  | "TARGET_MEMBER_NOT_LOWER"
  | "ROLE_NOT_LOWER"
  | "UNGRANTABLE_PERMISSION"
  | "ROLE_ASSIGNED_TO_ACTOR"

type RoleAccessContext = {
  account: Account
  actorMember: Member | null
  roles: AccountRole[]
}

type MemberRoleMutationContext = RoleAccessContext & {
  targetMember: Member
  role: AccountRole
  hasPermission(permission: RolePermission): boolean
}

export function resolveActorMember(account: Account, members: Member[]): Member | null {
  if (!account.currentMemberId) {
    return null
  }

  return members.find((member) => member.id === account.currentMemberId) ?? null
}

export function resolveHighestRole(member: Member | null, roles: AccountRole[]): AccountRole | null {
  if (!member?.highestRoleId) {
    return null
  }

  return roles.find((role) => role.id === member.highestRoleId) ?? null
}

export function resolveRoleManagementAccess(
  context: RoleAccessContext,
  hasManageRoles: boolean,
  role: AccountRole,
): RoleAccessReason {
  if (!hasManageRoles) {
    return "MISSING_PERMISSION"
  }

  if (context.account.owner) {
    return "ALLOWED"
  }

  const actorHighestRole = resolveHighestRole(context.actorMember, context.roles)

  if (!actorHighestRole) {
    return "ACTOR_ROLE_UNAVAILABLE"
  }

  if (actorHighestRole.hierarchyRank >= role.hierarchyRank) {
    return "ROLE_NOT_LOWER"
  }

  return "ALLOWED"
}

export function resolveRolePermissionMutationAccess(
  context: RoleAccessContext,
  hasManageRoles: boolean,
  role: AccountRole,
  actorRoleIds: string[],
): RoleAccessReason {
  const managementAccess = resolveRoleManagementAccess(context, hasManageRoles, role)

  if (managementAccess !== "ALLOWED") {
    return managementAccess
  }

  if (!context.account.owner && actorRoleIds.includes(role.id)) {
    return "ROLE_ASSIGNED_TO_ACTOR"
  }

  return "ALLOWED"
}

export function resolveRoleAssignmentAccess(
  context: MemberRoleMutationContext,
  hasAssignRoles: boolean,
  assigning: boolean,
): RoleAccessReason {
  if (!hasAssignRoles) {
    return "MISSING_PERMISSION"
  }

  if (context.account.owner) {
    return "ALLOWED"
  }

  if (context.targetMember.id === context.account.currentMemberId) {
    return "SELF_MEMBER"
  }

  if (context.targetMember.owner) {
    return "TARGET_OWNER"
  }

  const actorHighestRole = resolveHighestRole(context.actorMember, context.roles)

  if (!actorHighestRole) {
    return "ACTOR_ROLE_UNAVAILABLE"
  }

  if (actorHighestRole.hierarchyRank >= context.role.hierarchyRank) {
    return "ROLE_NOT_LOWER"
  }

  const targetHighestRole = resolveHighestRole(context.targetMember, context.roles)

  if (targetHighestRole && actorHighestRole.hierarchyRank >= targetHighestRole.hierarchyRank) {
    return "TARGET_MEMBER_NOT_LOWER"
  }

  if (
    assigning &&
    Object.entries(context.role.permissions).some(
      ([permission, enabled]) => enabled && !context.hasPermission(permission as RolePermission),
    )
  ) {
    return "UNGRANTABLE_PERMISSION"
  }

  return "ALLOWED"
}

export function resolveKickMemberAccess(
  context: RoleAccessContext & { targetMember: Member },
  hasManageMembers: boolean,
): RoleAccessReason {
  if (!hasManageMembers) {
    return "MISSING_PERMISSION"
  }

  if (context.targetMember.id === context.account.currentMemberId) {
    return "SELF_MEMBER"
  }

  if (context.targetMember.owner) {
    return "TARGET_OWNER"
  }

  if (context.account.owner) {
    return "ALLOWED"
  }

  const actorHighestRole = resolveHighestRole(context.actorMember, context.roles)

  if (!actorHighestRole) {
    return "ACTOR_ROLE_UNAVAILABLE"
  }

  const targetHighestRole = resolveHighestRole(context.targetMember, context.roles)

  if (targetHighestRole && actorHighestRole.hierarchyRank >= targetHighestRole.hierarchyRank) {
    return "TARGET_MEMBER_NOT_LOWER"
  }

  return "ALLOWED"
}

export function resolveBaseRoleAccess(
  context: RoleAccessContext,
  hasManageBaseRole: boolean,
  role: AccountRole,
  hasPermission: (permission: RolePermission) => boolean,
): RoleAccessReason {
  if (!hasManageBaseRole) {
    return "MISSING_PERMISSION"
  }

  const managementAccess = resolveRoleManagementAccess(context, true, role)

  if (managementAccess !== "ALLOWED") {
    return managementAccess
  }

  if (
    !context.account.owner &&
    Object.entries(role.permissions).some(
      ([permission, enabled]) => enabled && !hasPermission(permission as RolePermission),
    )
  ) {
    return "UNGRANTABLE_PERMISSION"
  }

  return "ALLOWED"
}

export function resolveHierarchyBoundary(
  context: RoleAccessContext,
  hasManageHierarchy: boolean,
): number | null {
  if (!hasManageHierarchy) {
    return null
  }

  if (context.account.owner) {
    return Number.NEGATIVE_INFINITY
  }

  return resolveHighestRole(context.actorMember, context.roles)?.hierarchyRank ?? null
}

export function roleAccessReasonTranslationKey(reason: RoleAccessReason): string {
  return `roleEditor.access.${reason}`
}
