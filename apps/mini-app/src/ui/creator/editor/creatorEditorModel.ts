import {
  createCreatorPersonalization,
  updateCreatorPersonalization,
} from "@/shared/api/creatorPersonalizationApi"
import type {
  NotificationPackEditorDescriptorResponse,
  VisualLayerRequest,
} from "@/shared/api/creatorPersonalizationApi"
import type {
  AvatarFrameContentResponse,
  NickStyleContentResponse,
  NotificationPackContentResponse,
  NotificationTemplateResponse,
  PersonalizationResponse,
  ProfileEffectContentResponse,
  ProfileThemeContentResponse,
} from "@/shared/api/personalizationApi"
import type {
  Currency,
  EffectType,
  FrameStyle,
  GradientStop,
  NotificationTemplateKey,
  Personalization,
  PersonalizationContent,
  PersonalizationStatus,
  PersonalizationType,
  VisualLayer,
} from "../../model"

export type CreatorGlowForm = {
  enabled: boolean
  color: string
  strength: number
}

export type CreatorVisualLayerForm = VisualLayer

export type NickStyleEditorForm = {
  colors: GradientStop[]
  angleDegrees: number
  glow: CreatorGlowForm
}

export type ProfileThemeEditorForm = {
  background: string
  backgroundSecondary: string
  surface: string
  surfaceSecondary: string
  accent: string
  text: string
  border: string
  gradientAngleDegrees: number
  textureStrength: number
  glow: CreatorGlowForm
  layers: CreatorVisualLayerForm[]
}

export type AvatarFrameEditorForm = {
  borderColor: string
  secondaryBorderColor: string
  borderWidthPx: number
  gapPx: number
  radiusPx: number
  style: FrameStyle
  glow: CreatorGlowForm
  layers: CreatorVisualLayerForm[]
}

export type ProfileEffectEditorForm = {
  effect: EffectType
  intensity: number
  speed: number
  density: number
  scale: number
  primaryColor: string
  secondaryColor: string
  layers: CreatorVisualLayerForm[]
}

export type NotificationPackEditorForm = {
  mixedAggregateTitle: string
  templates: Record<string, NotificationTemplateResponse>
}

export type CreatorEditorForm = {
  type: PersonalizationType
  name: string
  currency: Currency
  price: string
  nickStyle: NickStyleEditorForm
  profileTheme: ProfileThemeEditorForm
  avatarFrame: AvatarFrameEditorForm
  profileEffect: ProfileEffectEditorForm
  notificationPack: NotificationPackEditorForm
}

const targetByType: Record<PersonalizationType, "USER" | "ACCOUNT"> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}

function glowRequest(glow: CreatorGlowForm) {
  if (!glow.enabled) {
    return null
  }

  return {
    color: glow.color,
    strength: glow.strength,
  }
}

function glowForm(
  glow: { color: string; strength: number } | null,
  fallbackColor: string,
  fallbackStrength: number,
): CreatorGlowForm {
  return {
    enabled: Boolean(glow),
    color: glow?.color ?? fallbackColor,
    strength: glow?.strength ?? fallbackStrength,
  }
}

function layerRequest(layer: CreatorVisualLayerForm): VisualLayerRequest {
  return {
    id: layer.id,
    assetId: layer.assetId,
    xPercent: layer.xPercent,
    yPercent: layer.yPercent,
    widthPercent: layer.widthPercent,
    opacity: layer.opacity,
    rotationDegrees: layer.rotationDegrees,
    blendMode: layer.blendMode,
    animation: layer.animation,
    zIndex: layer.zIndex,
  }
}

function notificationPackDefaults(
  descriptor?: NotificationPackEditorDescriptorResponse,
): NotificationPackEditorForm {
  if (!descriptor) {
    return {
      mixedAggregateTitle: "",
      templates: {},
    }
  }

  return {
    mixedAggregateTitle: descriptor.mixedAggregateTitleDefault,
    templates: Object.fromEntries(
      descriptor.templates.map((template) => [template.type, { ...template.defaults }]),
    ),
  }
}

function completeNotificationTemplates(
  form: CreatorEditorForm,
  descriptor: NotificationPackEditorDescriptorResponse,
): Record<NotificationTemplateKey, NotificationTemplateResponse> {
  return Object.fromEntries(
    descriptor.templates.map((templateDescriptor) => {
      const template = form.notificationPack.templates[templateDescriptor.type]

      if (!template) {
        throw new Error(`Notification template ${templateDescriptor.type} is missing`)
      }

      return [templateDescriptor.type, template]
    }),
  ) as Record<NotificationTemplateKey, NotificationTemplateResponse>
}

