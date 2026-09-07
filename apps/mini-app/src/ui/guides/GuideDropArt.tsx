import type { CSSProperties } from "react"
import { PetInventoryArt } from "../components/events/PetInventoryArt"
import { resolveSaoPetInventoryTextureUrl } from "../components/events/saoPetInventoryArtwork"
import { guideMaterialArtworkStyle } from "./guideMaterialArtwork"
import { guideWeaponArtworkStyle } from "./guideWeaponArtwork"

type Props = {
  localizationKey: string
  size?: "card" | "detail"
}

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

  const weaponStyle = guideWeaponArtworkStyle(localizationKey)

  if (weaponStyle) {
    return <GuideAtlasArtwork style={weaponStyle} size={size} type="weapon" />
  }

  const materialStyle = guideMaterialArtworkStyle(localizationKey)

  if (materialStyle) {
    return <GuideAtlasArtwork style={materialStyle} size={size} type="material" />
  }

  if (localizationKey.startsWith("sao.item.aura")) {
    return (
      <span
        className={`guide-drop-art guide-drop-art--aura guide-drop-art--${size}`}
        aria-hidden="true"
      >
        <i />
        <b />
      </span>
    )
  }

  return (
    <span
      className={`guide-drop-art guide-drop-art--artifact guide-drop-art--${size}`}
      aria-hidden="true"
    >
      <i />
      <b />
      <em />
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
  type: "weapon" | "material"
}) {
  return (
    <span
      className={`guide-drop-art guide-drop-art--${type} guide-drop-art--${size}`}
      style={style}
      aria-hidden="true"
    />
  )
}
