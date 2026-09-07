import type { CSSProperties } from "react"

const SOURCE_ITEM_ATLAS_URL = "/guides/entities/source-item-atlas.webp"
const ATLAS_COLUMNS = 4
const ATLAS_ROWS = 2

const SOURCE_ITEM_INDEX: Record<string, number> = {
  corrupt_shard: 0,
  star_dust: 1,
  vibrant_shard: 2,
  arcane_shard: 3,
  fragment_aura_chest_key: 4,
  rare_shard: 5,
  aura_chest_key: 6,
  book_improvement: 7,
}

export function guideSourceItemArtworkStyle(
  localizationKey: string,
  displayCellSize: number,
): CSSProperties | undefined {
  const itemId = sourceItemId(localizationKey)

  if (!itemId) {
    return undefined
  }

  const index = SOURCE_ITEM_INDEX[itemId]

  if (index === undefined) {
    return undefined
  }

  const column = index % ATLAS_COLUMNS
  const row = Math.floor(index / ATLAS_COLUMNS)

  return {
    backgroundImage: `url(${SOURCE_ITEM_ATLAS_URL})`,
    backgroundPosition: `${-column * displayCellSize}px ${-row * displayCellSize}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${ATLAS_COLUMNS * displayCellSize}px ${ATLAS_ROWS * displayCellSize}px`,
    imageRendering: "pixelated",
  }
}

function sourceItemId(localizationKey: string) {
  if (!localizationKey.startsWith("sao.item.") || !localizationKey.endsWith(".name")) {
    return null
  }

  return localizationKey.slice("sao.item.".length, -".name".length)
}
