import { motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import type { CSSProperties } from "react"
import { Glyph } from "../shared/MnemosIcon"

type CristalixHeadProps = {
  skinUrl: string
  size?: number
  className?: string
}

type FaceDefinition = {
  canvas: HTMLCanvasElement | null
  baseX: number
  baseY: number
  overlayX: number
  overlayY: number
}

const HEAD_TEXTURE_SIZE = 8
const SKIN_REFERENCE_SIZE = 64

export function CristalixHead({
  skinUrl,
  size = 112,
  className,
}: CristalixHeadProps) {
  const frontRef = useRef<HTMLCanvasElement>(null)
  const rightRef = useRef<HTMLCanvasElement>(null)
  const topRef = useRef<HTMLCanvasElement>(null)
  const [loaded, setLoaded] = useState(false)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    setLoaded(false)
    setFailed(false)

    const image = new Image()

    image.onload = () => {
      if (image.naturalWidth < SKIN_REFERENCE_SIZE) {
        setFailed(true)
        return
      }

      const textureScale = image.naturalWidth / SKIN_REFERENCE_SIZE
      const faces: FaceDefinition[] = [
        {
          canvas: frontRef.current,
          baseX: 8,
          baseY: 8,
          overlayX: 40,
          overlayY: 8,
        },
        {
          canvas: rightRef.current,
          baseX: 0,
          baseY: 8,
          overlayX: 32,
          overlayY: 8,
        },
        {
          canvas: topRef.current,
          baseX: 8,
          baseY: 0,
          overlayX: 40,
          overlayY: 0,
        },
      ]

      const rendered = faces.every((face) => drawFace(image, textureScale, face))

      if (!rendered) {
        setFailed(true)
        return
      }

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

  const showPlaceholder = !loaded || failed
  const style = {
    width: size,
    height: size,
    "--cristalix-head-size": `${size}px`,
  } as CSSProperties

  return (
    <motion.div
      className={`cristalix-head cristalix-head--3d ${failed ? "cristalix-head--fallback" : ""} ${loaded ? "is-loaded" : ""} ${className ?? ""}`}
      style={style}
      initial={false}
      animate={{ scale: loaded ? 1 : 0.985 }}
      transition={{ duration: 0.28, ease: [0.22, 1, 0.36, 1] }}
      aria-label={failed ? "Cristalix skin unavailable" : "Cristalix skin head"}
    >
      <span className={`cristalix-head__placeholder ${showPlaceholder ? "is-visible" : ""}`} aria-hidden="true">
        <Glyph name="person" size={size * 0.32} />
      </span>

      <span className="cristalix-head__cube" aria-hidden="true">
        <canvas ref={frontRef} className="cristalix-head__face cristalix-head__face--front" width={HEAD_TEXTURE_SIZE} height={HEAD_TEXTURE_SIZE} />
        <canvas ref={rightRef} className="cristalix-head__face cristalix-head__face--right" width={HEAD_TEXTURE_SIZE} height={HEAD_TEXTURE_SIZE} />
        <canvas ref={topRef} className="cristalix-head__face cristalix-head__face--top" width={HEAD_TEXTURE_SIZE} height={HEAD_TEXTURE_SIZE} />
      </span>
    </motion.div>
  )
}

function drawFace(image: HTMLImageElement, textureScale: number, face: FaceDefinition) {
  const canvas = face.canvas
  const context = canvas?.getContext("2d")

  if (!canvas || !context) return false

  context.clearRect(0, 0, HEAD_TEXTURE_SIZE, HEAD_TEXTURE_SIZE)
  context.imageSmoothingEnabled = false

  context.drawImage(
    image,
    face.baseX * textureScale,
    face.baseY * textureScale,
    HEAD_TEXTURE_SIZE * textureScale,
    HEAD_TEXTURE_SIZE * textureScale,
    0,
    0,
    HEAD_TEXTURE_SIZE,
    HEAD_TEXTURE_SIZE,
  )

  context.drawImage(
    image,
    face.overlayX * textureScale,
    face.overlayY * textureScale,
    HEAD_TEXTURE_SIZE * textureScale,
    HEAD_TEXTURE_SIZE * textureScale,
    0,
    0,
    HEAD_TEXTURE_SIZE,
    HEAD_TEXTURE_SIZE,
  )

  return true
}
