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
const SKIN_REFERENCE_SIZE = 64

export function CristalixHead({
  skinUrl,
  size = 112,
  className,
}: CristalixHeadProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const [loaded, setLoaded] = useState(false)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    setLoaded(false)
    setFailed(false)

    const image = new Image()

    image.onload = () => {
      const textureScale = image.naturalWidth / SKIN_REFERENCE_SIZE

      if (!Number.isInteger(textureScale) || textureScale < 1 || image.naturalHeight < SKIN_REFERENCE_SIZE * textureScale) {
        setFailed(true)
        return
      }

      const canvas = canvasRef.current
      const context = canvas?.getContext("2d")

      if (!canvas || !context) {
        setFailed(true)
        return
      }

      context.clearRect(0, 0, HEAD_TEXTURE_SIZE, HEAD_TEXTURE_SIZE)
      context.imageSmoothingEnabled = false

      drawLayer(context, image, textureScale, 8, 8)
      drawLayer(context, image, textureScale, 40, 8)

      setLoaded(true)
    }

    image.onerror = () => {
      setFailed(true)
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
      className={`cristalix-head cristalix-head--face ${failed ? "cristalix-head--fallback" : ""} ${loaded ? "is-loaded" : ""} ${className ?? ""}`.trim()}
      style={style}
      initial={false}
      animate={{ opacity: loaded || failed ? 1 : 0.7, scale: loaded ? 1 : 0.985 }}
      transition={{ duration: 0.2, ease: [0.22, 1, 0.36, 1] }}
      aria-label={failed ? "Cristalix skin unavailable" : "Cristalix skin head"}
    >
      <span className={`cristalix-head__placeholder ${!loaded ? "is-visible" : ""}`} aria-hidden="true">
        <Glyph name="person" size={Math.max(12, Math.round(size * 0.34))} />
      </span>

      <canvas
        ref={canvasRef}
        className="cristalix-head__canvas"
        width={HEAD_TEXTURE_SIZE}
        height={HEAD_TEXTURE_SIZE}
        aria-hidden="true"
      />
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
