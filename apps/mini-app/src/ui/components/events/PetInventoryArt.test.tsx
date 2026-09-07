// @vitest-environment jsdom

import { cleanup, render, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { PetInventoryArt } from "./PetInventoryArt"

const mocks = vi.hoisted(() => ({
  renderMinecraftHead: vi.fn<
    (canvas: HTMLCanvasElement, image: CanvasImageSource, textureScale: number) => boolean
  >(() => true),
}))

vi.mock("../../shared/minecraftHeadRenderer", () => ({
  renderMinecraftHead: mocks.renderMinecraftHead,
}))

let imageWidth = 128
let imageHeight = 128

class MockImage {
  decoding = ""
  naturalWidth = imageWidth
  naturalHeight = imageHeight
  onload: (() => void) | null = null
  onerror: (() => void) | null = null

  private source = ""

  set src(value: string) {
    this.source = value
    queueMicrotask(() => this.onload?.())
  }

  get src() {
    return this.source
  }
}

beforeEach(() => {
  imageWidth = 128
  imageHeight = 128
  mocks.renderMinecraftHead.mockReset()
  mocks.renderMinecraftHead.mockReturnValue(true)
  vi.stubGlobal("Image", MockImage)
})

afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

describe("PetInventoryArt", () => {
  it("renders HD PET skins with their resolved Minecraft texture scale", async () => {
    const { container } = render(<PetInventoryArt textureUrl="https://example.test/pet.png" />)

    await waitFor(() => expect(mocks.renderMinecraftHead).toHaveBeenCalledTimes(1))

    expect(mocks.renderMinecraftHead.mock.calls[0]?.[2]).toBe(2)
    expect(container.querySelector("canvas")?.classList.contains("is-loaded")).toBe(true)
  })

  it("shows the local PET fallback for an invalid skin texture shape", async () => {
    imageWidth = 100
    imageHeight = 100

    const { container } = render(<PetInventoryArt textureUrl="https://example.test/pet.png" />)

    await waitFor(() => expect(container.querySelector("img")).not.toBeNull())

    expect(mocks.renderMinecraftHead).not.toHaveBeenCalled()
    expect(container.querySelector("img")?.getAttribute("src")).toBe("/event-icons/pet.png")
  })
})
