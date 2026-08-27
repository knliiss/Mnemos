import { api } from "@/shared/api/httpClient"

export type PlayerPresenceStatus = "ONLINE" | "OFFLINE" | "UNKNOWN" | "HIDDEN"

export type PlayerResponse = {
  playerId: string
  nickname: string
  presenceStatus: PlayerPresenceStatus
  realm: string | null
  realmType: string | null
  privacyHidden: boolean
  lastSeenOnline: string | null
  observedAt: string
}

export type PresenceSubscriptionResponse = {
  id: string
  playerId: string
  nickname: string | null
  presenceStatus: PlayerPresenceStatus | null
  realm: string | null
  lastSeenOnline: string | null
  createdAt: string
}

export type SaoRealmPlayerResponse = {
  playerId: string
  nickname: string
  accountId: string | null
  observedAt: string
}

export type SaoRealmResponse = {
  name: string
  number: number
  playerCount: number
  players: SaoRealmPlayerResponse[]
}

export type SaoRealmSnapshotResponse = {
  generatedAt: string
  trackedOnline: number
  realms: SaoRealmResponse[]
}

export async function resolvePlayerByNickname(nickname: string): Promise<PlayerResponse> {
  const params = new URLSearchParams({ nickname: nickname.trim() })

  return api<PlayerResponse>(`/api/v1/players/resolve?${params}`)
}

export async function getSaoRealmSnapshot(): Promise<SaoRealmSnapshotResponse> {
  return api<SaoRealmSnapshotResponse>("/api/v1/realms/sao")
}

export async function listPresenceSubscriptions(): Promise<PresenceSubscriptionResponse[]> {
  return api<PresenceSubscriptionResponse[]>("/api/v1/player-presence-subscriptions")
}

export async function createPresenceSubscription(
  playerId: string,
): Promise<PresenceSubscriptionResponse> {
  return api<PresenceSubscriptionResponse>("/api/v1/player-presence-subscriptions", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ playerId }),
  })
}

export async function deletePresenceSubscription(playerId: string): Promise<void> {
  await api<void>(`/api/v1/player-presence-subscriptions/${playerId}`, {
    method: "DELETE",
  })
}
