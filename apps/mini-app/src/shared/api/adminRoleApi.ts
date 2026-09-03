import { api } from "@/shared/api/httpClient"
import type { AdminAccountRoleResponse } from "@/shared/api/moderationApi"

export type CreateAdminRoleRequest = {
  name: string
  color: string
  permissions: string[]
}

export type UpdateAdminRoleDetailsRequest = {
  name?: string
  color?: string
}

export type UpdateAdminRolePermissionsRequest = {
  permissions: Record<string, boolean>
}

export async function createAdminRole(
  accountId: string,
  request: CreateAdminRoleRequest,
): Promise<AdminAccountRoleResponse> {
  return api<AdminAccountRoleResponse>(`/api/v1/admin/accounts/${accountId}/roles`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  })
}

export async function updateAdminRoleDetails(
  accountId: string,
  roleId: string,
  request: UpdateAdminRoleDetailsRequest,
): Promise<AdminAccountRoleResponse> {
  return api<AdminAccountRoleResponse>(`/api/v1/admin/accounts/${accountId}/roles/${roleId}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  })
}

export async function updateAdminRolePermissions(
  accountId: string,
  roleId: string,
  request: UpdateAdminRolePermissionsRequest,
): Promise<AdminAccountRoleResponse> {
  return api<AdminAccountRoleResponse>(
    `/api/v1/admin/accounts/${accountId}/roles/${roleId}/permissions`,
    {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(request),
    },
  )
}

export async function updateAdminRoleHierarchy(
  accountId: string,
  orderedRoleIds: string[],
): Promise<AdminAccountRoleResponse[]> {
  return api<AdminAccountRoleResponse[]>(`/api/v1/admin/accounts/${accountId}/roles/hierarchy`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ orderedRoleIds }),
  })
}

export async function updateAdminBaseRole(accountId: string, roleId: string): Promise<void> {
  await api<void>(`/api/v1/admin/accounts/${accountId}/roles/base/${roleId}`, { method: "PUT" })
}

export async function clearAdminBaseRole(accountId: string): Promise<void> {
  await api<void>(`/api/v1/admin/accounts/${accountId}/roles/base`, { method: "DELETE" })
}

export async function deleteAdminRole(accountId: string, roleId: string): Promise<void> {
  await api<void>(`/api/v1/admin/accounts/${accountId}/roles/${roleId}`, { method: "DELETE" })
}

export async function assignAdminMemberRole(
  accountId: string,
  memberId: string,
  roleId: string,
): Promise<void> {
  await api<void>(`/api/v1/admin/accounts/${accountId}/members/${memberId}/roles/${roleId}`, {
    method: "PUT",
  })
}

export async function revokeAdminMemberRole(
  accountId: string,
  memberId: string,
  roleId: string,
): Promise<void> {
  await api<void>(`/api/v1/admin/accounts/${accountId}/members/${memberId}/roles/${roleId}`, {
    method: "DELETE",
  })
}
