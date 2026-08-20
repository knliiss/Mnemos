import { motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import type { CSSProperties } from "react"
import { Glyph } from "../shared/MnemosIcon"

type CristalixHeadProps = {
  skinUrl: string
  size?: number
  className?: string
}

const HEAD_TEXTURE_SIZE = 8
const OVERLAY_CANVAS_SIZE = 10
const SKIN_REFERENCE_SIZE = 64

export function CristalixHead({ skinUrl, size = 112, className }: CristalixHeadProps) {
  const baseRef = useRef<HTMLCanvasElement>(null)
  const overlayRef = useRef<HTMLCanvasElement>(null)
  const [loadedSkinUrl, setLoadedSkinUrl] = useState<string | null>(null)
  const [failedSkinUrl, setFailedSkinUrl] = useState<string | null>(null)

  const loaded = loadedSkinUrl === skinUrl
  const failed = failedSkinUrl === skinUrl

  useEffect(() => {
    const image = new Image()
    image.decoding = "async"

    image.onload = () => {
      const textureScale = image.naturalWidth / SKIN_REFERENCE_SIZE

      if (
        !Number.isInteger(textureScale) ||
        textureScale < 1 ||
        image.naturalHeight < SKIN_REFERENCE_SIZE * textureScale
      ) {
        setFailedSkinUrl(skinUrl)
        return
      }

      const baseCanvas = baseRef.current
      const overlayCanvas = overlayRef.current
      const baseContext = baseCanvas?.getContext("2d")
      const overlayContext = overlayCanvas?.getContext("2d")

      if (!baseCanvas || !overlayCanvas || !baseContext || !overlayContext) {
        setFailedSkinUrl(skinUrl)
        return
      }

      drawLayer(baseContext, image, textureScale, 8, 8)
      drawOverlayLayer(overlayContext, image, textureScale)

      setFailedSkinUrl((current) => (current === skinUrl ? null : current))
      setLoadedSkinUrl(skinUrl)
    }

    image.onerror = () => {
      setLoadedSkinUrl((current) => (current === skinUrl ? null : current))
      setFailedSkinUrl(skinUrl)
    }

    image.src = skinUrl

    return () => {
      image.onload = null
      image.onerror = null
    }
  }, [skinUrl])

  const style = {
    width: size,
    height: size,
    "--cristalix-head-size": `${size}px`,
  } as CSSProperties

  return (
    <motion.span
      className={`cristalix-head cristalix-head--layered ${failed ? "cristalix-head--fallback" : ""} ${loaded ? "is-loaded" : ""} ${className ?? ""}`.trim()}
      style={style}
      initial={false}
      animate={{ opacity: loaded || failed ? 1 : 0.72, scale: loaded ? 1 : 0.985 }}
      transition={{ duration: 0.2, ease: [0.22, 1, 0.36, 1] }}
      aria-label={failed ? "Cristalix skin unavailable" : "Cristalix skin head"}
    >
      <span
        className={`cristalix-head__placeholder ${!loaded ? "is-visible" : ""}`}
        aria-hidden="true"
      >
        <Glyph name="person" size={Math.max(12, Math.round(size * 0.34))} />
      </span>

      <span className="cristalix-head__layers" aria-hidden="true">
        <canvas
          ref={baseRef}
          className="cristalix-head__canvas cristalix-head__canvas--base"
          width={HEAD_TEXTURE_SIZE}
          height={HEAD_TEXTURE_SIZE}
        />
        <canvas
          ref={overlayRef}
          className="cristalix-head__canvas cristalix-head__canvas--overlay"
          width={OVERLAY_CANVAS_SIZE}
          height={OVERLAY_CANVAS_SIZE}
        />
      </span>
    </motion.span>
  )
}

function drawLayer(
  context: CanvasRenderingContext2D,
  image: HTMLImageElement,
  textureScale: number,
  sourceX: number,
  sourceY: number,
) {
  context.clearRect(0, 0, HEAD_TEXTURE_SIZE, HEAD_TEXTURE_SIZE)
  context.imageSmoothingEnabled = false

  context.drawImage(
    image,
    sourceX * textureScale,
    sourceY * textureScale,
    HEAD_TEXTURE_SIZE * textureScale,
    HEAD_TEXTURE_SIZE * textureScale,
    0,
    0,
    HEAD_TEXTURE_SIZE,
    HEAD_TEXTURE_SIZE,
  )
}

function drawOverlayLayer(
  context: CanvasRenderingContext2D,
  image: HTMLImageElement,
  textureScale: number,
) {
  context.clearRect(0, 0, OVERLAY_CANVAS_SIZE, OVERLAY_CANVAS_SIZE)
  context.imageSmoothingEnabled = false

  // The texture is intentionally loaded directly from Cristalix. A cross-origin
  // image may be drawn to canvas, but reading the canvas back would taint it and
  // throw a SecurityError in browsers. Keep this path draw-only.
  context.drawImage(
    image,
    40 * textureScale,
    8 * textureScale,
    HEAD_TEXTURE_SIZE * textureScale,
    HEAD_TEXTURE_SIZE * textureScale,
    0,
    0,
    OVERLAY_CANVAS_SIZE,
    OVERLAY_CANVAS_SIZE,
  )
}
