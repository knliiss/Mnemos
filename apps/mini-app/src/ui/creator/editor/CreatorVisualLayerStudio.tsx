import type { Dispatch, PointerEvent, SetStateAction } from "react"
import { useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  listCreatorPersonalizationAssets,
  uploadCreatorPersonalizationAsset,
  type PersonalizationAssetResponse,
} from "@/shared/api/creatorPersonalizationApi"
import { handleError } from "@/shared/errors/errorHandler"
import type {
  VisualLayerAnimation,
  VisualLayerBlendMode,
} from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PersonalizationAssetImage } from "../../components/personalization/PersonalizationAssetImage"
import type { CreatorEditorForm, CreatorVisualLayerForm } from "./creatorEditorModel"
import "./creator-layer-studio.css"

type LayeredType = "PROFILE_THEME" | "AVATAR_FRAME" | "PROFILE_EFFECT"

type Props = {
  form: CreatorEditorForm
  editable: boolean
  setForm: Dispatch<SetStateAction<CreatorEditorForm>>
}

const blendModes: VisualLayerBlendMode[] = [
  "NORMAL",
  "SCREEN",
  "MULTIPLY",
  "OVERLAY",
  "SOFT_LIGHT",
]

const animations: VisualLayerAnimation[] = ["NONE", "FLOAT", "PULSE", "SPIN", "DRIFT"]

function isLayeredType(type: CreatorEditorForm["type"]): type is LayeredType {
  return ["PROFILE_THEME", "AVATAR_FRAME", "PROFILE_EFFECT"].includes(type)
}

function currentLayers(form: CreatorEditorForm): CreatorVisualLayerForm[] {
  if (form.type === "PROFILE_THEME") {
    return form.profileTheme.layers
  }

  if (form.type === "AVATAR_FRAME") {
    return form.avatarFrame.layers
  }

  if (form.type === "PROFILE_EFFECT") {
    return form.profileEffect.layers
  }

  return []
}

function layerId(): string {
  return `layer-${crypto.randomUUID().replaceAll("-", "").slice(0, 12)}`
}

function newLayer(asset: PersonalizationAssetResponse, zIndex: number): CreatorVisualLayerForm {
  return {
    id: layerId(),
    assetId: asset.id,
    assetUrl: asset.contentUrl,
    xPercent: 50,
    yPercent: 50,
    widthPercent: 48,
    opacity: 100,
    rotationDegrees: 0,
    blendMode: "NORMAL",
    animation: "NONE",
    zIndex,
  }
}

