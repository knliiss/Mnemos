import type {
  Currency,
  EffectType,
  FrameStyle,
  NotificationTemplateKey,
  PersonalizationType,
} from "@/ui/model"
import { api } from "@/shared/api/httpClient"
import type {
  GlowStyleResponse,
  GradientStopResponse,
  NotificationTemplateResponse,
  PersonalizationResponse,
} from "@/shared/api/personalizationApi"

export type PersonalizationPriceRequest = {
  currency: Currency
  amount: number
}

export type GlowStyleRequest = GlowStyleResponse
export type GradientStopRequest = GradientStopResponse
export type NotificationTemplateRequest = NotificationTemplateResponse

export type CreateNickStyleRequest = {
  name: string
  colors: GradientStopRequest[]
  angleDegrees: number
  glow: GlowStyleRequest | null
  price: PersonalizationPriceRequest
}

export type CreateProfileThemeRequest = {
  name: string
  background: string
  backgroundSecondary: string
  surface: string
  surfaceSecondary: string
  accent: string
  text: string
  border: string
  gradientAngleDegrees: number
  textureStrength: number
  glow: GlowStyleRequest | null
  price: PersonalizationPriceRequest
}

export type CreateAvatarFrameRequest = {
  name: string
  borderColor: string
  secondaryBorderColor: string
  borderWidthPx: number
  gapPx: number
  radiusPx: number
  style: FrameStyle
  glow: GlowStyleRequest | null
  price: PersonalizationPriceRequest
}

export type CreateProfileEffectRequest = {
  name: string
  effect: EffectType
  intensity: number
  speed: number
  density: number
  scale: number
  primaryColor: string
  secondaryColor: string | null
  price: PersonalizationPriceRequest
}

export type CreateNotificationPackRequest = {
  name: string
  mixedAggregateTitle: string
  templates: Record<NotificationTemplateKey, NotificationTemplateRequest>
  price: PersonalizationPriceRequest
}

export type CreatorRequestByType = {
  NICK_STYLE: CreateNickStyleRequest
  PROFILE_THEME: CreateProfileThemeRequest
  AVATAR_FRAME: CreateAvatarFrameRequest
  PROFILE_EFFECT: CreateProfileEffectRequest
  NOTIFICATION_PACK: CreateNotificationPackRequest
}

export type CreatorRevenueSummaryResponse = {
  confirmedPrime: number
  pendingPrime: number
  failedPrime: number
  confirmedOperations: number
  pendingOperations: number
  failedOperations: number
}

export type CreatorRevenueOperationResponse = {
  id: string
  purchaseOperationId: string
  amountPrime: number
  status: "PENDING" | "CONFIRMED" | "FAILED"
  walletTransactionId: string | null
  failureReason: string | null
  nextRetryAt: string | null
  createdAt: string
}

const createPaths: Record<PersonalizationType, string> = {
  NICK_STYLE: "nick-styles",
  PROFILE_THEME: "profile-themes",
  AVATAR_FRAME: "avatar-frames",
  PROFILE_EFFECT: "profile-effects",
  NOTIFICATION_PACK: "notification-packs",
}

const updatePaths: Record<PersonalizationType, string> = {
  NICK_STYLE: "nick-style",
  PROFILE_THEME: "profile-theme",
  AVATAR_FRAME: "avatar-frame",
  PROFILE_EFFECT: "profile-effect",
  NOTIFICATION_PACK: "notification-pack",
}

export async function listCreatorPersonalizations(): Promise<PersonalizationResponse[]> {
  return api<PersonalizationResponse[]>("/api/v1/personalizations/creator/me")
}

export async function getCreatorPersonalization(
  personalizationId: string,
): Promise<PersonalizationResponse> {
  return api<PersonalizationResponse>(`/api/v1/personalizations/creator/${personalizationId}`)
}

export async function createCreatorPersonalization<TType extends PersonalizationType>(
  type: TType,
  request: CreatorRequestByType[TType],
): Promise<PersonalizationResponse> {
  return api<PersonalizationResponse>(`/api/v1/personalizations/creator/${createPaths[type]}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  })
}

export async function updateCreatorPersonalization<TType extends PersonalizationType>(
  personalizationId: string,
  type: TType,
  request: CreatorRequestByType[TType],
): Promise<PersonalizationResponse> {
  return api<PersonalizationResponse>(
    `/api/v1/personalizations/creator/${personalizationId}/${updatePaths[type]}`,
    {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(request),
    },
  )
}

export async function submitCreatorPersonalization(
  personalizationId: string,
): Promise<PersonalizationResponse> {
  return api<PersonalizationResponse>(
    `/api/v1/personalizations/creator/${personalizationId}/submit`,
    {
      method: "POST",
    },
  )
}

export async function publishCreatorPersonalization(
  personalizationId: string,
): Promise<PersonalizationResponse> {
  return api<PersonalizationResponse>(
    `/api/v1/personalizations/creator/${personalizationId}/publish`,
    {
      method: "POST",
    },
  )
}

export async function getCreatorRevenueSummary(): Promise<CreatorRevenueSummaryResponse> {
  return api<CreatorRevenueSummaryResponse>("/api/v1/personalizations/creator/revenue/summary")
}

export async function getCreatorRevenueOperations(): Promise<CreatorRevenueOperationResponse[]> {
  return api<CreatorRevenueOperationResponse[]>(
    "/api/v1/personalizations/creator/revenue/operations",
  )
}
