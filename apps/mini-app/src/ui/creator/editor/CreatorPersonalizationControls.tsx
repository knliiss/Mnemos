import type { Dispatch, SetStateAction } from "react"
import { useTranslation } from "react-i18next"
import type { EffectType, FrameStyle, GradientStop } from "../../model"
import { Field } from "../../shared/Primitives"
import { RangeControl } from "../../shared/RangeControl"
import type { CreatorEditorForm, CreatorGlowForm } from "./creatorEditorModel"

const frameStyles: FrameStyle[] = ["SOLID", "DOUBLE", "SEGMENTED"]
const effectTypes: EffectType[] = ["AURORA", "PULSE", "SHIMMER", "PARTICLES"]
const gradientPositions = [25, 50, 75, 100, 0]

type Props = {
  form: CreatorEditorForm
  editable: boolean
  setForm: Dispatch<SetStateAction<CreatorEditorForm>>
}

type RangeFieldProps = {
  label: string
  value: number
  min: number
  max: number
  suffix?: string
  disabled: boolean
  onChange(value: number): void
}

type ColorFieldProps = {
  label: string
  value: string
  disabled: boolean
  onChange(value: string): void
}

type GlowControlsProps = {
  glow: CreatorGlowForm
  editable: boolean
  onChange(glow: CreatorGlowForm): void
}

function RangeField({ label, value, min, max, suffix = "%", disabled, onChange }: RangeFieldProps) {
  return (
    <Field label={label}>
      <div className="creator-parameter-range">
        <RangeControl
          value={value}
          min={min}
          max={max}
          disabled={disabled}
          onChange={onChange}
        />
        <output>
          {value}
          {suffix}
        </output>
      </div>
    </Field>
  )
}

function ColorField({ label, value, disabled, onChange }: ColorFieldProps) {
  return (
    <Field label={label}>
      <label className="creator-color-control">
        <input
          type="color"
          value={value}
          disabled={disabled}
          onChange={(event) => onChange(event.target.value.toUpperCase())}
        />
        <code>{value}</code>
      </label>
    </Field>
  )
}

function GlowControls({ glow, editable, onChange }: GlowControlsProps) {
  const { t } = useTranslation()

  return (
    <section className="creator-parameter-section creator-glow-controls">
      <header>
        <span>
          <small>{t("creator.editor.glowEyebrow")}</small>
          <strong>{t("creator.editor.glow")}</strong>
        </span>
        <button
          type="button"
          className={glow.enabled ? "is-active" : ""}
          disabled={!editable}
          onClick={() => onChange({ ...glow, enabled: !glow.enabled })}
        >
          {glow.enabled ? t("creator.editor.enabled") : t("creator.editor.disabled")}
        </button>
      </header>

      {glow.enabled ? (
        <div className="creator-parameter-grid creator-parameter-grid--compact">
          <ColorField
            label={t("creator.editor.glowColor")}
            value={glow.color}
            disabled={!editable}
            onChange={(color) => onChange({ ...glow, color })}
          />
          <RangeField
            label={t("creator.editor.glowStrength")}
            value={glow.strength}
            min={1}
            max={100}
            disabled={!editable}
            onChange={(strength) => onChange({ ...glow, strength })}
          />
        </div>
      ) : null}
    </section>
  )
}

function nextGradientStop(colors: GradientStop[]): GradientStop {
  const used = new Set(colors.map((stop) => stop.position))
  const position = gradientPositions.find((candidate) => !used.has(candidate)) ?? 50
  const sourceColor = colors.at(-1)?.color ?? "#CBFF2D"

  return {
    color: sourceColor,
    position,
  }
}

