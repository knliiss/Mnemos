import type { PersonalizationType } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"

type PersonalizationTypeGlyphProps = {
  type: PersonalizationType
}

export function PersonalizationTypeGlyph({ type }: PersonalizationTypeGlyphProps) {
  const glyph =
    type === "NOTIFICATION_PACK"
      ? "bell"
      : type === "NICK_STYLE"
        ? "spark"
        : type === "PROFILE_THEME"
          ? "creator"
          : type === "AVATAR_FRAME"
            ? "profile-mark"
            : "events"

  return <Glyph name={glyph} size={20} />
}
