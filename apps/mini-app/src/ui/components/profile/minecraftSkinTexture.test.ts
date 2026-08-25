import { describe, expect, it } from "vitest"
import { resolveMinecraftSkinTextureScale } from "./minecraftSkinTexture"

describe("resolveMinecraftSkinTextureScale", () => {
  it.each([
    [64, 32, 1],
    [64, 64, 1],
    [128, 64, 2],
    [128, 128, 2],
    [1024, 512, 16],
    [1024, 1024, 16],
  ])("accepts Minecraft skin texture %sx%s", (width, height, expectedScale) => {
    expect(resolveMinecraftSkinTextureScale(width, height)).toBe(expectedScale)
  })

  it.each([
    [63, 32],
    [64, 48],
    [1024, 768],
    [0, 0],
  ])("rejects invalid skin texture %sx%s", (width, height) => {
    expect(resolveMinecraftSkinTextureScale(width, height)).toBeNull()
  })
})
