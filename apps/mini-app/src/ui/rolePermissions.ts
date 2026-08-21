import type { RolePermission } from "./model"

export type PermissionGroupKey =
  | "members"
  | "roles"
  | "wallet"
  | "activity"
  | "bets"
  | "personalization"

export const rolePermissionGroups: {
  key: PermissionGroupKey
  permissions: RolePermission[]
}[] = [
  {
    key: "members",
    permissions: ["MANAGE_MEMBERS", "ASSIGN_MEMBER_ROLES"],
  },
  {
    key: "roles",
    permissions: ["MANAGE_ROLES", "MANAGE_ROLE_HIERARCHY", "MANAGE_BASE_ROLE"],
  },
  {
    key: "wallet",
    permissions: ["VIEW_TRANSACTION_HISTORY"],
  },
  {
    key: "activity",
    permissions: ["MODERATE_COMMENTS"],
  },
  {
    key: "bets",
    permissions: ["PLACE_BETS"],
  },
  {
    key: "personalization",
    permissions: ["BUY_ACCOUNT_PERSONALIZATIONS", "APPLY_ACCOUNT_PERSONALIZATIONS"],
  },
]

export const allRolePermissions = Array.from(
  new Set(rolePermissionGroups.flatMap((group) => group.permissions)),
)
