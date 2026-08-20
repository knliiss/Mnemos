import type {
  AccountMemberResponse,
  AccountRoleResponse,
} from "@/shared/api/backendContracts"
import { api } from "@/shared/api/httpClient"

export async function selectActiveAccount(accountId: string): Promise<void> {
  await api<void>(`/api/v1/me/accounts/active/${accountId}`, { method: "PUT" })
}

export async function createAccount(
  cristalixNickname: string,
): Promise<{ accountId: string; memberId: string }> {
  return api<{ accountId: string; memberId: string }>("/api/v1/accounts", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ cristalixNickname }),
  })
}

export async function joinAccount(
  inviteCode: string,
): Promise<{ accountId: string; memberId: string }> {
  return api<{ accountId: string; memberId: string }>("/api/v1/account-memberships", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ inviteCode }),
  })
}

export async function leaveActiveAccount(): Promise<void> {
  await api<void>("/api/v1/me/accounts/active/leave", { method: "POST" })
}

export async function reactivateAccount(accountId: string): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/reactivate`, { method: "POST" })
}

export async function getAccountMembers(accountId: string): Promise<AccountMemberResponse[]> {
  return api<AccountMemberResponse[]>(`/api/v1/accounts/${accountId}/members`)
}

export async function kickAccountMember(accountId: string, memberId: string): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/members/${memberId}`, { method: "DELETE" })
}

export async function getAccountRoles(accountId: string): Promise<AccountRoleResponse[]> {
  return api<AccountRoleResponse[]>(`/api/v1/accounts/${accountId}/roles`)
}

export async function getMemberRoles(
  accountId: string,
  memberId: string,
): Promise<AccountRoleResponse[]> {
  return api<AccountRoleResponse[]>(`/api/v1/accounts/${accountId}/members/${memberId}/roles`)
}

export async function assignMemberRole(
  accountId: string,
  memberId: string,
  roleId: string,
): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/members/${memberId}/roles/${roleId}`, {
    method: "PUT",
  })
}

export async function revokeMemberRole(
  accountId: string,
  memberId: string,
  roleId: string,
): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/members/${memberId}/roles/${roleId}`, {
    method: "DELETE",
  })
}
