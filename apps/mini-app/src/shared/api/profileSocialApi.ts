import { api } from "@/shared/api/httpClient"
import type { PageResponse } from "@/shared/api/backendContracts"
import type { EffectType, FrameStyle, VisualLayer } from "@/ui/model"
import type { PlayerPresenceStatus } from "@/shared/api/playerDirectoryApi"

export type AccountReactionType = "LIKE" | "DISLIKE"

export type ProfileGlowResponse = {
  color: string
  strength: number
}

export type ProfileAppearanceResponse = {
  nickStyle: {
    personalizationId: string
    colors: Array<{
      color: string
      position: number
    }>
    angleDegrees: number
    glow: ProfileGlowResponse | null
  } | null
  profileTheme: {
    personalizationId: string
    background: string
    backgroundSecondary: string
    surface: string
    surfaceSecondary: string
    accent: string
    text: string
    border: string
    gradientAngleDegrees: number
    textureStrength: number
    glow: ProfileGlowResponse | null
    layers: VisualLayer[]
  } | null
  profileBackground?: {
    personalizationId: string
    effect: EffectType
    intensity: number
    speed: number
    density: number
    scale: number
    primaryColor: string
    secondaryColor: string | null
    layers: VisualLayer[]
  } | null
  avatarFrame: {
    personalizationId: string
    borderColor: string
    secondaryBorderColor: string
    borderWidthPx: number
    gapPx: number
    radiusPx: number
    style: FrameStyle
    glow: ProfileGlowResponse | null
    layers: VisualLayer[]
  } | null
  profileEffect: {
    personalizationId: string
    effect: EffectType
    intensity: number
    speed: number
    density: number
    scale: number
    primaryColor: string
    secondaryColor: string | null
    layers: VisualLayer[]
  } | null
}

export type PublicProfilePresenceResponse = {
  status: PlayerPresenceStatus
  online: boolean | null
  realm: string | null
  lastSeenOnline: string | null
  observedAt: string | null
}

export type PublicProfileResponse = {
  accountId: string
  cristalixUuid: string
  cristalixNickname: string
  likes: number
  dislikes: number
  comments: number
  appearance: ProfileAppearanceResponse
  presence: PublicProfilePresenceResponse
  createdAt: string
}

export type CommentResponse = {
  id: string
  targetAccountId: string
  authorAccountId: string
  parentCommentId: string | null
  replyToAuthorAccountId: string | null
  content: string | null
  deleted: boolean
  canEdit: boolean
  canDelete: boolean
  likes: number
  replies: number
  createdAt: string
  updatedAt: string
}

export type SpringPageResponse<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export async function searchPublicProfiles(
  query: string,
  page = 0,
  size = 20,
): Promise<SpringPageResponse<PublicProfileResponse>> {
  const params = new URLSearchParams({
    q: query.trim(),
    page: String(page),
    size: String(size),
  })

  const response = await api<PageResponse<PublicProfileResponse>>(`/api/v1/profiles?${params}`)

  return toSpringPageResponse(response)
}

export async function getPublicProfile(accountId: string): Promise<PublicProfileResponse> {
  return api<PublicProfileResponse>(`/api/v1/profiles/${accountId}`)
}

export async function getAccountReaction(accountId: string): Promise<AccountReactionType | null> {
  const response = await api<{ type: AccountReactionType | null }>(
    `/api/v1/accounts/${accountId}/reaction`,
  )
  return response.type
}

export async function getAccountReactionSummary(
  accountId: string,
): Promise<{ likes: number; dislikes: number }> {
  return api<{ likes: number; dislikes: number }>(`/api/v1/accounts/${accountId}/reaction/summary`)
}

export async function setAccountReaction(
  accountId: string,
  type: AccountReactionType,
): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/reaction`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ type }),
  })
}

export async function removeAccountReaction(accountId: string): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/reaction`, { method: "DELETE" })
}

export async function getAccountComments(
  accountId: string,
  page = 0,
  size = 20,
): Promise<SpringPageResponse<CommentResponse>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  const response = await api<PageResponse<CommentResponse>>(
    `/api/v1/accounts/${accountId}/comments?${params}`,
  )

  return toSpringPageResponse(response)
}

export async function getCommentReplies(
  commentId: string,
  page = 0,
  size = 50,
): Promise<SpringPageResponse<CommentResponse>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  const response = await api<PageResponse<CommentResponse>>(
    `/api/v1/comments/${commentId}/replies?${params}`,
  )

  return toSpringPageResponse(response)
}

export async function createComment(
  accountId: string,
  content: string,
  parentCommentId: string | null = null,
  replyToCommentId: string | null = null,
): Promise<CommentResponse> {
  return api<CommentResponse>(`/api/v1/accounts/${accountId}/comments`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      parentCommentId,
      replyToCommentId,
      content: content.trim(),
    }),
  })
}

export async function editComment(commentId: string, content: string): Promise<CommentResponse> {
  return api<CommentResponse>(`/api/v1/comments/${commentId}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ content: content.trim() }),
  })
}

export async function deleteComment(commentId: string): Promise<void> {
  await api<void>(`/api/v1/comments/${commentId}`, { method: "DELETE" })
}

export async function getCommentLikeState(commentId: string): Promise<boolean> {
  const response = await api<{ liked: boolean }>(`/api/v1/comments/${commentId}/like`)
  return response.liked
}

export async function getCommentLikeCount(commentId: string): Promise<number> {
  const response = await api<{ likes: number }>(`/api/v1/comments/${commentId}/like/count`)
  return response.likes
}

export async function likeComment(commentId: string): Promise<void> {
  await api<void>(`/api/v1/comments/${commentId}/like`, { method: "PUT" })
}

export async function unlikeComment(commentId: string): Promise<void> {
  await api<void>(`/api/v1/comments/${commentId}/like`, { method: "DELETE" })
}

function toSpringPageResponse<T>(response: PageResponse<T>): SpringPageResponse<T> {
  return {
    content: response.items,
    totalElements: response.totalItems,
    totalPages: response.totalPages,
    number: response.page,
    size: response.size,
    numberOfElements: response.items.length,
    first: response.page === 0,
    last: !response.hasNextPage,
    empty: response.items.length === 0,
  }
}
