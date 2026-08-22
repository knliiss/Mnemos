import type {
  NotificationPackEditorDescriptorResponse,
  NotificationPackEditorFieldKey,
} from "@/shared/api/creatorPersonalizationApi"
import type { NotificationTemplateResponse } from "@/shared/api/personalizationApi"
import type { CreatorEditorForm, CreatorGlowForm } from "./creatorEditorModel"

const PLACEHOLDER = /\{([a-zA-Z][a-zA-Z0-9_]*)}/g

function inRange(value: number, min: number, max: number): boolean {
  return Number.isInteger(value) && value >= min && value <= max
}

function glowValid(glow: CreatorGlowForm): boolean {
  return !glow.enabled || inRange(glow.strength, 1, 100)
}

function placeholders(value: string): string[] {
  return [...value.matchAll(PLACEHOLDER)].map((match) => match[1])
}

function textStructureValid(value: string, maxLength: number): boolean {
  if (!value.trim() || value.length > maxLength || value.includes("<") || value.includes(">")) {
    return false
  }

  const valueWithoutPlaceholders = value.replace(PLACEHOLDER, "")

  return !valueWithoutPlaceholders.includes("{") && !valueWithoutPlaceholders.includes("}")
}

function staticTextValid(value: string, maxLength: number): boolean {
  return textStructureValid(value, maxLength) && placeholders(value).length === 0
}

function fieldValue(
  template: NotificationTemplateResponse,
  field: NotificationPackEditorFieldKey,
): string {
  if (field === "SOLO_TITLE") {
    return template.soloTitle
  }

  if (field === "SOLO_BODY") {
    return template.soloBody
  }

  if (field === "COMPACT") {
    return template.compact
  }

  return template.aggregateTitle
}

export function isCreatorVisualFormValid(form: CreatorEditorForm): boolean {
  if (form.type === "NICK_STYLE") {
    const positions = form.nickStyle.colors.map((stop) => stop.position)

    return (
      form.nickStyle.colors.length >= 1 &&
      form.nickStyle.colors.length <= 4 &&
      positions.every((position) => inRange(position, 0, 100)) &&
      new Set(positions).size === positions.length &&
      inRange(form.nickStyle.angleDegrees, 0, 360) &&
      glowValid(form.nickStyle.glow)
    )
  }

  if (form.type === "PROFILE_THEME") {
    return (
      inRange(form.profileTheme.gradientAngleDegrees, 0, 360) &&
      inRange(form.profileTheme.textureStrength, 0, 100) &&
      glowValid(form.profileTheme.glow)
    )
  }

  if (form.type === "AVATAR_FRAME") {
    return (
      inRange(form.avatarFrame.borderWidthPx, 1, 8) &&
      inRange(form.avatarFrame.gapPx, 0, 8) &&
      inRange(form.avatarFrame.radiusPx, 0, 32) &&
      glowValid(form.avatarFrame.glow)
    )
  }

  if (form.type === "PROFILE_EFFECT") {
    return (
      inRange(form.profileEffect.intensity, 1, 100) &&
      inRange(form.profileEffect.speed, 1, 100) &&
      inRange(form.profileEffect.density, 1, 100) &&
      inRange(form.profileEffect.scale, 1, 100)
    )
  }

  return true
}

export function isNotificationPackFormValid(
  form: CreatorEditorForm,
  descriptor: NotificationPackEditorDescriptorResponse | null,
): boolean {
  if (form.type !== "NOTIFICATION_PACK") {
    return true
  }

  if (!descriptor) {
    return false
  }

  if (!staticTextValid(form.notificationPack.mixedAggregateTitle, descriptor.maxTextLength)) {
    return false
  }

  return descriptor.templates.every((templateDescriptor) => {
    const template = form.notificationPack.templates[templateDescriptor.type]

    if (!template) {
      return false
    }

    if (
      template.emoji !== null &&
      (!staticTextValid(template.emoji, descriptor.maxEmojiLength) ||
        template.emoji.length > descriptor.maxEmojiLength)
    ) {
      return false
    }

    const allowedVariables = new Set(templateDescriptor.variables.map((variable) => variable.key))

    return descriptor.fields.every((field) => {
      const value = fieldValue(template, field.key)

      if (!textStructureValid(value, descriptor.maxTextLength)) {
        return false
      }

      const valuePlaceholders = placeholders(value)

      if (!field.variablesAllowed) {
        return valuePlaceholders.length === 0
      }

      return valuePlaceholders.every((placeholder) => allowedVariables.has(placeholder))
    })
  })
}
