import { initialMembers, initialRoles, permissionGroups } from "../mockData"
import type { AccountRole, RolePermission } from "../model"

export type RolePreset = {
  id: string
  title: string
  note: string
  sourceRoleId: string
}

const storageKey = "mnemos.mock.roles.v4"

export const rolePresets: RolePreset[] = [
  {
    id: "preset-member",
    title: "Базова",
    note: "Перегляд, ставки та персоналізації",
    sourceRoleId: "role-member",
  },
  {
    id: "preset-moderation",
    title: "Модерація",
    note: "Учасники, коментарі та історія ставок",
    sourceRoleId: "role-moderator",
  },
  {
    id: "preset-management",
    title: "Керування",
    note: "Майже всі права без системної ієрархії",
    sourceRoleId: "role-admin",
  },
]

export const roleColors = [
  "#D8D4C9",
  "#C08B67",
  "#A985A8",
  "#D4FF42",
  "#68B8C8",
  "#E9B45F",
  "#D17178",
  "#8D8B84",
]

export function loadMockRoles(): AccountRole[] {
  if (typeof window === "undefined") return initialRoles

  const raw = window.localStorage.getItem(storageKey)
  if (!raw) return initialRoles

  try {
    const parsed = JSON.parse(raw) as AccountRole[]

    return Array.isArray(parsed) && parsed.length ? parsed : initialRoles
  } catch {
    return initialRoles
  }
}

export function normalizeRoles(roles: AccountRole[]): AccountRole[] {
  const locked = roles.filter((role) => role.locked)
  const editable = roles.filter((role) => !role.locked)
  const ordered = [...locked, ...editable]

  return ordered.map((role, index) => ({
    ...role,
    hierarchyRank: (index + 1) * 10,
  }))
}

export function persistMockRoles(roles: AccountRole[]): AccountRole[] {
  const normalized = normalizeRoles(roles)

  if (typeof window !== "undefined") {
    window.localStorage.setItem(storageKey, JSON.stringify(normalized))
  }

  return normalized
}

export function createEmptyPermissions(): Record<RolePermission, boolean> {
  return Object.fromEntries(
    permissionGroups
      .flatMap((group) => group.permissions)
      .map((permission) => [permission, false]),
  ) as Record<RolePermission, boolean>
}

export function enabledPermissions(role: AccountRole): number {
  return Object.values(role.permissions).filter(Boolean).length
}

export function memberCount(role: AccountRole): number {
  return initialMembers.filter((member) => member.roleIds.includes(role.id)).length
}
