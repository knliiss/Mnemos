import type { CSSProperties } from "react"
import type { GuideDungeonType } from "./guideMilestones"

const DUNGEON_INDEX: Record<GuideDungeonType, number> = {
  EASY: 0,
  NORMAL: 1,
  HARD: 2,
  INSANE: 3,
}

export function guideDungeonArtworkStyle(
  type: GuideDungeonType,
  displayCellSize = 72,
): CSSProperties {
  const index = DUNGEON_INDEX[type]

  return {
    backgroundImage: "url(/guides/entities/dungeon-guide-strip.webp)",
    backgroundPosition: `${-index * displayCellSize}px 0px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${4 * displayCellSize}px ${displayCellSize}px`,
    imageRendering: "pixelated",
    width: `${displayCellSize}px`,
    height: `${displayCellSize}px`,
  }
}
