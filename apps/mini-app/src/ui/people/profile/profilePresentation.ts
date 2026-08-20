import type { CSSProperties } from "react"
import type { TFunction } from "i18next"
import type { PublicProfileResponse } from "@/shared/api/profileSocialApi"

const SKIN_BASE_URL = "https://webdata.c7x.dev/textures/skin"

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
