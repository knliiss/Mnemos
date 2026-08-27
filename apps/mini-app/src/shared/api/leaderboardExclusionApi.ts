import { api } from "@/shared/api/httpClient"

export type LeaderboardExclusionSubjectType = "ACCOUNT" | "PLAYER"
export type LeaderboardExclusionScope = "ALL" | "COINS" | "DROPS" | "BOOSTERS"

export type LeaderboardExclusionResponse = {
  id: string
  subjectType: LeaderboardExclusionSubjectType
  subjectId: string
  scope: LeaderboardExclusionScope
  reason: string | null
  createdByUserId: string
  createdAt: string
}

export type CreateLeaderboardExclusionRequest = {
  subjectType: LeaderboardExclusionSubjectType
  subjectId: string
  scope: LeaderboardExclusionScope
  reason: string | null
}

export async function listLeaderboardExclusions(): Promise<LeaderboardExclusionResponse[]> {
  return api<LeaderboardExclusionResponse[]>("/api/v1/moderation/leaderboard-exclusions")
}

export async function createLeaderboardExclusion(
  request: CreateLeaderboardExclusionRequest,
): Promise<LeaderboardExclusionResponse> {
  return api<LeaderboardExclusionResponse>("/api/v1/moderation/leaderboard-exclusions", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  })
}

export async function deleteLeaderboardExclusion(id: string): Promise<void> {
  await api<void>(`/api/v1/moderation/leaderboard-exclusions/${id}`, {
    method: "DELETE",
  })
}
