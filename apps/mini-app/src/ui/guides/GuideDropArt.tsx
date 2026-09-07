import type { CSSProperties } from "react"
import { PetInventoryArt } from "../components/events/PetInventoryArt"
import { resolveSaoPetInventoryTextureUrl } from "../components/events/saoPetInventoryArtwork"
import {
  resolveSaoItemAtlasSprite,
  type SaoItemAtlasSprite,
} from "../components/events/saoTrackedItemArtwork"
import { guideMaterialArtworkStyle } from "./guideMaterialArtwork"
import { guideSourceItemArtworkStyle } from "./guideSourceItemArtwork"
import { guideWeaponArtworkStyle } from "./guideWeaponArtwork"

type Props = {
  localizationKey: string
  size?: "card" | "detail"
}

type ArtworkType = "source" | "tracked" | "weapon" | "material" | "missing"

export function GuideDropArt({ localizationKey, size = "card" }: Props) {
  const petTextureUrl = resolveSaoPetInventoryTextureUrl(localizationKey)

  if (petTextureUrl) {
    return (
      <span
        className={`guide-drop-art guide-drop-art--pet guide-drop-art--${size}`}
        aria-hidden="true"
      >
        <PetInventoryArt textureUrl={petTextureUrl} />
      </span>
    )
  }

  const displayCellSize = size === "detail" ? 92 : 56
  const sourceStyle = guideSourceItemArtworkStyle(localizationKey, displayCellSize)

  if (sourceStyle) {
    return <GuideAtlasArtwork style={sourceStyle} size={size} type="source" />
  }

  const trackedSprite = resolveSaoItemAtlasSprite(localizationKey)

  if (trackedSprite) {
    return (
      <GuideAtlasArtwork
        style={trackedItemStyle(trackedSprite, displayCellSize)}
        size={size}
        type="tracked"
      />
    )
  }

  const weaponStyle = guideWeaponArtworkStyle(localizationKey)

  if (weaponStyle) {
    return <GuideAtlasArtwork style={weaponStyle} size={size} type="weapon" />
  }

  const materialStyle = guideMaterialArtworkStyle(localizationKey)

  if (materialStyle) {
    return <GuideAtlasArtwork style={materialStyle} size={size} type="material" />
  }

  return <GuideAtlasArtwork style={{}} size={size} type="missing" />
}

function GuideAtlasArtwork({
  style,
  size,
  type,
}: {
  style: CSSProperties
  size: "card" | "detail"
  type: ArtworkType
}) {
  return (
    <span
      className={`guide-drop-art guide-drop-art--${type} guide-drop-art--${size}`}
      style={style}
      aria-hidden="true"
    />
  )
}

function trackedItemStyle(sprite: SaoItemAtlasSprite, displayCellSize: number): CSSProperties {
  return {
    backgroundImage: `url(${sprite.atlas.path})`,
    backgroundPosition: `${-sprite.column * displayCellSize}px ${-sprite.row * displayCellSize}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${sprite.atlas.columns * displayCellSize}px ${sprite.atlas.rows * displayCellSize}px`,
    imageRendering: "pixelated",
  }
}
