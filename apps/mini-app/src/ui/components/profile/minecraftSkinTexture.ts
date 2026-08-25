const SKIN_REFERENCE_WIDTH = 64
const LEGACY_SKIN_REFERENCE_HEIGHT = 32
const MODERN_SKIN_REFERENCE_HEIGHT = 64

export function resolveMinecraftSkinTextureScale(width: number, height: number): number | null {
  const textureScale = width / SKIN_REFERENCE_WIDTH

  if (!Number.isInteger(textureScale) || textureScale < 1) {
    return null
  }

  const legacyHeight = LEGACY_SKIN_REFERENCE_HEIGHT * textureScale
  const modernHeight = MODERN_SKIN_REFERENCE_HEIGHT * textureScale

  if (height !== legacyHeight && height !== modernHeight) {
    return null
  }

  return textureScale
}
