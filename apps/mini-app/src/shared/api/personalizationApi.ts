import type { PageResponse } from "@/shared/api/backendContracts"
import { api } from "@/shared/api/httpClient"
import type {
  Currency,
  EffectType,
  FrameStyle,
  NotificationTemplateKey,
  PersonalizationStatus,
  PersonalizationTarget,
  PersonalizationType,
} from "@/ui/model"

export type GradientStopResponse = {
  color: string
  position: number
}

export type GlowStyleResponse = {
  color: string
  strength: number
}

export type NotificationTemplateResponse = {
  emoji: string | null
  soloTitle: string
  soloBody: string
  compact: string
  aggregateTitle: string
}

export type NotificationPackContentResponse = {
  mixedAggregateTitle: string
  templates: Record<NotificationTemplateKey, NotificationTemplateResponse>
}

export type NickStyleContentResponse = {
  colors: GradientStopResponse[]
  angleDegrees: number
  glow: GlowStyleResponse | null
}

export type ProfileThemeContentResponse = {
  background: string
  backgroundSecondary: string
  surface: string
  surfaceSecondary: string
  accent: string
  text: string
  border: string
  gradientAngleDegrees: number
  textureStrength: number
  glow: GlowStyleResponse | null
}

export type AvatarFrameContentResponse = {
  borderColor: string
  secondaryBorderColor: string
  borderWidthPx: number
  gapPx: number
  radiusPx: number
  style: FrameStyle
  glow: GlowStyleResponse | null
}

export type ProfileEffectContentResponse = {
  effect: EffectType
  intensity: number
  speed: number
  density: number
  scale: number
  primaryColor: string
  secondaryColor: string | null
}

export type PersonalizationContentResponse =
  | NotificationPackContentResponse
  | NickStyleContentResponse
  | ProfileThemeContentResponse
  | AvatarFrameContentResponse
  | ProfileEffectContentResponse

export type PersonalizationResponse = {
  id: string
  creatorUserId: string
  type: PersonalizationType
  origin: string
  visibility: string
  name: string
  content: PersonalizationContentResponse
  price: {
    currency: Currency
    amount: number
  } | null
  status: PersonalizationStatus
  rejectionReason: string | null
  createdAt: string
  updatedAt: string
}

export type PersonalizationPriceRangeResponse = {
  type: PersonalizationType
  origin: string
  currency: Currency
  min: number
  max: number
}

export type PersonalizationPricingResponse = {
  ranges: PersonalizationPriceRangeResponse[]
  customNotificationPackPurchasePrice: {
    currency: Currency
    amount: number
  }
}

export type OwnedPersonalizationResponse = {
  personalizationId: string
  ownerType: PersonalizationTarget
  ownerId: string
  type: PersonalizationType
  acquiredAt: string
  active: boolean
}

export type PurchasePersonalizationResponse = {
  personalizationId: string
  operationId: string
}

export type PurchaseOperationResponse = {
  operationId: string
  personalizationId: string
  ownerType: PersonalizationTarget
  ownerId: string
  currency: Currency
  amount: number
  status: "PENDING" | "CONFIRMED" | "FAILED"
  walletTransactionId: string | null
  failureReason: string | null
  createdAt: string
}

export async function listPersonalizations(
  type: PersonalizationType,
  currency?: Currency,
  page = 0,
  size = 100,
): Promise<PageResponse<PersonalizationResponse>> {
  const params = new URLSearchParams({
    type,
    page: String(page),
    size: String(size),
  })

  if (currency) {
    params.set("currency", currency)
  }

  return api<PageResponse<PersonalizationResponse>>(`/api/v1/personalizations?${params}`)
}

export async function getPersonalization(
  personalizationId: string,
): Promise<PersonalizationResponse> {
  return api<PersonalizationResponse>(`/api/v1/personalizations/${personalizationId}`)
}

export async function getPersonalizationPricing(): Promise<PersonalizationPricingResponse> {
  return api<PersonalizationPricingResponse>("/api/v1/personalizations/pricing")
}

export async function listOwnedPersonalizations(
  type?: PersonalizationType,
  page = 0,
  size = 100,
): Promise<PageResponse<OwnedPersonalizationResponse>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })

  if (type) {
    params.set("type", type)
  }

  return api<PageResponse<OwnedPersonalizationResponse>>(
    `/api/v1/personalizations/ownership/me?${params}`,
  )
}

export async function activatePersonalization(
  personalizationId: string,
): Promise<OwnedPersonalizationResponse> {
  return api<OwnedPersonalizationResponse>(
    `/api/v1/personalizations/ownership/${personalizationId}/active`,
    {
      method: "PUT",
    },
  )
}

export async function deactivatePersonalization(
  personalizationId: string,
): Promise<OwnedPersonalizationResponse> {
  return api<OwnedPersonalizationResponse>(
    `/api/v1/personalizations/ownership/${personalizationId}/active`,
    {
      method: "DELETE",
    },
  )
}

export async function purchasePersonalization(
  personalizationId: string,
): Promise<PurchasePersonalizationResponse> {
  return api<PurchasePersonalizationResponse>(
    `/api/v1/personalizations/${personalizationId}/purchase`,
    {
      method: "POST",
      headers: { "Idempotency-Key": crypto.randomUUID() },
    },
  )
}

export async function getPurchaseOperation(
  operationId: string,
): Promise<PurchaseOperationResponse> {
  return api<PurchaseOperationResponse>(`/api/v1/personalizations/purchases/${operationId}`)
}
