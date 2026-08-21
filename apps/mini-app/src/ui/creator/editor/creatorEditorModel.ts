import type { TFunction } from "i18next"
import {
  createCreatorPersonalization,
  updateCreatorPersonalization,
} from "@/shared/api/creatorPersonalizationApi"
import type {
  NotificationPackContentResponse,
  PersonalizationResponse,
} from "@/shared/api/personalizationApi"
import type {
  Currency,
  Personalization,
  PersonalizationContent,
  PersonalizationStatus,
  PersonalizationType,
} from "../../model"

export type CreatorEditorForm = {
  type: PersonalizationType
  name: string
  currency: Currency
  price: string
  accent: string
  secondary: string
  intensity: number
  message: string
}

const targetByType: Record<PersonalizationType, "USER" | "ACCOUNT"> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}

export async function saveCreatorForm(
  id: string | null,
  form: CreatorEditorForm,
): Promise<PersonalizationResponse> {
  const price = {
    currency: form.type === "NOTIFICATION_PACK" ? ("PRIME" as const) : form.currency,
    amount: Number(form.price),
  }

  if (form.type === "NICK_STYLE") {
    const request = {
      name: form.name.trim(),
      colors: [
        { color: form.accent, position: 0 },
        { color: form.secondary, position: 100 },
      ],
      angleDegrees: 45,
      glow: { color: form.accent, strength: form.intensity },
      price,
    }

    return id
      ? updateCreatorPersonalization(id, "NICK_STYLE", request)
      : createCreatorPersonalization("NICK_STYLE", request)
  }

  if (form.type === "PROFILE_THEME") {
    const request = {
      name: form.name.trim(),
      background: "#11140F",
      surface: "#1C2119",
      accent: form.accent,
      text: "#F1F4E9",
      border: form.secondary,
      glow: { color: form.accent, strength: form.intensity },
      price,
    }

    return id
      ? updateCreatorPersonalization(id, "PROFILE_THEME", request)
      : createCreatorPersonalization("PROFILE_THEME", request)
  }

  if (form.type === "AVATAR_FRAME") {
    const request = {
      name: form.name.trim(),
      borderColor: form.accent,
      borderWidthPx: Math.max(1, Math.min(8, Math.round(form.intensity / 20) + 1)),
      radiusPx: 8,
      glow: { color: form.secondary, strength: form.intensity },
      price,
    }

    return id
      ? updateCreatorPersonalization(id, "AVATAR_FRAME", request)
      : createCreatorPersonalization("AVATAR_FRAME", request)
  }

  if (form.type === "PROFILE_EFFECT") {
    const request = {
      name: form.name.trim(),
      effect: "AURORA" as const,
      intensity: form.intensity,
      primaryColor: form.accent,
      secondaryColor: form.secondary,
      price,
    }

    return id
      ? updateCreatorPersonalization(id, "PROFILE_EFFECT", request)
      : createCreatorPersonalization("PROFILE_EFFECT", request)
  }

  const template = {
    emoji: null,
    soloTitle: form.name.trim(),
    soloBody: form.message.trim(),
    compact: form.message.trim(),
    aggregateTitle: form.name.trim(),
  }
  const request = {
    name: form.name.trim(),
    mixedAggregateTitle: form.name.trim(),
    templates: {
      ITEM_DROP: template,
      BOOSTER: template,
      DUNGEON: template,
      RAID: template,
      GLOBAL: template,
    },
    price,
  }

  return id
    ? updateCreatorPersonalization(id, "NOTIFICATION_PACK", request)
    : createCreatorPersonalization("NOTIFICATION_PACK", request)
}

export function createDefaultCreatorEditorForm(t: TFunction): CreatorEditorForm {
  return {
    type: "NICK_STYLE",
    name: "",
    currency: "PRIME",
    price: "25",
    accent: "#C7F24A",
    secondary: "#B98DC2",
    intensity: 48,
    message: t("creator.editor.defaultMessage"),
  }
}

