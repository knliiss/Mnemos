import type { CSSProperties } from "react"
import { PetInventoryArt } from "../components/events/PetInventoryArt"
import { resolveSaoPetInventoryTextureUrl } from "../components/events/saoPetInventoryArtwork"
import {
  resolveSaoItemAtlasSprite,
  type SaoItemAtlasSprite,
} from "../components/events/saoTrackedItemArtwork"
import "../events/event-art.css"
import { guideMaterialArtworkStyle } from "./guideMaterialArtwork"
import { guideSourceItemArtworkStyle } from "./guideSourceItemArtwork"
import { resolveGuideWeaponSprite } from "./guideWeaponArtwork"

type Props = {
  localizationKey: string
  size?: "card" | "detail"
}

type ArtworkType = "source" | "material" | "missing"

export function GuideDropArt({ localizationKey, size = "card" }: Props) {
  const petTextureUrl = resolveSaoPetInventoryTextureUrl(localizationKey)

  if (petTextureUrl) {
    return <GuideInventoryArtwork petTextureUrl={petTextureUrl} size={size} />
  }

  const trackedSprite = resolveSaoItemAtlasSprite(localizationKey)

  if (trackedSprite) {
    return <GuideInventoryArtwork itemSprite={trackedSprite} size={size} />
  }

  const guideWeaponSprite = resolveGuideWeaponSprite(localizationKey)

  if (guideWeaponSprite) {
    return <GuideInventoryArtwork itemSprite={guideWeaponSprite} size={size} />
  }

  const displayCellSize = guideArtFrameSize(size)
  const sourceStyle = guideSourceItemArtworkStyle(localizationKey, displayCellSize)

  if (sourceStyle) {
    return <GuideAtlasArtwork style={sourceStyle} size={size} type="source" />
  }

  const materialStyle = guideMaterialArtworkStyle(localizationKey)

  if (materialStyle) {
    return <GuideAtlasArtwork style={materialStyle} size={size} type="material" />
  }

  return <GuideAtlasArtwork style={{}} size={size} type="missing" />
}

function GuideInventoryArtwork({
  size,
  itemSprite = null,
  petTextureUrl = null,
}: {
  size: "card" | "detail"
  itemSprite?: SaoItemAtlasSprite | null
  petTextureUrl?: string | null
}) {
  const frameSize = guideArtFrameSize(size)
  const contentSize = eventItemContentSize(frameSize)
  const variant = petTextureUrl ? "drop-pet" : "drop-sword"
  const style = {
    width: frameSize,
    height: frameSize,
    "--event-art-content-size": `${contentSize}px`,
  } as CSSProperties

  return (
    <span
      className={`guide-drop-art guide-drop-art--inventory guide-drop-art--${size} event-art event-art--${variant} event-art--asset`}
      style={style}
      aria-hidden="true"
    >
      {petTextureUrl ? (
        <PetInventoryArt textureUrl={petTextureUrl} />
      ) : itemSprite ? (
        <span className="event-art__image" style={trackedItemStyle(itemSprite, contentSize)} />
      ) : null}
    </span>
  )
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

function trackedItemStyle(sprite: SaoItemAtlasSprite, contentSize: number): CSSProperties {
  return {
    backgroundImage: `url(${sprite.atlas.path})`,
    backgroundPosition: `${-sprite.column * contentSize}px ${-sprite.row * contentSize}px`,
    backgroundRepeat: "no-repeat",
    backgroundSize: `${sprite.atlas.columns * contentSize}px ${sprite.atlas.rows * contentSize}px`,
    imageRendering: "pixelated",
  }
}

function guideArtFrameSize(size: "card" | "detail") {
  return size === "detail" ? 92 : 56
}

function eventItemContentSize(frameSize: number) {
  return Math.min(30, Math.max(18, Math.round(frameSize * 0.66)))
}
