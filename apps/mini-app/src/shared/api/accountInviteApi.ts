import { api } from "@/shared/api/httpClient"

export type AccountInviteStatus = "ACTIVE" | "CONSUMED" | "REVOKED" | "EXPIRED"

export type AccountInvite = {
  id: string
  code: string
  createdByUserId: string
  createdAt: string
  expiresAt: string | null
  consumedAt: string | null
  revokedAt: string | null
  status: AccountInviteStatus
}

export type CreatedAccountInvite = {
  id: string
  code: string
  createdAt: string
  expiresAt: string | null
}

export async function getAccountInvites(accountId: string): Promise<AccountInvite[]> {
  return api<AccountInvite[]>(`/api/v1/accounts/${accountId}/invites`)
}

export async function createAccountInvite(
  accountId: string,
  expiresInMinutes: number | null,
): Promise<CreatedAccountInvite> {
  return api<CreatedAccountInvite>(`/api/v1/accounts/${accountId}/invites`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ expiresInMinutes }),
  })
}

export async function revokeAccountInvite(accountId: string, inviteId: string): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/invites/${inviteId}`, { method: "DELETE" })
}
