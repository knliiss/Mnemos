import type { RolePermission } from "./model"

export type PermissionGroupKey =
  | "account"
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
    key: "account",
    permissions: [
      "VIEW_ACCOUNT_SETTINGS",
      "EDIT_ACCOUNT_SETTINGS",
      "MANAGE_ACCOUNT_INVITES",
    ],
  },
  {
    key: "members",
    permissions: [
      "VIEW_MEMBERS",
      "INVITE_MEMBERS",
      "REMOVE_MEMBERS",
      "ASSIGN_MEMBER_ROLES",
      "KICK_MEMBERS",
    ],
  },
  {
    key: "roles",
    permissions: [
      "VIEW_ROLES",
      "CREATE_ROLES",
      "EDIT_ROLE_DETAILS",
      "EDIT_ROLE_PERMISSIONS",
      "DELETE_ROLES",
      "MANAGE_ROLE_HIERARCHY",
      "MANAGE_ROLES",
    ],
  },
  {
    key: "wallet",
    permissions: [
      "VIEW_WALLET",
      "VIEW_TRANSACTION_HISTORY",
      "SPEND_WALLET_FUNDS",
    ],
  },
  {
    key: "activity",
    permissions: [
      "CLAIM_DAILY_REWARD",
      "PUBLISH_COMMENTS",
      "PUBLISH_REACTIONS",
      "MODERATE_COMMENTS",
    ],
  },
  {
    key: "bets",
    permissions: [
      "VIEW_BETS",
      "PLACE_BETS",
      "MANAGE_BETS",
      "VIEW_BET_HISTORY",
    ],
  },
  {
    key: "personalization",
    permissions: [
      "VIEW_PERSONALIZATIONS",
      "APPLY_PERSONALIZATIONS",
      "BUY_PERSONALIZATIONS",
      "MANAGE_PERSONALIZATIONS",
    ],
  },
]

export const allRolePermissions = Array.from(
  new Set(rolePermissionGroups.flatMap((group) => group.permissions)),
)
