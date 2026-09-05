import { motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import type { CSSProperties } from "react"
import { Glyph } from "../../shared/MnemosIcon"
import {
  createSteveHeadTexture,
  renderMinecraftHead,
} from "./minecraftHeadRenderer"
import { resolveMinecraftSkinTextureScale } from "./minecraftSkinTexture"
import "./cristalix-head.css"

type CristalixHeadProps = {
  skinUrl: string
  size?: number
  className?: string
}

export function CristalixHead({ skinUrl, size = 112, className }: CristalixHeadProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const [loadedSkinUrl, setLoadedSkinUrl] = useState<string | null>(null)
  const [failedSkinUrl, setFailedSkinUrl] = useState<string | null>(null)

  const loaded = loadedSkinUrl === skinUrl
  const failed = failedSkinUrl === skinUrl

  useEffect(() => {
    const canvas = canvasRef.current

    if (!canvas) {
      return
    }

    const image = new Image()
    image.decoding = "async"

    const renderFallback = () => {
      const rendered = renderMinecraftHead(canvas, createSteveHeadTexture(), 1)

      if (!rendered) {
        setLoadedSkinUrl((current) => (current === skinUrl ? null : current))
        setFailedSkinUrl(skinUrl)
        return
      }

      setLoadedSkinUrl(skinUrl)
      setFailedSkinUrl(skinUrl)
    }

    image.onload = () => {
      const textureScale = resolveMinecraftSkinTextureScale(image.naturalWidth, image.naturalHeight)

      if (textureScale === null || !renderMinecraftHead(canvas, image, textureScale)) {
        renderFallback()
        return
      }

      setFailedSkinUrl((current) => (current === skinUrl ? null : current))
      setLoadedSkinUrl(skinUrl)
    }

    image.onerror = renderFallback
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
      className={`cristalix-head cristalix-head--3d ${failed ? "cristalix-head--fallback" : ""} ${loaded ? "is-loaded" : ""} ${className ?? ""}`.trim()}
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

      <canvas ref={canvasRef} className="cristalix-head__canvas" aria-hidden="true" />
    </motion.span>
  )
}