export function CreatorPersonalizationControls({ form, editable, setForm }: Props) {
  const { t } = useTranslation()

  if (form.type === "NICK_STYLE") {
    const updateNickStyle = (next: Partial<CreatorEditorForm["nickStyle"]>) => {
      setForm((current) => ({
        ...current,
        nickStyle: {
          ...current.nickStyle,
          ...next,
        },
      }))
    }

    const updateStop = (index: number, stop: GradientStop) => {
      updateNickStyle({
        colors: form.nickStyle.colors.map((current, currentIndex) =>
          currentIndex === index ? stop : current,
        ),
      })
    }

    return (
      <div className="creator-visual-controls">
        <section className="creator-parameter-section">
          <header>
            <span>
              <small>{t("creator.editor.gradientEyebrow")}</small>
              <strong>{t("creator.editor.gradient")}</strong>
            </span>
            <button
              type="button"
              disabled={!editable || form.nickStyle.colors.length >= 4}
              onClick={() =>
                updateNickStyle({
                  colors: [...form.nickStyle.colors, nextGradientStop(form.nickStyle.colors)],
                })
              }
            >
              {t("creator.editor.addColor")}
            </button>
          </header>

          <div className="creator-gradient-stops">
            {form.nickStyle.colors.map((stop, index) => (
              <div className="creator-gradient-stop" key={`${index}-${stop.position}`}>
                <input
                  type="color"
                  value={stop.color}
                  disabled={!editable}
                  aria-label={t("creator.editor.gradientColor", { index: index + 1 })}
                  onChange={(event) =>
                    updateStop(index, {
                      ...stop,
                      color: event.target.value.toUpperCase(),
                    })
                  }
                />
                <RangeControl
                  min={0}
                  max={100}
                  value={stop.position}
                  disabled={!editable}
                  ariaLabel={t("creator.editor.gradientPosition", { index: index + 1 })}
                  onChange={(position) =>
                    updateStop(index, {
                      ...stop,
                      position,
                    })
                  }
                />
                <output>{stop.position}%</output>
                <button
                  type="button"
                  disabled={!editable || form.nickStyle.colors.length <= 1}
                  aria-label={t("creator.editor.removeColor", { index: index + 1 })}
                  onClick={() =>
                    updateNickStyle({
                      colors: form.nickStyle.colors.filter(
                        (_, currentIndex) => currentIndex !== index,
                      ),
                    })
                  }
                >
                  ×
                </button>
              </div>
            ))}
          </div>

          <RangeField
            label={t("creator.editor.gradientAngle")}
            value={form.nickStyle.angleDegrees}
            min={0}
            max={360}
            suffix="°"
            disabled={!editable}
            onChange={(angleDegrees) => updateNickStyle({ angleDegrees })}
          />
        </section>

        <GlowControls
          glow={form.nickStyle.glow}
          editable={editable}
          onChange={(glow) => updateNickStyle({ glow })}
        />
      </div>
    )
  }

  if (form.type === "PROFILE_THEME") {
    const updateTheme = (next: Partial<CreatorEditorForm["profileTheme"]>) => {
      setForm((current) => ({
        ...current,
        profileTheme: {
          ...current.profileTheme,
          ...next,
        },
      }))
    }

    return (
      <div className="creator-visual-controls">
        <section className="creator-parameter-section">
          <header>
            <span>
              <small>{t("creator.editor.paletteEyebrow")}</small>
              <strong>{t("creator.editor.themePalette")}</strong>
            </span>
          </header>

          <div className="creator-parameter-grid">
            <ColorField
              label={t("creator.editor.background")}
              value={form.profileTheme.background}
              disabled={!editable}
              onChange={(background) => updateTheme({ background })}
            />
            <ColorField
              label={t("creator.editor.backgroundSecondary")}
              value={form.profileTheme.backgroundSecondary}
              disabled={!editable}
              onChange={(backgroundSecondary) => updateTheme({ backgroundSecondary })}
            />
            <ColorField
              label={t("creator.editor.surface")}
              value={form.profileTheme.surface}
              disabled={!editable}
              onChange={(surface) => updateTheme({ surface })}
            />
            <ColorField
              label={t("creator.editor.surfaceSecondary")}
              value={form.profileTheme.surfaceSecondary}
              disabled={!editable}
              onChange={(surfaceSecondary) => updateTheme({ surfaceSecondary })}
            />
            <ColorField
              label={t("creator.editor.accent")}
              value={form.profileTheme.accent}
              disabled={!editable}
              onChange={(accent) => updateTheme({ accent })}
            />
            <ColorField
              label={t("creator.editor.textColor")}
              value={form.profileTheme.text}
              disabled={!editable}
              onChange={(text) => updateTheme({ text })}
            />
            <ColorField
              label={t("creator.editor.borderColor")}
              value={form.profileTheme.border}
              disabled={!editable}
              onChange={(border) => updateTheme({ border })}
            />
          </div>

          <div className="creator-parameter-grid creator-parameter-grid--compact">
            <RangeField
              label={t("creator.editor.gradientAngle")}
              value={form.profileTheme.gradientAngleDegrees}
              min={0}
              max={360}
              suffix="°"
              disabled={!editable}
              onChange={(gradientAngleDegrees) => updateTheme({ gradientAngleDegrees })}
            />
            <RangeField
              label={t("creator.editor.textureStrength")}
              value={form.profileTheme.textureStrength}
              min={0}
              max={100}
              disabled={!editable}
              onChange={(textureStrength) => updateTheme({ textureStrength })}
            />
          </div>
        </section>

        <GlowControls
          glow={form.profileTheme.glow}
          editable={editable}
          onChange={(glow) => updateTheme({ glow })}
        />
      </div>
    )
  }

  if (form.type === "AVATAR_FRAME") {
    const updateFrame = (next: Partial<CreatorEditorForm["avatarFrame"]>) => {
      setForm((current) => ({
        ...current,
        avatarFrame: {
          ...current.avatarFrame,
          ...next,
        },
      }))
    }

    return (
      <div className="creator-visual-controls">
        <section className="creator-parameter-section">
          <header>
            <span>
              <small>{t("creator.editor.frameEyebrow")}</small>
              <strong>{t("creator.editor.frameGeometry")}</strong>
            </span>
          </header>

          <div className="creator-segmented-control" aria-label={t("creator.editor.frameStyle")}>
            {frameStyles.map((style) => (
              <button
                type="button"
                key={style}
                className={form.avatarFrame.style === style ? "is-active" : ""}
                disabled={!editable}
                onClick={() => updateFrame({ style })}
              >
                {t(`creator.editor.frameStyles.${style}`)}
              </button>
            ))}
          </div>

          <div className="creator-parameter-grid creator-parameter-grid--compact">
            <ColorField
              label={t("creator.editor.primaryBorder")}
              value={form.avatarFrame.borderColor}
              disabled={!editable}
              onChange={(borderColor) => updateFrame({ borderColor })}
            />
            <ColorField
              label={t("creator.editor.secondaryBorder")}
              value={form.avatarFrame.secondaryBorderColor}
              disabled={!editable}
              onChange={(secondaryBorderColor) => updateFrame({ secondaryBorderColor })}
            />
          </div>

          <div className="creator-parameter-grid creator-parameter-grid--triple">
            <RangeField
              label={t("creator.editor.borderWidth")}
              value={form.avatarFrame.borderWidthPx}
              min={1}
              max={8}
              suffix="px"
              disabled={!editable}
              onChange={(borderWidthPx) => updateFrame({ borderWidthPx })}
            />
            <RangeField
              label={t("creator.editor.frameGap")}
              value={form.avatarFrame.gapPx}
              min={0}
              max={8}
              suffix="px"
              disabled={!editable}
              onChange={(gapPx) => updateFrame({ gapPx })}
            />
            <RangeField
              label={t("creator.editor.radius")}
              value={form.avatarFrame.radiusPx}
              min={0}
              max={32}
              suffix="px"
              disabled={!editable}
              onChange={(radiusPx) => updateFrame({ radiusPx })}
            />
          </div>
        </section>

        <GlowControls
          glow={form.avatarFrame.glow}
          editable={editable}
          onChange={(glow) => updateFrame({ glow })}
        />
      </div>
    )
  }

  if (form.type === "PROFILE_EFFECT") {
    const updateEffect = (next: Partial<CreatorEditorForm["profileEffect"]>) => {
      setForm((current) => ({
        ...current,
        profileEffect: {
          ...current.profileEffect,
          ...next,
        },
      }))
    }

    return (
      <div className="creator-visual-controls">
        <section className="creator-parameter-section">
          <header>
            <span>
              <small>{t("creator.editor.effectEyebrow")}</small>
              <strong>{t("creator.editor.effectBehavior")}</strong>
            </span>
          </header>

          <div className="creator-segmented-control creator-segmented-control--four">
            {effectTypes.map((effect) => (
              <button
                type="button"
                key={effect}
                className={form.profileEffect.effect === effect ? "is-active" : ""}
                disabled={!editable}
                onClick={() => updateEffect({ effect })}
              >
                {t(`creator.editor.effects.${effect}`)}
              </button>
            ))}
          </div>

          <div className="creator-parameter-grid creator-parameter-grid--compact">
            <ColorField
              label={t("creator.editor.primary")}
              value={form.profileEffect.primaryColor}
              disabled={!editable}
              onChange={(primaryColor) => updateEffect({ primaryColor })}
            />
            <ColorField
              label={t("creator.editor.secondary")}
              value={form.profileEffect.secondaryColor}
              disabled={!editable}
              onChange={(secondaryColor) => updateEffect({ secondaryColor })}
            />
          </div>

          <div className="creator-parameter-grid creator-parameter-grid--compact">
            <RangeField
              label={t("creator.editor.intensity")}
              value={form.profileEffect.intensity}
              min={1}
              max={100}
              disabled={!editable}
              onChange={(intensity) => updateEffect({ intensity })}
            />
            <RangeField
              label={t("creator.editor.speed")}
              value={form.profileEffect.speed}
              min={1}
              max={100}
              disabled={!editable}
              onChange={(speed) => updateEffect({ speed })}
            />
            <RangeField
              label={t("creator.editor.density")}
              value={form.profileEffect.density}
              min={1}
              max={100}
              disabled={!editable}
              onChange={(density) => updateEffect({ density })}
            />
            <RangeField
              label={t("creator.editor.scale")}
              value={form.profileEffect.scale}
              min={1}
              max={100}
              disabled={!editable}
              onChange={(scale) => updateEffect({ scale })}
            />
          </div>
        </section>
      </div>
    )
  }

  return null
}
