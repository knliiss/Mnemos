import { api } from "@/shared/api/httpClient"

export type BadgeResponse = {
  id: string
  code: string
  name: string
  icon: string
  color: string
}

export type UserBadgeResponse = {
  badge: BadgeResponse
  grantedAt: string
  active: boolean
}

export async function listAdminBadges(): Promise<BadgeResponse[]> {
  return api<BadgeResponse[]>("/api/v1/personalizations/admin/badges")
}

export async function listAdminUserBadges(userId: string): Promise<UserBadgeResponse[]> {
  return api<UserBadgeResponse[]>(`/api/v1/personalizations/admin/badges/users/${userId}`)
}

export async function createAdminBadge(request: {
  code: string
  name: string
  icon: string
  color: string
}): Promise<BadgeResponse> {
  return api<BadgeResponse>("/api/v1/personalizations/admin/badges", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  })
}

export async function grantAdminBadge(badgeId: string, userId: string): Promise<void> {
  await api<void>(`/api/v1/personalizations/admin/badges/${badgeId}/users/${userId}`, {
    method: "POST",
  })
}

export async function revokeAdminBadge(badgeId: string, userId: string): Promise<void> {
  await api<void>(`/api/v1/personalizations/admin/badges/${badgeId}/users/${userId}`, {
    method: "DELETE",
  })
}
