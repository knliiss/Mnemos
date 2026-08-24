import { motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import type { CSSProperties } from "react"
import { Glyph } from "../../shared/MnemosIcon"
import "./cristalix-head.css"

type CristalixHeadProps = {
  skinUrl: string
  size?: number
  className?: string
}

const HEAD_TEXTURE_SIZE = 8
const SKIN_REFERENCE_SIZE = 64

const STEVE_FACE = [
  ["#6f4b32", "#704a31", "#62402d", "#5a3928", "#5b3a29", "#68452f", "#745039", "#76513a"],
  ["#704a31", "#9a6845", "#a6714b", "#a86f4a", "#a56b48", "#a16a47", "#96613f", "#704a31"],
  ["#9d6946", "#ad7650", "#b57b54", "#b77e56", "#b57a53", "#b07952", "#a66f4b", "#95603f"],
  ["#a46d49", "#ece7d8", "#5d7187", "#ad7650", "#ac744e", "#5d7187", "#ece7d8", "#9b6544"],
  ["#a26b48", "#f4efdf", "#3c5268", "#ad754f", "#aa724d", "#3c5268", "#f4efdf", "#986241"],
  ["#9f6845", "#aa724c", "#aa714c", "#784936", "#784936", "#a66d49", "#a36b47", "#95603f"],
  ["#97613f", "#a56c48", "#774936", "#7c4b37", "#7c4b37", "#754532", "#9e6644", "#8f5b3d"],
  ["#68432f", "#8d593b", "#9b6341", "#9f6643", "#9d6542", "#965f40", "#875337", "#64402d"],
] as const

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

    const renderSteveFallback = () => {
      const baseCanvas = baseRef.current
      const overlayCanvas = overlayRef.current
      const baseContext = baseCanvas?.getContext("2d")
      const overlayContext = overlayCanvas?.getContext("2d")

      if (!baseCanvas || !overlayCanvas || !baseContext || !overlayContext) {
        setLoadedSkinUrl((current) => (current === skinUrl ? null : current))
        setFailedSkinUrl(skinUrl)
        return
      }

      drawSteveFace(baseContext)
      overlayContext.clearRect(0, 0, HEAD_TEXTURE_SIZE, HEAD_TEXTURE_SIZE)

      setLoadedSkinUrl(skinUrl)
      setFailedSkinUrl(skinUrl)
    }

    image.onload = () => {
      const textureScale = image.naturalWidth / SKIN_REFERENCE_SIZE

      if (
        !Number.isInteger(textureScale) ||
        textureScale < 1 ||
        image.naturalHeight < SKIN_REFERENCE_SIZE * textureScale
      ) {
        renderSteveFallback()
        return
      }

      const baseCanvas = baseRef.current
      const overlayCanvas = overlayRef.current
      const baseContext = baseCanvas?.getContext("2d")
      const overlayContext = overlayCanvas?.getContext("2d")

      if (!baseCanvas || !overlayCanvas || !baseContext || !overlayContext) {
        renderSteveFallback()
        return
      }

      drawLayer(baseContext, image, textureScale, 8, 8)
      drawLayer(overlayContext, image, textureScale, 40, 8)

      setFailedSkinUrl((current) => (current === skinUrl ? null : current))
      setLoadedSkinUrl(skinUrl)
    }

    image.onerror = renderSteveFallback
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
      role="img"
      initial={false}
      animate={{ opacity: loaded || failed ? 1 : 0.72, scale: loaded ? 1 : 0.985 }}
      transition={{ duration: 0.2, ease: [0.22, 1, 0.36, 1] }}
      aria-label={failed ? "Default Steve skin head" : "Cristalix skin head"}
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
          width={HEAD_TEXTURE_SIZE}
          height={HEAD_TEXTURE_SIZE}
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

function drawSteveFace(context: CanvasRenderingContext2D) {
  context.clearRect(0, 0, HEAD_TEXTURE_SIZE, HEAD_TEXTURE_SIZE)
  context.imageSmoothingEnabled = false

  STEVE_FACE.forEach((row, y) => {
    row.forEach((color, x) => {
      context.fillStyle = color
      context.fillRect(x, y, 1, 1)
    })
  })
}