export function creatorResponseToEditorForm(
  response: PersonalizationResponse,
  t: TFunction,
): CreatorEditorForm {
  const content = response.content
  const defaults = createDefaultCreatorEditorForm(t)
  const base: CreatorEditorForm = {
    ...defaults,
    type: response.type,
    name: response.name,
    currency: response.price?.currency ?? "PRIME",
    price: String(response.price?.amount ?? 25),
  }

  if (response.type === "NICK_STYLE") {
    const value = content as
      | Extract<PersonalizationContent, { kind: "NICK_STYLE" }>
      | {
          colors: { color: string; position: number }[]
          glow: { color: string; strength: number } | null
        }

    return {
      ...base,
      accent: value.colors[0]?.color ?? base.accent,
      secondary: value.colors[1]?.color ?? value.colors[0]?.color ?? base.secondary,
      intensity: value.glow?.strength ?? base.intensity,
    }
  }

  if (response.type === "PROFILE_THEME") {
    const value = content as { accent: string; border: string; glow: { strength: number } | null }

    return {
      ...base,
      accent: value.accent,
      secondary: value.border,
      intensity: value.glow?.strength ?? base.intensity,
    }
  }

  if (response.type === "AVATAR_FRAME") {
    const value = content as {
      borderColor: string
      glow: { color: string; strength: number } | null
    }

    return {
      ...base,
      accent: value.borderColor,
      secondary: value.glow?.color ?? base.secondary,
      intensity: value.glow?.strength ?? base.intensity,
    }
  }

  if (response.type === "PROFILE_EFFECT") {
    const value = content as {
      primaryColor: string
      secondaryColor: string | null
      intensity: number
    }

    return {
      ...base,
      accent: value.primaryColor,
      secondary: value.secondaryColor ?? base.secondary,
      intensity: value.intensity,
    }
  }

  const value = content as NotificationPackContentResponse

  return {
    ...base,
    currency: "PRIME",
    message: value.templates.ITEM_DROP.soloBody || value.mixedAggregateTitle || base.message,
  }
}

export function buildCreatorPreview(
  form: CreatorEditorForm,
  id: string,
  status: PersonalizationStatus,
  username: string,
  fallbackName: string,
): Personalization {
  let content: PersonalizationContent

  if (form.type === "NICK_STYLE") {
    content = {
      kind: "NICK_STYLE",
      colors: [
        { color: form.accent, position: 0 },
        { color: form.secondary, position: 100 },
      ],
      angleDegrees: 45,
      glow: { color: form.accent, strength: form.intensity },
    }
  } else if (form.type === "PROFILE_THEME") {
    content = {
      kind: "PROFILE_THEME",
      background: "#11140F",
      surface: "#1C2119",
      accent: form.accent,
      text: "#F1F4E9",
      border: form.secondary,
      glow: { color: form.accent, strength: form.intensity },
    }
  } else if (form.type === "AVATAR_FRAME") {
    content = {
      kind: "AVATAR_FRAME",
      borderColor: form.accent,
      borderWidthPx: Math.max(1, Math.min(8, Math.round(form.intensity / 20) + 1)),
      radiusPx: 8,
      glow: { color: form.secondary, strength: form.intensity },
    }
  } else if (form.type === "PROFILE_EFFECT") {
    content = {
      kind: "PROFILE_EFFECT",
      effect: "AURORA",
      intensity: form.intensity,
      primaryColor: form.accent,
      secondaryColor: form.secondary,
    }
  } else {
    const template = {
      emoji: null,
      soloTitle: form.name || "Mnemos",
      soloBody: form.message,
      compact: form.message,
      aggregateTitle: form.name || "Mnemos",
    }

    content = {
      kind: "NOTIFICATION_PACK",
      mixedAggregateTitle: form.name || "Mnemos",
      templates: {
        ITEM_DROP: template,
        BOOSTER: template,
        DUNGEON: template,
        RAID: template,
        GLOBAL: template,
      },
    }
  }

  return {
    id,
    type: form.type,
    target: targetByType[form.type],
    name: form.name.trim() || fallbackName,
    creator: `@${username}`,
    currency: form.type === "NOTIFICATION_PACK" ? "PRIME" : form.currency,
    price: Number(form.price) || 0,
    status,
    owned: false,
    active: false,
    preview: "creator",
    content,
  }
}
