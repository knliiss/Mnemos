import type {
  AvatarFrameContentResponse,
  NickStyleContentResponse,
  NotificationPackContentResponse,
  OwnedPersonalizationResponse,
  PersonalizationResponse,
  ProfileEffectContentResponse,
  ProfileThemeContentResponse,
} from "@/shared/api/personalizationApi"
import type {
  AvatarFrameContent,
  NickStyleContent,
  NotificationPackContent,
  Personalization,
  PersonalizationContent,
  PersonalizationTarget,
  ProfileEffectContent,
  ProfileThemeContent,
} from "../model"

const targets: Record<Personalization["type"], PersonalizationTarget> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}

export function toProductPersonalization(
  response: PersonalizationResponse,
  owned: OwnedPersonalizationResponse | null,
  currentUserId?: string,
  currentUsername?: string,
): Personalization {
  const target = owned?.ownerType ?? targets[response.type]
  const creator = response.creatorUserId === currentUserId && currentUsername
    ? `@${currentUsername}`
    : `Creator ${response.creatorUserId.slice(0, 8)}`

  return {
    id: response.id,
    type: response.type,
    target,
    name: response.name,
    creator,
    currency: response.price?.currency ?? "COIN",
    price: response.price?.amount ?? 0,
    status: response.status,
    owned: Boolean(owned),
    active: owned?.active ?? false,
    preview: response.name,
    content: toProductContent(response),
  }
}

function toProductContent(response: PersonalizationResponse): PersonalizationContent {
  if (response.type === "NOTIFICATION_PACK") {
    const content = response.content as NotificationPackContentResponse

    return {
      kind: "NOTIFICATION_PACK",
      mixedAggregateTitle: content.mixedAggregateTitle,
      templates: content.templates,
    } satisfies NotificationPackContent
  }

  if (response.type === "NICK_STYLE") {
    const content = response.content as NickStyleContentResponse

    return {
      kind: "NICK_STYLE",
      colors: content.colors,
      angleDegrees: content.angleDegrees,
      glow: content.glow,
    } satisfies NickStyleContent
  }

  if (response.type === "PROFILE_THEME") {
    const content = response.content as ProfileThemeContentResponse

    return {
      kind: "PROFILE_THEME",
      background: content.background,
      surface: content.surface,
      accent: content.accent,
      text: content.text,
      border: content.border,
      glow: content.glow,
    } satisfies ProfileThemeContent
  }

  if (response.type === "AVATAR_FRAME") {
    const content = response.content as AvatarFrameContentResponse

    return {
      kind: "AVATAR_FRAME",
      borderColor: content.borderColor,
      borderWidthPx: content.borderWidthPx,
      radiusPx: content.radiusPx,
      glow: content.glow,
    } satisfies AvatarFrameContent
  }

  const content = response.content as ProfileEffectContentResponse

  return {
    kind: "PROFILE_EFFECT",
    effect: content.effect,
    intensity: content.intensity,
    primaryColor: content.primaryColor,
    secondaryColor: content.secondaryColor,
  } satisfies ProfileEffectContent
}
