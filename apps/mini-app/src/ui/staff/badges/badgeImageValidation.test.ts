import { describe, expect, it } from "vitest"
import {
  MAX_BADGE_IMAGE_DIMENSION,
  MIN_BADGE_IMAGE_DIMENSION,
  validateBadgePngHeader,
} from "./badgeImageValidation"

function pngHeader(width: number, height: number): Uint8Array {
  const header = new Uint8Array(24)
  const signature = [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]

  header.set(signature)

  const view = new DataView(header.buffer)
  view.setUint32(16, width)
  view.setUint32(20, height)

  return header
}

describe("validateBadgePngHeader", () => {
  it("accepts square PNG dimensions inside the supported range", () => {
    expect(validateBadgePngHeader(pngHeader(64, 64))).toBe(true)
    expect(validateBadgePngHeader(pngHeader(256, 256))).toBe(true)
    expect(validateBadgePngHeader(pngHeader(512, 512))).toBe(true)
  })

  it("rejects non-square and out-of-range dimensions", () => {
    const belowMinimum = MIN_BADGE_IMAGE_DIMENSION - 1
    const aboveMaximum = MAX_BADGE_IMAGE_DIMENSION + 1

    expect(validateBadgePngHeader(pngHeader(128, 64))).toBe(false)
    expect(validateBadgePngHeader(pngHeader(belowMinimum, belowMinimum))).toBe(false)
    expect(validateBadgePngHeader(pngHeader(aboveMaximum, aboveMaximum))).toBe(false)
  })

  it("rejects files without the PNG signature", () => {
    const header = pngHeader(128, 128)
    header[0] = 0

    expect(validateBadgePngHeader(header)).toBe(false)
  })
})
