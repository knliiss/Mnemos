import { useEffect, useRef, useState } from "react"
import { resolveMinecraftSkinTextureScale } from "../profile/minecraftSkinTexture"
import { renderMinecraftHead } from "../../shared/minecraftHeadRenderer"

const FALLBACK_PET_ASSET = "/event-icons/pet.png"

type Props = {
  textureUrl: string
}

export function PetInventoryArt({ textureUrl }: Props) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const [loadedTextureUrl, setLoadedTextureUrl] = useState<string | null>(null)
  const [failedTextureUrl, setFailedTextureUrl] = useState<string | null>(null)

  const loaded = loadedTextureUrl === textureUrl
  const failed = failedTextureUrl === textureUrl

  useEffect(() => {
    const canvas = canvasRef.current

    if (!canvas) {
      return
    }

    const image = new Image()
    image.decoding = "async"

    const markFailed = () => {
      setLoadedTextureUrl((current) => (current === textureUrl ? null : current))
      setFailedTextureUrl(textureUrl)
    }

    image.onload = () => {
      const textureScale = resolveMinecraftSkinTextureScale(image.naturalWidth, image.naturalHeight)

      if (textureScale === null || !renderMinecraftHead(canvas, image, textureScale)) {
        markFailed()
        return
      }

      setFailedTextureUrl((current) => (current === textureUrl ? null : current))
      setLoadedTextureUrl(textureUrl)
    }

    image.onerror = markFailed
    image.src = textureUrl

    return () => {
      image.onload = null
      image.onerror = null
    }
  }, [textureUrl])

  if (failed) {
    return (
      <img
        className="event-art__image"
        src={FALLBACK_PET_ASSET}
        alt=""
        draggable={false}
        decoding="async"
      />
    )
  }

  return (
    <canvas
      ref={canvasRef}
      className={`event-art__image event-art__pet-head ${loaded ? "is-loaded" : ""}`}
      aria-hidden="true"
    />
  )
}
