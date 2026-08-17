import { motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
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
      const canvas = canvasRef.current
      const context = canvas?.getContext("2d")

      if (!canvas || !context || image.naturalWidth < SKIN_REFERENCE_SIZE) {
        setFailed(true)
        return
      }

      const textureScale = image.naturalWidth / SKIN_REFERENCE_SIZE

      context.clearRect(0, 0, HEAD_TEXTURE_SIZE, HEAD_TEXTURE_SIZE)
      context.imageSmoothingEnabled = false

      context.drawImage(
        image,
        8 * textureScale,
        8 * textureScale,
        8 * textureScale,
        8 * textureScale,
        0,
        0,
        HEAD_TEXTURE_SIZE,
        HEAD_TEXTURE_SIZE,
      )

      context.drawImage(
        image,
        40 * textureScale,
        8 * textureScale,
        8 * textureScale,
        8 * textureScale,
        0,
        0,
        HEAD_TEXTURE_SIZE,
        HEAD_TEXTURE_SIZE,
      )

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

  return (
    <motion.div
      className={`cristalix-head ${failed ? "cristalix-head--fallback" : ""} ${className ?? ""}`}
      style={{ width: size, height: size }}
      initial={false}
      animate={{ scale: loaded ? 1 : 0.985 }}
      transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}
      aria-label={failed ? "Cristalix skin unavailable" : "Cristalix skin head"}
    >
      <span className={`cristalix-head__placeholder ${showPlaceholder ? "is-visible" : ""}`} aria-hidden="true">
        <Glyph name="person" size={size * 0.34} />
      </span>
      <canvas
        ref={canvasRef}
        className={loaded ? "is-loaded" : ""}
        width={HEAD_TEXTURE_SIZE}
        height={HEAD_TEXTURE_SIZE}
        aria-hidden="true"
      />
    </motion.div>
  )
}
