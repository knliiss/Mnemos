import { api } from "@/shared/api/httpClient"
import { runtimeConfig } from "@/shared/config/runtimeConfig"

export type Badge = {
  id: string
  code: string
  name: string
  imageUrl: string
  color: string
}

export type UserBadge = {
  badge: Badge
  grantedAt: string
  active: boolean
}

type BadgeResponse = Omit<Badge, "imageUrl"> & {
  imageUrl: string
}

type UserBadgeResponse = Omit<UserBadge, "badge"> & {
  badge: BadgeResponse
}

export async function listMyBadges(): Promise<UserBadge[]> {
  const response = await api<UserBadgeResponse[]>("/api/v1/personalizations/badges/me")

  return response.map(toUserBadge)
}

export async function activateMyBadge(badgeId: string): Promise<UserBadge> {
  const response = await api<UserBadgeResponse>(
    `/api/v1/personalizations/badges/${badgeId}/active`,
    {
      method: "PUT",
    },
  )

  return toUserBadge(response)
}

export async function deactivateMyBadge(): Promise<void> {
  await api<void>("/api/v1/personalizations/badges/active", {
    method: "DELETE",
  })
}

export function resolveBadgeImageUrl(path: string): string {
  if (/^https?:\/\//i.test(path)) {
    return path
  }

  const apiOrigin = runtimeConfig.apiUrl.replace(/\/$/, "")
  const normalizedPath = path.startsWith("/") ? path : `/${path}`

  return `${apiOrigin}${normalizedPath}`
}

function toUserBadge(response: UserBadgeResponse): UserBadge {
  return {
    ...response,
    badge: {
      ...response.badge,
      imageUrl: resolveBadgeImageUrl(response.badge.imageUrl),
    },
  }
}
