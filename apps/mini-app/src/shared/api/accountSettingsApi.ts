import { api } from "@/shared/api/httpClient"

export async function updateAccountBaseRole(
  accountId: string,
  roleId: string | null,
): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/base-role`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ roleId }),
  })
}

export async function transferAccountOwnership(accountId: string, memberId: string): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/owner/${memberId}`, { method: "PUT" })
}

export async function deactivateAccount(accountId: string): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}`, { method: "DELETE" })
}
