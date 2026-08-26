import type { CSSProperties } from "react"
import type { Personalization } from "../../model"
import "./personalization-category-swatch.css"

type SwatchStyle = CSSProperties & {
  "--swatch-primary"?: string
  "--swatch-secondary"?: string
  "--swatch-accent"?: string
}

export function PersonalizationCategorySwatch({ item }: { item: Personalization }) {
  const content = item.content

  if (!content) {
    return (
      <span className="personalization-category-swatch personalization-category-swatch--empty" />
    )
  }

  if (content.kind === "NOTIFICATION_PACK") {
    return (
      <span className="personalization-category-swatch personalization-category-swatch--notification">
        <i />
        <b />
      </span>
    )
  }

  if (content.kind === "NICK_STYLE") {
    const gradient = content.colors.map((stop) => `${stop.color} ${stop.position}%`).join(", ")

    return (
      <span
        className="personalization-category-swatch personalization-category-swatch--nick"
        style={{ backgroundImage: `linear-gradient(${content.angleDegrees}deg, ${gradient})` }}
      />
    )
  }

  if (content.kind === "PROFILE_THEME") {
    const style: SwatchStyle = {
      "--swatch-primary": content.background,
      "--swatch-secondary": content.surface,
      "--swatch-accent": content.accent,
    }

    return (
      <span
        className="personalization-category-swatch personalization-category-swatch--theme"
        style={style}
      >
        <i />
        <b />
      </span>
    )
  }

  if (content.kind === "AVATAR_FRAME") {
    const style: SwatchStyle = {
      "--swatch-primary": content.borderColor,
      "--swatch-secondary": content.secondaryBorderColor,
    }

    return (
      <span
        className={`personalization-category-swatch personalization-category-swatch--frame personalization-category-swatch--frame-${content.style.toLowerCase()}`}
        style={style}
      >
        <i />
      </span>
    )
  }

  const style: SwatchStyle = {
    "--swatch-primary": content.primaryColor,
    "--swatch-secondary": content.secondaryColor ?? content.primaryColor,
  }
  const scope = content.kind === "PROFILE_BACKGROUND" ? "background" : "effect"

  return (
    <span
      className={`personalization-category-swatch personalization-category-swatch--${scope} personalization-category-swatch--${content.effect.toLowerCase()}`}
      style={style}
    >
      <i />
      <b />
    </span>
  )
}
