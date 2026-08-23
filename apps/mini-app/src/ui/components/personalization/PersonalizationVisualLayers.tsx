import type { CSSProperties } from "react"
import type { VisualLayer, VisualLayerBlendMode } from "../../model"
import { PersonalizationAssetImage } from "./PersonalizationAssetImage"
import "./personalization-visual-layers.css"

type Props = {
  layers: VisualLayer[]
  className?: string
}

const blendModeByValue: Record<VisualLayerBlendMode, CSSProperties["mixBlendMode"]> = {
  NORMAL: "normal",
  SCREEN: "screen",
  MULTIPLY: "multiply",
  OVERLAY: "overlay",
  SOFT_LIGHT: "soft-light",
}

export function PersonalizationVisualLayers({ layers, className = "" }: Props) {
  if (!layers.length) {
    return null
  }

  return (
    <span className={`personalization-visual-layers ${className}`.trim()} aria-hidden="true">
      {[...layers]
        .sort((left, right) => left.zIndex - right.zIndex)
        .map((layer) => (
          <span
            className={`personalization-visual-layer is-${layer.animation.toLowerCase()}`}
            key={layer.id}
            style={{
              left: `${layer.xPercent}%`,
              top: `${layer.yPercent}%`,
              width: `${layer.widthPercent}%`,
              opacity: layer.opacity / 100,
              zIndex: layer.zIndex,
              mixBlendMode: blendModeByValue[layer.blendMode],
              transform: `translate(-50%, -50%) rotate(${layer.rotationDegrees}deg)`,
            }}
          >
            <PersonalizationAssetImage src={layer.assetUrl} />
          </span>
        ))}
    </span>
  )
}
