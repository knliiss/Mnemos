import type { CreatorEditorForm, CreatorGlowForm } from "./creatorEditorModel"

function inRange(value: number, min: number, max: number): boolean {
  return Number.isInteger(value) && value >= min && value <= max
}

function glowValid(glow: CreatorGlowForm): boolean {
  return !glow.enabled || inRange(glow.strength, 1, 100)
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