export async function saveCreatorForm(
  id: string | null,
  form: CreatorEditorForm,
  descriptor: NotificationPackEditorDescriptorResponse,
): Promise<PersonalizationResponse> {
  const price = {
    currency: form.type === "NOTIFICATION_PACK" ? ("PRIME" as const) : form.currency,
    amount: Number(form.price),
  }
  const name = form.name.trim()

  if (form.type === "NICK_STYLE") {
    const request = {
      name,
      colors: form.nickStyle.colors,
      angleDegrees: form.nickStyle.angleDegrees,
      glow: glowRequest(form.nickStyle.glow),
      price,
    }

    return id
      ? updateCreatorPersonalization(id, "NICK_STYLE", request)
      : createCreatorPersonalization("NICK_STYLE", request)
  }

  if (form.type === "PROFILE_THEME") {
    const theme = form.profileTheme
    const request = {
      name,
      background: theme.background,
      backgroundSecondary: theme.backgroundSecondary,
      surface: theme.surface,
      surfaceSecondary: theme.surfaceSecondary,
      accent: theme.accent,
      text: theme.text,
      border: theme.border,
      gradientAngleDegrees: theme.gradientAngleDegrees,
      textureStrength: theme.textureStrength,
      glow: glowRequest(theme.glow),
      layers: theme.layers.map(layerRequest),
      price,
    }

    return id
      ? updateCreatorPersonalization(id, "PROFILE_THEME", request)
      : createCreatorPersonalization("PROFILE_THEME", request)
  }

  if (form.type === "AVATAR_FRAME") {
    const frame = form.avatarFrame
    const request = {
      name,
      borderColor: frame.borderColor,
      secondaryBorderColor: frame.secondaryBorderColor,
      borderWidthPx: frame.borderWidthPx,
      gapPx: frame.gapPx,
      radiusPx: frame.radiusPx,
      style: frame.style,
      glow: glowRequest(frame.glow),
      layers: frame.layers.map(layerRequest),
      price,
    }

    return id
      ? updateCreatorPersonalization(id, "AVATAR_FRAME", request)
      : createCreatorPersonalization("AVATAR_FRAME", request)
  }

  if (form.type === "PROFILE_EFFECT") {
    const effect = form.profileEffect
    const request = {
      name,
      effect: effect.effect,
      intensity: effect.intensity,
      speed: effect.speed,
      density: effect.density,
      scale: effect.scale,
      primaryColor: effect.primaryColor,
      secondaryColor: effect.secondaryColor,
      layers: effect.layers.map(layerRequest),
      price,
    }

    return id
      ? updateCreatorPersonalization(id, "PROFILE_EFFECT", request)
      : createCreatorPersonalization("PROFILE_EFFECT", request)
  }

  const request = {
    name,
    mixedAggregateTitle: form.notificationPack.mixedAggregateTitle.trim(),
    templates: completeNotificationTemplates(form, descriptor),
    price,
  }

  return id
    ? updateCreatorPersonalization(id, "NOTIFICATION_PACK", request)
    : createCreatorPersonalization("NOTIFICATION_PACK", request)
}

export function createDefaultCreatorEditorForm(
  descriptor?: NotificationPackEditorDescriptorResponse,
): CreatorEditorForm {
  return {
    type: "NICK_STYLE",
    name: "",
    currency: "PRIME",
    price: "25",
    nickStyle: {
      colors: [
        { color: "#CBFF2D", position: 0 },
        { color: "#B98DC2", position: 52 },
        { color: "#5BD1C2", position: 100 },
      ],
      angleDegrees: 42,
      glow: {
        enabled: true,
        color: "#CBFF2D",
        strength: 48,
      },
    },
    profileTheme: {
      background: "#080B08",
      backgroundSecondary: "#151B0E",
      surface: "#151812",
      surfaceSecondary: "#20251A",
      accent: "#CBFF2D",
      text: "#F3F5EC",
      border: "#343B2E",
      gradientAngleDegrees: 145,
      textureStrength: 24,
      glow: {
        enabled: true,
        color: "#CBFF2D",
        strength: 28,
      },
      layers: [],
    },
    avatarFrame: {
      borderColor: "#CBFF2D",
      secondaryBorderColor: "#B98DC2",
      borderWidthPx: 3,
      gapPx: 2,
      radiusPx: 14,
      style: "DOUBLE",
      glow: {
        enabled: true,
        color: "#CBFF2D",
        strength: 42,
      },
      layers: [],
    },
    profileEffect: {
      effect: "AURORA",
      intensity: 58,
      speed: 42,
      density: 48,
      scale: 60,
      primaryColor: "#CBFF2D",
      secondaryColor: "#B98DC2",
      layers: [],
    },
    notificationPack: notificationPackDefaults(descriptor),
  }
}

