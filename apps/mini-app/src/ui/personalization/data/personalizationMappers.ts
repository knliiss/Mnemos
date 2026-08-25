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
} from "../../model"

const targets: Record<Personalization["type"], PersonalizationTarget> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}

export function toPersonalization(
  response: PersonalizationResponse,
  owned: OwnedPersonalizationResponse | null,
  currentUserId?: string,
  currentUsername?: string,
): Personalization {
  const target = owned?.ownerType ?? targets[response.type]
  const createdByCurrentUser = Boolean(
    response.creatorUserId && response.creatorUserId === currentUserId,
  )
  const creator = resolveCreator(response, createdByCurrentUser, currentUsername)

  return {
    id: response.id,
    type: response.type,
    target,
    name: response.name,
    creator,
    createdByCurrentUser,
    currency: response.price?.currency ?? "COIN",
    price: response.price?.amount ?? 0,
    status: response.status,
    owned: Boolean(owned),
    active: owned?.active ?? false,
    preview: response.name,
    content: toPersonalizationContent(response),
  }
}

function resolveCreator(
  response: PersonalizationResponse,
  createdByCurrentUser: boolean,
  currentUsername?: string,
): string {
  if (response.origin === "SYSTEM") {
    return "Mnemos"
  }

  if (createdByCurrentUser && currentUsername) {
    return `@${currentUsername}`
  }

  return response.creatorUserId ? `Creator ${response.creatorUserId.slice(0, 8)}` : "Mnemos"
}

function toPersonalizationContent(response: PersonalizationResponse): PersonalizationContent {
  if (response.type === "NOTIFICATION_PACK") {
    const content = response.content as NotificationPackContentResponse
    const templates = Object.fromEntries(
      Object.entries(content.templates).map(([key, template]) => [
        key,
        {
          ...template,
          variants: template.variants ?? [],
        },
      ]),
    ) as NotificationPackContent["templates"]

    return {
      kind: "NOTIFICATION_PACK",
      mixedAggregateTitle: content.mixedAggregateTitle,
      templates,
      valueMappings: content.valueMappings ?? {},
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
      backgroundSecondary: content.backgroundSecondary,
      surface: content.surface,
      surfaceSecondary: content.surfaceSecondary,
      accent: content.accent,
      text: content.text,
      border: content.border,
      gradientAngleDegrees: content.gradientAngleDegrees,
      textureStrength: content.textureStrength,
      glow: content.glow,
      layers: content.layers ?? [],
    } satisfies ProfileThemeContent
  }

  if (response.type === "AVATAR_FRAME") {
    const content = response.content as AvatarFrameContentResponse

    return {
      kind: "AVATAR_FRAME",
      borderColor: content.borderColor,
      secondaryBorderColor: content.secondaryBorderColor,
      borderWidthPx: content.borderWidthPx,
      gapPx: content.gapPx,
      radiusPx: content.radiusPx,
      style: content.style,
      glow: content.glow,
      layers: content.layers ?? [],
    } satisfies AvatarFrameContent
  }

  const content = response.content as ProfileEffectContentResponse

  return {
    kind: "PROFILE_EFFECT",
    effect: content.effect,
    intensity: content.intensity,
    speed: content.speed,
    density: content.density,
    scale: content.scale,
    primaryColor: content.primaryColor,
    secondaryColor: content.secondaryColor,
    layers: content.layers ?? [],
  } satisfies ProfileEffectContent
}
