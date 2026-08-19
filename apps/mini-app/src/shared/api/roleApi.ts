import type { AccountRoleResponse } from "@/shared/api/backendContracts"
import { api } from "@/shared/api/httpClient"
import type { RolePermission } from "@/product-ui/model"

export async function createRole(
  accountId: string,
  name: string,
  color: string,
  permissions: RolePermission[],
): Promise<AccountRoleResponse> {
  return api<AccountRoleResponse>(`/api/v1/accounts/${accountId}/roles`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, color, permissions }),
  })
}

export async function updateRoleDetails(
  accountId: string,
  roleId: string,
  name: string,
  color: string,
): Promise<AccountRoleResponse> {
  return api<AccountRoleResponse>(`/api/v1/accounts/${accountId}/roles/${roleId}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, color }),
  })
}

export async function updateRolePermissions(
  accountId: string,
  roleId: string,
  permissions: Record<RolePermission, boolean>,
): Promise<AccountRoleResponse> {
  return api<AccountRoleResponse>(`/api/v1/accounts/${accountId}/roles/${roleId}/permissions`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ permissions }),
  })
}

export async function updateRoleHierarchy(
  accountId: string,
  orderedRoleIds: string[],
): Promise<AccountRoleResponse[]> {
  return api<AccountRoleResponse[]>(`/api/v1/accounts/${accountId}/roles/hierarchy`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ orderedRoleIds }),
  })
}

export async function deleteRole(accountId: string, roleId: string): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/roles/${roleId}`, { method: "DELETE" })
}
