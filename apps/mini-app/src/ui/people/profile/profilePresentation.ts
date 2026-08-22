import type { CSSProperties } from "react"
import type { TFunction } from "i18next"
import type { PublicProfileResponse } from "@/shared/api/profileSocialApi"

const SKIN_BASE_URL = "https://webdata.c7x.dev/textures/skin"

type ProfileCardStyle = CSSProperties & {
  "--profile-theme-background"?: string
  "--profile-theme-background-secondary"?: string
  "--profile-theme-surface"?: string
  "--profile-theme-surface-secondary"?: string
  "--profile-theme-accent"?: string
  "--profile-theme-text"?: string
  "--profile-theme-border"?: string
  "--profile-theme-angle"?: string
  "--profile-theme-texture"?: string
  "--profile-theme-glow"?: string
  "--profile-effect-primary"?: string
  "--profile-effect-secondary"?: string
  "--profile-effect-opacity"?: string
  "--profile-effect-duration"?: string
  "--profile-effect-density"?: string
  "--profile-effect-scale"?: string
}

type ProfileHeadStyle = CSSProperties & {
  "--profile-frame-color"?: string
  "--profile-frame-secondary"?: string
  "--profile-frame-width"?: string
  "--profile-frame-gap"?: string
  "--profile-frame-radius"?: string
  "--profile-frame-glow"?: string
}

export function nicknameStyle(profile: PublicProfileResponse): CSSProperties | undefined {
  const style = profile.appearance.nickStyle

  if (!style || !style.colors.length) {
    return undefined
  }

  const stops = style.colors.map((stop) => `${stop.color} ${stop.position}%`).join(", ")

  return {
    backgroundImage: `linear-gradient(${style.angleDegrees}deg, ${stops})`,
    backgroundClip: "text",
    WebkitBackgroundClip: "text",
    color: "transparent",
    textShadow: style.glow
      ? `0 0 ${Math.max(2, Math.round(style.glow.strength / 10))}px ${style.glow.color}`
      : undefined,
  }
}

export function profileCardStyle(profile: PublicProfileResponse): ProfileCardStyle | undefined {
  const theme = profile.appearance.profileTheme
  const effect = profile.appearance.profileEffect

  if (!theme && !effect) {
    return undefined
  }

  const style: ProfileCardStyle = {}

  if (theme) {
    style["--profile-theme-background"] = theme.background
    style["--profile-theme-background-secondary"] = theme.backgroundSecondary
    style["--profile-theme-surface"] = theme.surface
    style["--profile-theme-surface-secondary"] = theme.surfaceSecondary
    style["--profile-theme-accent"] = theme.accent
    style["--profile-theme-text"] = theme.text
    style["--profile-theme-border"] = theme.border
    style["--profile-theme-angle"] = `${theme.gradientAngleDegrees}deg`
    style["--profile-theme-texture"] = `${Math.round(theme.textureStrength * 0.16)}%`
    style["--profile-theme-glow"] = glowShadow(theme.glow)
  }

  if (effect) {
    const opacity = 0.08 + (effect.intensity / 100) * 0.2
    const durationSeconds = 8.2 - (effect.speed / 100) * 6.4
    const scale = 0.8 + (effect.scale / 100) * 0.5

    style["--profile-effect-primary"] = effect.primaryColor
    style["--profile-effect-secondary"] = effect.secondaryColor ?? effect.primaryColor
    style["--profile-effect-opacity"] = opacity.toFixed(3)
    style["--profile-effect-duration"] = `${durationSeconds.toFixed(2)}s`
    style["--profile-effect-density"] = `${effect.density}%`
    style["--profile-effect-scale"] = scale.toFixed(3)
  }

  return style
}

export function profileCardClassName(profile: PublicProfileResponse): string {
  const classes = ["public-profile-card"]
  const appearance = profile.appearance

  if (appearance.profileTheme) {
    classes.push("has-profile-theme")
  }

  if (appearance.profileEffect) {
    classes.push(
      "has-profile-effect",
      `profile-effect--${appearance.profileEffect.effect.toLowerCase()}`,
    )
  }

  return classes.join(" ")
}

export function profileHeadClassName(profile: PublicProfileResponse): string {
  const frame = profile.appearance.avatarFrame
  const classes = ["public-profile-head"]

  if (!frame) {
    return classes.join(" ")
  }

  classes.push("has-avatar-frame", `profile-frame--${frame.style.toLowerCase()}`)

  return classes.join(" ")
}

export function profileHeadStyle(profile: PublicProfileResponse): ProfileHeadStyle | undefined {
  const frame = profile.appearance.avatarFrame

  if (!frame) {
    return undefined
  }

  return {
    "--profile-frame-color": frame.borderColor,
    "--profile-frame-secondary": frame.secondaryBorderColor,
    "--profile-frame-width": `${frame.borderWidthPx}px`,
    "--profile-frame-gap": `${frame.gapPx}px`,
    "--profile-frame-radius": `${frame.radiusPx}px`,
    "--profile-frame-glow": glowShadow(frame.glow),
  }
}

export function profileSkinUrl(cristalixUuid: string) {
  return `${SKIN_BASE_URL}/${cristalixUuid}`
}

export function shortAccountId(accountId: string) {
  return `#${accountId.slice(0, 8)}`
}

export function rarityLabel(value: string | undefined, t: TFunction) {
  if (!value) {
    return t("people.profile.drop")
  }

  return t(`people.profile.rarities.${value}`, { defaultValue: value })
}

export function localeForLanguage(language?: string) {
  if (language === "en") {
    return "en-US"
  }

  if (language === "ru") {
    return "ru-RU"
  }

  return "uk-UA"
}

function glowShadow(glow: { color: string; strength: number } | null): string {
  if (!glow) {
    return "none"
  }

  const blur = Math.max(6, Math.round(glow.strength * 0.32))
  const alpha = Math.min(0.6, 0.14 + glow.strength / 220)

  return `0 0 ${blur}px color-mix(in srgb, ${glow.color} ${Math.round(alpha * 100)}%, transparent)`
}
