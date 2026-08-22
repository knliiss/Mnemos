import { api } from "@/shared/api/httpClient"
import { resolveBadgeImageUrl, type Badge, type UserBadge } from "@/shared/api/badgeApi"

type BadgeResponse = Omit<Badge, "imageUrl"> & {
  imageUrl: string
}

type UserBadgeResponse = Omit<UserBadge, "badge"> & {
  badge: BadgeResponse
}

export async function listAdminBadges(): Promise<Badge[]> {
  const response = await api<BadgeResponse[]>("/api/v1/personalizations/admin/badges")

  return response.map(toBadge)
}

export async function listAdminUserBadges(userId: string): Promise<UserBadge[]> {
  const response = await api<UserBadgeResponse[]>(
    `/api/v1/personalizations/admin/badges/users/${userId}`,
  )

  return response.map((item) => ({
    ...item,
    badge: toBadge(item.badge),
  }))
}

export async function createAdminBadge(request: {
  code: string
  name: string
  image: File
  color: string
}): Promise<Badge> {
  const form = new FormData()
  const metadata = new Blob(
    [
      JSON.stringify({
        code: request.code,
        name: request.name,
        color: request.color,
      }),
    ],
    {
      type: "application/json",
    },
  )

  form.append("metadata", metadata)
  form.append("image", request.image, request.image.name)

  const response = await api<BadgeResponse>("/api/v1/personalizations/admin/badges", {
    method: "POST",
    body: form,
  })

  return toBadge(response)
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

function toBadge(response: BadgeResponse): Badge {
  return {
    ...response,
    imageUrl: resolveBadgeImageUrl(response.imageUrl),
  }
}