export function creatorResponseToEditorForm(
  response: PersonalizationResponse,
  descriptor: NotificationPackEditorDescriptorResponse,
): CreatorEditorForm {
  const defaults = createDefaultCreatorEditorForm(descriptor)
  const base: CreatorEditorForm = {
    ...defaults,
    type: response.type,
    name: response.name,
    currency: response.price?.currency ?? "PRIME",
    price: String(response.price?.amount ?? 25),
  }

  if (response.type === "NICK_STYLE") {
    const value = response.content as NickStyleContentResponse

    return {
      ...base,
      nickStyle: {
        colors: value.colors,
        angleDegrees: value.angleDegrees,
        glow: glowForm(value.glow, defaults.nickStyle.glow.color, defaults.nickStyle.glow.strength),
      },
    }
  }

  if (response.type === "PROFILE_THEME") {
    const value = response.content as ProfileThemeContentResponse

    return {
      ...base,
      profileTheme: {
        background: value.background,
        backgroundSecondary: value.backgroundSecondary ?? value.background,
        surface: value.surface,
        surfaceSecondary: value.surfaceSecondary ?? value.surface,
        accent: value.accent,
        text: value.text,
        border: value.border,
        gradientAngleDegrees:
          value.gradientAngleDegrees ?? defaults.profileTheme.gradientAngleDegrees,
        textureStrength: value.textureStrength ?? defaults.profileTheme.textureStrength,
        glow: glowForm(
          value.glow,
          defaults.profileTheme.glow.color,
          defaults.profileTheme.glow.strength,
        ),
        layers: value.layers ?? [],
      },
    }
  }

  if (response.type === "AVATAR_FRAME") {
    const value = response.content as AvatarFrameContentResponse

    return {
      ...base,
      avatarFrame: {
        borderColor: value.borderColor,
        secondaryBorderColor: value.secondaryBorderColor ?? value.borderColor,
        borderWidthPx: value.borderWidthPx,
        gapPx: value.gapPx ?? 0,
        radiusPx: value.radiusPx,
        style: value.style ?? "SOLID",
        glow: glowForm(
          value.glow,
          defaults.avatarFrame.glow.color,
          defaults.avatarFrame.glow.strength,
        ),
        layers: value.layers ?? [],
      },
    }
  }

  if (response.type === "PROFILE_EFFECT") {
    const value = response.content as ProfileEffectContentResponse

    return {
      ...base,
      profileEffect: {
        effect: value.effect,
        intensity: value.intensity,
        speed: value.speed ?? defaults.profileEffect.speed,
        density: value.density ?? defaults.profileEffect.density,
        scale: value.scale ?? defaults.profileEffect.scale,
        primaryColor: value.primaryColor,
        secondaryColor: value.secondaryColor ?? defaults.profileEffect.secondaryColor,
        layers: value.layers ?? [],
      },
    }
  }

  const value = response.content as NotificationPackContentResponse

  return {
    ...base,
    currency: "PRIME",
    notificationPack: {
      mixedAggregateTitle: value.mixedAggregateTitle,
      templates: { ...value.templates },
    },
  }
}

export function buildCreatorPreview(
  form: CreatorEditorForm,
  descriptor: NotificationPackEditorDescriptorResponse,
  id: string,
  status: PersonalizationStatus,
  username: string,
  fallbackName: string,
): Personalization {
  let content: PersonalizationContent

  if (form.type === "NICK_STYLE") {
    content = {
      kind: "NICK_STYLE",
      colors: form.nickStyle.colors,
      angleDegrees: form.nickStyle.angleDegrees,
      glow: glowRequest(form.nickStyle.glow),
    }
  } else if (form.type === "PROFILE_THEME") {
    content = {
      kind: "PROFILE_THEME",
      background: form.profileTheme.background,
      backgroundSecondary: form.profileTheme.backgroundSecondary,
      surface: form.profileTheme.surface,
      surfaceSecondary: form.profileTheme.surfaceSecondary,
      accent: form.profileTheme.accent,
      text: form.profileTheme.text,
      border: form.profileTheme.border,
      gradientAngleDegrees: form.profileTheme.gradientAngleDegrees,
      textureStrength: form.profileTheme.textureStrength,
      glow: glowRequest(form.profileTheme.glow),
      layers: form.profileTheme.layers,
    }
  } else if (form.type === "AVATAR_FRAME") {
    content = {
      kind: "AVATAR_FRAME",
      borderColor: form.avatarFrame.borderColor,
      secondaryBorderColor: form.avatarFrame.secondaryBorderColor,
      borderWidthPx: form.avatarFrame.borderWidthPx,
      gapPx: form.avatarFrame.gapPx,
      radiusPx: form.avatarFrame.radiusPx,
      style: form.avatarFrame.style,
      glow: glowRequest(form.avatarFrame.glow),
      layers: form.avatarFrame.layers,
    }
  } else if (form.type === "PROFILE_EFFECT") {
    content = {
      kind: "PROFILE_EFFECT",
      effect: form.profileEffect.effect,
      intensity: form.profileEffect.intensity,
      speed: form.profileEffect.speed,
      density: form.profileEffect.density,
      scale: form.profileEffect.scale,
      primaryColor: form.profileEffect.primaryColor,
      secondaryColor: form.profileEffect.secondaryColor,
      layers: form.profileEffect.layers,
    }
  } else {
    content = {
      kind: "NOTIFICATION_PACK",
      mixedAggregateTitle: form.notificationPack.mixedAggregateTitle,
      templates: completeNotificationTemplates(form, descriptor),
    }
  }

  return {
    id,
    type: form.type,
    target: targetByType[form.type],
    name: form.name.trim() || fallbackName,
    creator: `@${username}`,
    createdByCurrentUser: true,
    currency: form.type === "NOTIFICATION_PACK" ? "PRIME" : form.currency,
    price: Number(form.price) || 0,
    status,
    owned: false,
    active: false,
    preview: "creator",
    content,
  }
}
