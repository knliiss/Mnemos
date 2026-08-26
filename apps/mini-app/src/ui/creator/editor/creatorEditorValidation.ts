import type {
  NotificationPackEditorDescriptorResponse,
  NotificationPackEditorFieldKey,
} from "@/shared/api/creatorPersonalizationApi"
import type { NotificationTemplate, NotificationTemplateVariant } from "../../model"
import type {
  CreatorEditorForm,
  CreatorGlowForm,
  ProfileVisualEffectEditorForm,
} from "./creatorEditorModel"

const PLACEHOLDER = /\{([a-zA-Z][a-zA-Z0-9_]*)(?:\.([a-zA-Z][a-zA-Z0-9_]*))?}/g

type TemplateDescriptor = NotificationPackEditorDescriptorResponse["templates"][number]
type PlaceholderReference = {
  key: string
  form: string | null
}

function inRange(value: number, min: number, max: number): boolean {
  return Number.isInteger(value) && value >= min && value <= max
}

function glowValid(glow: CreatorGlowForm): boolean {
  return !glow.enabled || inRange(glow.strength, 1, 100)
}

function profileVisualEffectValid(effect: ProfileVisualEffectEditorForm): boolean {
  return (
    inRange(effect.intensity, 1, 100) &&
    inRange(effect.speed, 1, 100) &&
    inRange(effect.density, 1, 100) &&
    inRange(effect.scale, 1, 100)
  )
}

function placeholderReferences(value: string): PlaceholderReference[] {
  return [...value.matchAll(PLACEHOLDER)].map((match) => ({
    key: match[1],
    form: match[2] ?? null,
  }))
}

function textStructureValid(value: string, maxLength: number): boolean {
  if (!value.trim() || value.length > maxLength || value.includes("<") || value.includes(">")) {
    return false
  }

  const valueWithoutPlaceholders = value.replace(PLACEHOLDER, "")

  return !valueWithoutPlaceholders.includes("{") && !valueWithoutPlaceholders.includes("}")
}

function staticTextValid(value: string, maxLength: number): boolean {
  return textStructureValid(value, maxLength) && placeholderReferences(value).length === 0
}

function fieldValue(
  template: Pick<NotificationTemplate, "soloTitle" | "soloBody" | "compact" | "aggregateTitle">,
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

function referenceValid(reference: PlaceholderReference, descriptor: TemplateDescriptor): boolean {
  const variable = descriptor.variables.find((candidate) => candidate.key === reference.key)

  if (!variable) {
    return false
  }

  if (!reference.form) {
    return true
  }

  return (variable.forms ?? []).some((form) => form.key === reference.form)
}

function messageFieldsValid(
  template: Pick<
    NotificationTemplate,
    "emoji" | "soloTitle" | "soloBody" | "compact" | "aggregateTitle"
  >,
  descriptor: TemplateDescriptor,
  editorDescriptor: NotificationPackEditorDescriptorResponse,
): boolean {
  if (
    template.emoji !== null &&
    (!staticTextValid(template.emoji, editorDescriptor.maxEmojiLength) ||
      template.emoji.length > editorDescriptor.maxEmojiLength)
  ) {
    return false
  }

  return editorDescriptor.fields.every((field) => {
    const value = fieldValue(template, field.key)

    if (!textStructureValid(value, editorDescriptor.maxTextLength)) {
      return false
    }

    const references = placeholderReferences(value)

    if (!field.variablesAllowed) {
      return references.length === 0
    }

    return references.every((reference) => referenceValid(reference, descriptor))
  })
}

function variantConditionsValid(
  variant: NotificationTemplateVariant,
  descriptor: TemplateDescriptor,
): boolean {
  const conditionFields = descriptor.conditionFields ?? []

  if (!variant.conditions.length || !conditionFields.length) {
    return false
  }

  const uniqueFields = new Set(variant.conditions.map((condition) => condition.field))

  if (uniqueFields.size !== variant.conditions.length) {
    return false
  }

  return variant.conditions.every((condition) => {
    const field = conditionFields.find((candidate) => candidate.field === condition.field)

    return Boolean(field?.values.some((value) => value.value === condition.value))
  })
}

function variantsValid(
  template: NotificationTemplate,
  descriptor: TemplateDescriptor,
  editorDescriptor: NotificationPackEditorDescriptorResponse,
): boolean {
  const ids = new Set(template.variants.map((variant) => variant.id))

  if (ids.size !== template.variants.length) {
    return false
  }

  return template.variants.every(
    (variant) =>
      variantConditionsValid(variant, descriptor) &&
      messageFieldsValid(variant, descriptor, editorDescriptor),
  )
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

  if (form.type === "PROFILE_BACKGROUND") {
    return profileVisualEffectValid(form.profileBackground)
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
    return profileVisualEffectValid(form.profileEffect)
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

    return Boolean(
      template &&
        messageFieldsValid(template, templateDescriptor, descriptor) &&
        variantsValid(template, templateDescriptor, descriptor),
    )
  })
}
