import type { Dispatch, SetStateAction } from "react"
import { useTranslation } from "react-i18next"
import type { CreatorEditorForm } from "./creatorEditorModel"
import "./creator-design-presets.css"

type Props = {
  form: CreatorEditorForm
  editable: boolean
  setForm: Dispatch<SetStateAction<CreatorEditorForm>>
}

type PresetId = "MNEMOS" | "VOID" | "EMBER" | "FROST"

type Preset = {
  id: PresetId
  accent: string
  secondary: string
}

const presets: Preset[] = [
  { id: "MNEMOS", accent: "#CBFF2D", secondary: "#8FAE32" },
  { id: "VOID", accent: "#D59AD8", secondary: "#765CFF" },
  { id: "EMBER", accent: "#FFB34F", secondary: "#FF6B55" },
  { id: "FROST", accent: "#8FE9FF", secondary: "#D9F7FF" },
]

function applyPreset(form: CreatorEditorForm, preset: PresetId): CreatorEditorForm {
  if (form.type === "NICK_STYLE") {
    const next = {
      MNEMOS: {
        colors: [
          { color: "#CBFF2D", position: 0 },
          { color: "#EAF5C9", position: 100 },
        ],
        angleDegrees: 120,
        glow: { enabled: true, color: "#CBFF2D", strength: 28 },
      },
      VOID: {
        colors: [
          { color: "#D59AD8", position: 0 },
          { color: "#765CFF", position: 52 },
          { color: "#9FFFE0", position: 100 },
        ],
        angleDegrees: 135,
        glow: { enabled: true, color: "#A875FF", strength: 34 },
      },
      EMBER: {
        colors: [
          { color: "#FFD17A", position: 0 },
          { color: "#FF8A4F", position: 55 },
          { color: "#FF5D67", position: 100 },
        ],
        angleDegrees: 105,
        glow: { enabled: true, color: "#FF8A4F", strength: 30 },
      },
      FROST: {
        colors: [
          { color: "#F5FFFF", position: 0 },
          { color: "#8FE9FF", position: 58 },
          { color: "#A5B7FF", position: 100 },
        ],
        angleDegrees: 145,
        glow: { enabled: true, color: "#8FE9FF", strength: 26 },
      },
    }[preset]

    return {
      ...form,
      nickStyle: next,
    }
  }

  if (form.type === "PROFILE_THEME") {
    const next = {
      MNEMOS: {
        background: "#090D07",
        backgroundSecondary: "#12180B",
        surface: "#11170D",
        surfaceSecondary: "#1A2113",
        accent: "#CBFF2D",
        text: "#EDF3E5",
        border: "#34431E",
        gradientAngleDegrees: 128,
        textureStrength: 18,
        glow: { enabled: true, color: "#CBFF2D", strength: 20 },
      },
      VOID: {
        background: "#0B0911",
        backgroundSecondary: "#171124",
        surface: "#15101D",
        surfaceSecondary: "#21182C",
        accent: "#D59AD8",
        text: "#F2EAF4",
        border: "#4A3450",
        gradientAngleDegrees: 142,
        textureStrength: 12,
        glow: { enabled: true, color: "#A875FF", strength: 24 },
      },
      EMBER: {
        background: "#100B07",
        backgroundSecondary: "#24120C",
        surface: "#1B100B",
        surfaceSecondary: "#2B1710",
        accent: "#FFB34F",
        text: "#FFF0DB",
        border: "#5A3420",
        gradientAngleDegrees: 116,
        textureStrength: 20,
        glow: { enabled: true, color: "#FF8A4F", strength: 22 },
      },
      FROST: {
        background: "#071014",
        backgroundSecondary: "#0D1B23",
        surface: "#0D171D",
        surfaceSecondary: "#15252E",
        accent: "#8FE9FF",
        text: "#ECFBFF",
        border: "#28505E",
        gradientAngleDegrees: 150,
        textureStrength: 10,
        glow: { enabled: true, color: "#8FE9FF", strength: 20 },
      },
    }[preset]

    return {
      ...form,
      profileTheme: {
        ...form.profileTheme,
        ...next,
      },
    }
  }

  if (form.type === "AVATAR_FRAME") {
    const next = {
      MNEMOS: {
        borderColor: "#CBFF2D",
        secondaryBorderColor: "#6F872E",
        borderWidthPx: 2,
        gapPx: 3,
        radiusPx: 14,
        style: "DOUBLE" as const,
        glow: { enabled: true, color: "#CBFF2D", strength: 28 },
      },
      VOID: {
        borderColor: "#D59AD8",
        secondaryBorderColor: "#765CFF",
        borderWidthPx: 2,
        gapPx: 4,
        radiusPx: 18,
        style: "SEGMENTED" as const,
        glow: { enabled: true, color: "#A875FF", strength: 34 },
      },
      EMBER: {
        borderColor: "#FFB34F",
        secondaryBorderColor: "#FF5D67",
        borderWidthPx: 3,
        gapPx: 2,
        radiusPx: 12,
        style: "DOUBLE" as const,
        glow: { enabled: true, color: "#FF8A4F", strength: 30 },
      },
      FROST: {
        borderColor: "#D9F7FF",
        secondaryBorderColor: "#8FE9FF",
        borderWidthPx: 2,
        gapPx: 3,
        radiusPx: 20,
        style: "SOLID" as const,
        glow: { enabled: true, color: "#8FE9FF", strength: 26 },
      },
    }[preset]

    return {
      ...form,
      avatarFrame: {
        ...form.avatarFrame,
        ...next,
      },
    }
  }

  if (form.type === "PROFILE_EFFECT") {
    const next = {
      MNEMOS: {
        effect: "PULSE" as const,
        intensity: 42,
        speed: 38,
        density: 44,
        scale: 58,
        primaryColor: "#CBFF2D",
        secondaryColor: "#7C9B26",
      },
      VOID: {
        effect: "AURORA" as const,
        intensity: 58,
        speed: 34,
        density: 48,
        scale: 64,
        primaryColor: "#D59AD8",
        secondaryColor: "#765CFF",
      },
      EMBER: {
        effect: "PARTICLES" as const,
        intensity: 52,
        speed: 46,
        density: 62,
        scale: 56,
        primaryColor: "#FFB34F",
        secondaryColor: "#FF5D67",
      },
      FROST: {
        effect: "SHIMMER" as const,
        intensity: 44,
        speed: 30,
        density: 36,
        scale: 60,
        primaryColor: "#8FE9FF",
        secondaryColor: "#D9F7FF",
      },
    }[preset]

    return {
      ...form,
      profileEffect: {
        ...form.profileEffect,
        ...next,
      },
    }
  }

  return form
}

export function CreatorDesignPresets({ form, editable, setForm }: Props) {
  const { t } = useTranslation()

  if (form.type === "NOTIFICATION_PACK") {
    return null
  }

  return (
    <section className="creator-design-presets">
      <header>
        <span>
          <small>{t("creator.editor.presetsEyebrow")}</small>
          <strong>{t("creator.editor.presetsTitle")}</strong>
        </span>
        <em>{t("creator.editor.presetsHint")}</em>
      </header>

      <div className="creator-design-presets__grid">
        {presets.map((preset) => (
          <button
            type="button"
            key={preset.id}
            disabled={!editable}
            onClick={() => setForm((current) => applyPreset(current, preset.id))}
          >
            <span
              className="creator-design-presets__swatch"
              style={{
                background: `linear-gradient(135deg, ${preset.accent}, ${preset.secondary})`,
              }}
            />
            <span>
              <strong>{t(`creator.editor.presets.${preset.id}.title`)}</strong>
              <small>{t(`creator.editor.presets.${preset.id}.description`)}</small>
            </span>
          </button>
        ))}
      </div>
    </section>
  )
}
