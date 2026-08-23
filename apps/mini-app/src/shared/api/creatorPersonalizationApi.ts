import type {
  Currency,
  EffectType,
  FrameStyle,
  NotificationTemplateKey,
  PersonalizationType,
  VisualLayerAnimation,
  VisualLayerBlendMode,
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

export type VisualLayerRequest = {
  id: string
  assetId: string
  xPercent: number
  yPercent: number
  widthPercent: number
  opacity: number
  rotationDegrees: number
  blendMode: VisualLayerBlendMode
  animation: VisualLayerAnimation
  zIndex: number
}

export type PersonalizationAssetResponse = {
  id: string
  contentUrl: string
  contentType: string
  sizeBytes: number
  widthPx: number
  heightPx: number
  createdAt: string
}

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
  layers: VisualLayerRequest[]
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
  layers: VisualLayerRequest[]
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
  layers: VisualLayerRequest[]
  price: PersonalizationPriceRequest
}

export type CreateNotificationPackRequest = {
  name: string
  mixedAggregateTitle: string
  templates: Record<NotificationTemplateKey, NotificationTemplateRequest>
  price: PersonalizationPriceRequest
}

export type NotificationPackEditorFieldKey =
  | "SOLO_TITLE"
  | "SOLO_BODY"
  | "COMPACT"
  | "AGGREGATE_TITLE"

export type NotificationPackEditorDescriptorResponse = {
  mixedAggregateTitleDefault: string
  maxTextLength: number
  maxEmojiLength: number
  fields: Array<{
    key: NotificationPackEditorFieldKey
    variablesAllowed: boolean
  }>
  templates: Array<{
    type: NotificationTemplateKey
    variables: Array<{
      key: string
      sampleValue: string
    }>
    sampleValues: Record<string, string>
    defaults: NotificationTemplateResponse
  }>
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

export async function listCreatorPersonalizationAssets(): Promise<PersonalizationAssetResponse[]> {
  return api<PersonalizationAssetResponse[]>("/api/v1/personalizations/creator/assets")
}

export async function uploadCreatorPersonalizationAsset(
  file: File,
): Promise<PersonalizationAssetResponse> {
  const body = new FormData()

  body.append("file", file)

  return api<PersonalizationAssetResponse>("/api/v1/personalizations/creator/assets", {
    method: "POST",
    body,
  })
}

export async function getNotificationPackEditorDescriptor(): Promise<NotificationPackEditorDescriptorResponse> {
  return api<NotificationPackEditorDescriptorResponse>(
    "/api/v1/personalizations/creator/notification-packs/descriptor",
  )
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
