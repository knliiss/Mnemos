import { api } from "@/shared/api/httpClient"

export type CollectorCredentialResponse = {
  id: string
  name: string
  enabled: boolean
  createdAt: string
  updatedAt: string
  revokedAt: string | null
  accessKey: string | null
}

export type CollectorPresenceResponse = {
  activeCollectors: number
  revision: number
  activeCollectorIds: string[]
}

export type CollectorActivationResponse = {
  token: string
  expiresAt: string
}

export async function listCollectorCredentials(): Promise<CollectorCredentialResponse[]> {
  return api<CollectorCredentialResponse[]>("/api/v1/realtime/admin/collectors")
}

export async function getCollectorPresence(): Promise<CollectorPresenceResponse> {
  return api<CollectorPresenceResponse>("/api/v1/realtime/admin/collectors/presence")
}

export async function createCollectorActivation(): Promise<CollectorActivationResponse> {
  return api<CollectorActivationResponse>("/api/v1/realtime/collector-activations", {
    method: "POST",
  })
}

export async function enableCollectorCredential(
  credentialId: string,
): Promise<CollectorCredentialResponse> {
  return api<CollectorCredentialResponse>(
    `/api/v1/realtime/admin/collectors/${credentialId}/enable`,
    {
      method: "POST",
    },
  )
}

export async function disableCollectorCredential(
  credentialId: string,
): Promise<CollectorCredentialResponse> {
  return api<CollectorCredentialResponse>(
    `/api/v1/realtime/admin/collectors/${credentialId}/disable`,
    {
      method: "POST",
    },
  )
}

export async function revokeCollectorCredential(
  credentialId: string,
): Promise<CollectorCredentialResponse> {
  return api<CollectorCredentialResponse>(`/api/v1/realtime/admin/collectors/${credentialId}`, {
    method: "DELETE",
  })
}