export function CreatorVisualLayerStudio({ form, editable, setForm }: Props) {
  const { t } = useTranslation()
  const canvasRef = useRef<HTMLDivElement>(null)
  const [assets, setAssets] = useState<PersonalizationAssetResponse[]>([])
  const [selectedLayerId, setSelectedLayerId] = useState<string | null>(null)
  const [loadingAssets, setLoadingAssets] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [draggingLayerId, setDraggingLayerId] = useState<string | null>(null)
  const layers = currentLayers(form)

  useEffect(() => {
    let cancelled = false

    setLoadingAssets(true)

    void listCreatorPersonalizationAssets()
      .then((response) => {
        if (!cancelled) {
          setAssets(response)
        }
      })
      .catch((error) => {
        if (!cancelled) {
          handleError(error)
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoadingAssets(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (selectedLayerId && !layers.some((layer) => layer.id === selectedLayerId)) {
      setSelectedLayerId(layers.at(-1)?.id ?? null)
    }
  }, [layers, selectedLayerId])

  const selectedLayer = useMemo(
    () => layers.find((layer) => layer.id === selectedLayerId) ?? null,
    [layers, selectedLayerId],
  )

  if (!isLayeredType(form.type)) {
    return null
  }

  const setLayers = (nextLayers: CreatorVisualLayerForm[]) => {
    const type = form.type

    setForm((current) => {
      if (type === "PROFILE_THEME") {
        return {
          ...current,
          profileTheme: {
            ...current.profileTheme,
            layers: nextLayers,
          },
        }
      }

      if (type === "AVATAR_FRAME") {
        return {
          ...current,
          avatarFrame: {
            ...current.avatarFrame,
            layers: nextLayers,
          },
        }
      }

      return {
        ...current,
        profileEffect: {
          ...current.profileEffect,
          layers: nextLayers,
        },
      }
    })
  }

  const updateLayer = (layerIdValue: string, patch: Partial<CreatorVisualLayerForm>) => {
    setLayers(
      layers.map((layer) =>
        layer.id === layerIdValue
          ? {
              ...layer,
              ...patch,
            }
          : layer,
      ),
    )
  }

  const addAsset = (asset: PersonalizationAssetResponse) => {
    if (!editable || layers.length >= 8) {
      return
    }

    const highestZ = layers.reduce((highest, layer) => Math.max(highest, layer.zIndex), -1)
    const layer = newLayer(asset, Math.min(highestZ + 1, 20))

    setLayers([...layers, layer])
    setSelectedLayerId(layer.id)
  }

  const duplicateLayer = () => {
    if (!editable || !selectedLayer || layers.length >= 8) {
      return
    }

    const highestZ = layers.reduce((highest, layer) => Math.max(highest, layer.zIndex), -1)
    const duplicate: CreatorVisualLayerForm = {
      ...selectedLayer,
      id: layerId(),
      xPercent: Math.min(150, selectedLayer.xPercent + 4),
      yPercent: Math.min(150, selectedLayer.yPercent + 4),
      zIndex: Math.min(highestZ + 1, 20),
    }

    setLayers([...layers, duplicate])
    setSelectedLayerId(duplicate.id)
  }

  const removeLayer = () => {
    if (!editable || !selectedLayer) {
      return
    }

    const next = layers.filter((layer) => layer.id !== selectedLayer.id)

    setLayers(next)
    setSelectedLayerId(next.at(-1)?.id ?? null)
  }

  const moveZ = (direction: -1 | 1) => {
    if (!editable || !selectedLayer) {
      return
    }

    updateLayer(selectedLayer.id, {
      zIndex: Math.max(0, Math.min(20, selectedLayer.zIndex + direction)),
    })
  }

  const upload = async (file: File | null) => {
    if (!file || !editable || uploading) {
      return
    }

    setUploading(true)

    try {
      const asset = await uploadCreatorPersonalizationAsset(file)

      setAssets((current) => [asset, ...current.filter((item) => item.id !== asset.id)])
      addAsset(asset)
    } catch (error) {
      handleError(error)
    } finally {
      setUploading(false)
    }
  }

  const updatePosition = (event: PointerEvent<HTMLDivElement>) => {
    if (!editable || !draggingLayerId || !canvasRef.current) {
      return
    }

    const bounds = canvasRef.current.getBoundingClientRect()
    const xPercent = Math.round(((event.clientX - bounds.left) / bounds.width) * 100)
    const yPercent = Math.round(((event.clientY - bounds.top) / bounds.height) * 100)

    updateLayer(draggingLayerId, {
      xPercent: Math.max(-50, Math.min(150, xPercent)),
      yPercent: Math.max(-50, Math.min(150, yPercent)),
    })
  }

  return (
    <section className="creator-layer-studio">
      <header className="creator-layer-studio__header">
        <span>
          <small>{t("creator.editor.layersEyebrow")}</small>
          <strong>{t("creator.editor.layersTitle")}</strong>
        </span>
        <em>{t("creator.editor.layerCount", { count: layers.length })}</em>
      </header>

      <div className="creator-layer-studio__workspace">
        <div
          ref={canvasRef}
          className="creator-layer-canvas"
          onPointerMove={updatePosition}
          onPointerUp={() => setDraggingLayerId(null)}
          onPointerCancel={() => setDraggingLayerId(null)}
          onPointerLeave={() => setDraggingLayerId(null)}
        >
          <div className="creator-layer-canvas__profile">
            <span className="creator-layer-canvas__avatar">M</span>
            <span>
              <strong>Mnemos</strong>
              <small>{t("creator.editor.canvasHint")}</small>
            </span>
          </div>

          {[...layers]
            .sort((left, right) => left.zIndex - right.zIndex)
            .map((layer) => (
              <button
                type="button"
                key={layer.id}
                className={
                  layer.id === selectedLayerId
                    ? "creator-layer-canvas__layer is-selected"
                    : "creator-layer-canvas__layer"
                }
                style={{
                  left: `${layer.xPercent}%`,
                  top: `${layer.yPercent}%`,
                  width: `${layer.widthPercent}%`,
                  opacity: layer.opacity / 100,
                  zIndex: layer.zIndex + 2,
                  mixBlendMode: layer.blendMode.toLowerCase().replace("_", "-") as
                    | "normal"
                    | "screen"
                    | "multiply"
                    | "overlay"
                    | "soft-light",
                  transform: `translate(-50%, -50%) rotate(${layer.rotationDegrees}deg)`,
                }}
                disabled={!editable}
                onPointerDown={(event) => {
                  event.currentTarget.setPointerCapture(event.pointerId)
                  setSelectedLayerId(layer.id)
                  setDraggingLayerId(layer.id)
                }}
                onClick={() => setSelectedLayerId(layer.id)}
              >
                <PersonalizationAssetImage src={layer.assetUrl} />
              </button>
            ))}

          {!layers.length ? (
            <span className="creator-layer-canvas__empty">
              <Glyph name="creator" size={22} />
              {t("creator.editor.emptyCanvas")}
            </span>
          ) : null}
        </div>

        <aside className="creator-layer-inspector">
          {selectedLayer ? (
            <>
              <div className="creator-layer-inspector__actions">
                <button type="button" disabled={!editable} onClick={() => moveZ(-1)}>
                  {t("creator.editor.layerBack")}
                </button>
                <button type="button" disabled={!editable} onClick={() => moveZ(1)}>
                  {t("creator.editor.layerForward")}
                </button>
                <button
                  type="button"
                  disabled={!editable || layers.length >= 8}
                  onClick={duplicateLayer}
                >
                  {t("creator.editor.layerDuplicate")}
                </button>
                <button type="button" disabled={!editable} onClick={removeLayer}>
                  {t("creator.editor.layerDelete")}
                </button>
              </div>

              <LayerRange
                label="X"
                min={-50}
                max={150}
                value={selectedLayer.xPercent}
                disabled={!editable}
                onChange={(xPercent) => updateLayer(selectedLayer.id, { xPercent })}
              />
              <LayerRange
                label="Y"
                min={-50}
                max={150}
                value={selectedLayer.yPercent}
                disabled={!editable}
                onChange={(yPercent) => updateLayer(selectedLayer.id, { yPercent })}
              />
              <LayerRange
                label={t("creator.editor.layerSize")}
                min={5}
                max={200}
                value={selectedLayer.widthPercent}
                disabled={!editable}
                onChange={(widthPercent) => updateLayer(selectedLayer.id, { widthPercent })}
              />
              <LayerRange
                label={t("creator.editor.layerOpacity")}
                min={1}
                max={100}
                value={selectedLayer.opacity}
                disabled={!editable}
                onChange={(opacity) => updateLayer(selectedLayer.id, { opacity })}
              />
              <LayerRange
                label={t("creator.editor.layerRotation")}
                min={-180}
                max={180}
                value={selectedLayer.rotationDegrees}
                disabled={!editable}
                suffix="°"
                onChange={(rotationDegrees) =>
                  updateLayer(selectedLayer.id, { rotationDegrees })
                }
              />

              <label className="creator-layer-select">
                <span>{t("creator.editor.layerBlend")}</span>
                <select
                  value={selectedLayer.blendMode}
                  disabled={!editable}
                  onChange={(event) =>
                    updateLayer(selectedLayer.id, {
                      blendMode: event.target.value as VisualLayerBlendMode,
                    })
                  }
                >
                  {blendModes.map((mode) => (
                    <option value={mode} key={mode}>
                      {t(`creator.editor.blendModes.${mode}`)}
                    </option>
                  ))}
                </select>
              </label>

              <label className="creator-layer-select">
                <span>{t("creator.editor.layerAnimation")}</span>
                <select
                  value={selectedLayer.animation}
                  disabled={!editable}
                  onChange={(event) =>
                    updateLayer(selectedLayer.id, {
                      animation: event.target.value as VisualLayerAnimation,
                    })
                  }
                >
                  {animations.map((animation) => (
                    <option value={animation} key={animation}>
                      {t(`creator.editor.layerAnimations.${animation}`)}
                    </option>
                  ))}
                </select>
              </label>
            </>
          ) : (
            <p>{t("creator.editor.selectLayer")}</p>
          )}
        </aside>
      </div>

      <section className="creator-asset-library">
        <header>
          <span>
            <small>{t("creator.editor.assetsEyebrow")}</small>
            <strong>{t("creator.editor.assetsTitle")}</strong>
          </span>
          <label className="creator-asset-upload">
            <input
              type="file"
              accept="image/png"
              disabled={!editable || uploading}
              onChange={(event) => {
                void upload(event.target.files?.[0] ?? null)
                event.currentTarget.value = ""
              }}
            />
            <Glyph name="plus" size={14} />
            {uploading ? t("creator.editor.assetUploading") : t("creator.editor.assetUpload")}
          </label>
        </header>

        {loadingAssets ? (
          <p className="creator-asset-library__hint">{t("creator.editor.assetsLoading")}</p>
        ) : assets.length ? (
          <div className="creator-asset-library__grid">
            {assets.map((asset) => (
              <button
                type="button"
                key={asset.id}
                disabled={!editable || layers.length >= 8}
                onClick={() => addAsset(asset)}
              >
                <span>
                  <PersonalizationAssetImage src={asset.contentUrl} />
                </span>
                <small>
                  {asset.widthPx}×{asset.heightPx}
                </small>
              </button>
            ))}
          </div>
        ) : (
          <p className="creator-asset-library__hint">{t("creator.editor.assetsEmpty")}</p>
        )}
      </section>
    </section>
  )
}

type LayerRangeProps = {
  label: string
  value: number
  min: number
  max: number
  disabled: boolean
  suffix?: string
  onChange(value: number): void
}

function LayerRange({
  label,
  value,
  min,
  max,
  disabled,
  suffix = "%",
  onChange,
}: LayerRangeProps) {
  return (
    <label className="creator-layer-range">
      <span>{label}</span>
      <input
        type="range"
        value={value}
        min={min}
        max={max}
        disabled={disabled}
        onChange={(event) => onChange(Number(event.target.value))}
      />
      <output>
        {value}
        {suffix}
      </output>
    </label>
  )
}
