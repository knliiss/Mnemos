import type { TFunction } from "i18next"
import type { CSSProperties, ReactElement } from "react"
import { useTranslation } from "react-i18next"
import { BoosterArt, DungeonArt, GlobalEventArt, ItemTypeArt, RaidArt } from "../events/EventArt"
import type {
  AvatarFrameContent,
  NickStyleContent,
  NotificationPackContent,
  NotificationTemplateKey,
  Personalization,
  ProfileEffectContent,
  ProfileThemeContent,
} from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import "./personalization-preview.css"

type PreviewProps = {
  item: Personalization
  small?: boolean
  detail?: boolean
}

type TemplateSample = {
  key: NotificationTemplateKey
  label: string
  values: Record<string, string>
  art: ReactElement
}

export function PersonalizationVisual({ item, small = false, detail = false }: PreviewProps) {
  const content = item.content
  const className =
    `real-preview real-preview--${item.type.toLowerCase()} ${small ? "is-small" : ""} ${detail ? "is-detail" : ""}`.trim()

  if (!content) {
    return (
      <div className={className}>
        <Glyph name="spark" size={small ? 18 : 28} />
      </div>
    )
  }

  if (content.kind === "NOTIFICATION_PACK") {
    return (
      <NotificationPackPreview
        content={content}
        className={className}
        small={small}
        detail={detail}
      />
    )
  }

  if (content.kind === "NICK_STYLE") {
    return <NickPreview content={content} className={className} />
  }

  if (content.kind === "PROFILE_THEME") {
    return <ThemePreview content={content} className={className} />
  }

  if (content.kind === "AVATAR_FRAME") {
    return <FramePreview content={content} className={className} />
  }

  return <EffectPreview content={content} className={className} />
}

export function PersonalizationDetails({ item }: { item: Personalization }) {
  const { t } = useTranslation()
  const content = item.content

  if (!content) {
    return (
      <section className="visual-preview-note">
        <Glyph name="spark" size={17} />
        <span>
          <strong>{t("personalization.preview.missingTitle")}</strong>
          <small>{t("personalization.preview.missingCopy")}</small>
        </span>
      </section>
    )
  }

  if (content.kind === "NOTIFICATION_PACK") {
    return <NotificationPackShowcase content={content} />
  }

  if (content.kind === "NICK_STYLE") {
    return (
      <section className="personalization-spec-grid">
        <Spec label={t("personalization.preview.angle")} value={`${content.angleDegrees}°`} />
        <Spec label={t("personalization.preview.colors")} value={`${content.colors.length}`} />
        <Spec
          label={t("personalization.preview.glow")}
          value={content.glow ? `${content.glow.strength}%` : t("personalization.preview.none")}
        />
      </section>
    )
  }

  if (content.kind === "PROFILE_THEME") {
    return (
      <section className="personalization-spec-grid">
        <Spec label={t("personalization.preview.accent")} value={content.accent} />
        <Spec label={t("personalization.preview.surface")} value={content.surface} />
        <Spec label={t("personalization.preview.border")} value={content.border} />
      </section>
    )
  }

  if (content.kind === "AVATAR_FRAME") {
    return (
      <section className="personalization-spec-grid">
        <Spec label={t("personalization.preview.thickness")} value={`${content.borderWidthPx}px`} />
        <Spec label={t("personalization.preview.radius")} value={`${content.radiusPx}px`} />
        <Spec label={t("personalization.preview.color")} value={content.borderColor} />
      </section>
    )
  }

  return (
    <section className="personalization-spec-grid">
      <Spec label={t("personalization.preview.effect")} value={content.effect} />
      <Spec label={t("personalization.preview.intensity")} value={`${content.intensity}%`} />
      <Spec label={t("personalization.preview.primary")} value={content.primaryColor} />
    </section>
  )
}

function NotificationPackShowcase({ content }: { content: NotificationPackContent }) {
  const { t } = useTranslation()
  const templateSamples = createTemplateSamples(t)

  return (
    <section className="notification-pack-showcase">
      <header className="notification-pack-showcase__header">
        <span className="notification-pack-showcase__mark">
          <Glyph name="bell" size={18} />
        </span>
        <span>
          <small>{t("personalization.preview.fullPack")}</small>
          <strong>{content.mixedAggregateTitle}</strong>
        </span>
        <em>{t("personalization.preview.templateCount", { count: templateSamples.length })}</em>
      </header>

      <div className="notification-pack-aggregate">
        <span className="notification-pack-aggregate__stack" aria-hidden="true">
          <i />
          <i />
          <i />
        </span>
        <span>
          <small>{t("personalization.preview.mixedAggregation")}</small>
          <strong>{content.mixedAggregateTitle}</strong>
          <em>{t("personalization.preview.mixedAggregationCopy")}</em>
        </span>
      </div>

      <div className="notification-pack-template-list">
        {templateSamples.map((sample) => {
          const template = content.templates[sample.key]

          return (
            <article className="notification-pack-template" key={sample.key}>
              <span className="notification-pack-template__art">{sample.art}</span>
              <span className="notification-pack-template__copy">
                <span className="notification-pack-template__eyebrow">
                  <b>{sample.label}</b>
                  <i>{template.emoji ?? t("personalization.preview.noEmoji")}</i>
                </span>
                <strong>{renderTemplate(template.soloTitle, sample.values)}</strong>
                <p>{renderTemplate(template.soloBody, sample.values)}</p>
                <span className="notification-pack-template__variants">
                  <em>
                    <small>{t("personalization.preview.compact")}</small>
                    {renderTemplate(template.compact, sample.values)}
                  </em>
                  <em>
                    <small>{t("personalization.preview.aggregate")}</small>
                    {renderTemplate(template.aggregateTitle, sample.values)}
                  </em>
                </span>
              </span>
            </article>
          )
        })}
      </div>
    </section>
  )
}

function NotificationPackPreview({
  content,
  className,
  small,
  detail,
}: {
  content: NotificationPackContent
  className: string
  small: boolean
  detail: boolean
}) {
  const { t } = useTranslation()
  const template = content.templates.ITEM_DROP
  const values = {
    player: "north",
    item: t("personalization.preview.samples.item"),
    rarity: t("personalization.preview.samples.mythical"),
    itemType: t("personalization.preview.samples.sword"),
  }
  const body = renderTemplate(template.soloBody, values)

  return (
    <div
      className={`${className} notification-real-preview ${detail ? "notification-real-preview--detail" : ""}`.trim()}
    >
      <span className="notification-real-preview__event">
        <ItemTypeArt itemType="SWORD" size={small ? 26 : detail ? 46 : 34} />
      </span>
      <span className="notification-real-preview__copy">
        <small>{t("personalization.preview.itemDrop")}</small>
        <strong>{renderTemplate(template.soloTitle, values)}</strong>
        {!small ? <p>{body}</p> : null}
        {detail ? <em>{renderTemplate(template.compact, values)}</em> : null}
      </span>
      {!small ? (
        <span className="notification-real-preview__emoji">{template.emoji ?? "•"}</span>
      ) : null}
    </div>
  )
}

function NickPreview({ content, className }: { content: NickStyleContent; className: string }) {
  const { t } = useTranslation()
  const stops = content.colors.map((stop) => `${stop.color} ${stop.position}%`).join(", ")
  const glow = content.glow
    ? `0 0 ${Math.max(5, content.glow.strength / 3)}px ${content.glow.color}`
    : "none"

  return (
    <div className={`${className} nick-real-preview`}>
      <small>{t("personalization.preview.nickStyle")}</small>
      <strong
        style={{
          backgroundImage: `linear-gradient(${content.angleDegrees}deg, ${stops})`,
          textShadow: glow,
        }}
      >
        {t("personalization.preview.samples.nickname")}
      </strong>
      <em>{t("personalization.preview.nickExample")}</em>
    </div>
  )
}

function ThemePreview({ content, className }: { content: ProfileThemeContent; className: string }) {
  const { t } = useTranslation()
  const nickname = t("personalization.preview.samples.nickname")

  return (
    <div
      className={`${className} theme-real-preview`}
      style={{
        background: content.background,
        borderColor: content.border,
        color: content.text,
      }}
    >
      <span
        className="theme-real-preview__avatar"
        style={{
          background: content.surface,
          borderColor: content.accent,
        }}
      >
        {nickname.slice(0, 1).toUpperCase()}
      </span>
      <span style={{ background: content.surface }}>
        <b style={{ color: content.text }}>{nickname}</b>
        <i style={{ background: content.accent }} />
      </span>
    </div>
  )
}

function FramePreview({ content, className }: { content: AvatarFrameContent; className: string }) {
  const { t } = useTranslation()
  const nickname = t("personalization.preview.samples.nickname")

  return (
    <div className={`${className} frame-real-preview`}>
      <span
        style={{
          borderColor: content.borderColor,
          borderWidth: content.borderWidthPx,
          borderRadius: content.radiusPx,
          boxShadow: content.glow
            ? `0 0 ${Math.max(6, content.glow.strength / 2)}px color-mix(in srgb, ${content.glow.color} 55%, transparent)`
            : "none",
        }}
      >
        {nickname.slice(0, 1).toUpperCase()}
      </span>
      <small>{t("personalization.preview.avatarFrame")}</small>
    </div>
  )
}

function EffectPreview({
  content,
  className,
}: {
  content: ProfileEffectContent
  className: string
}) {
  const { t } = useTranslation()
  const nickname = t("personalization.preview.samples.nickname")

  return (
    <div
      className={`${className} effect-real-preview effect-real-preview--${content.effect.toLowerCase()}`}
      style={
        {
          "--effect-primary": content.primaryColor,
          "--effect-secondary": content.secondaryColor ?? content.primaryColor,
          "--effect-intensity": `${content.intensity}%`,
        } as CSSProperties
      }
    >
      <span>{nickname.slice(0, 1).toUpperCase()}</span>
      <small>
        {t(`personalization.preview.effects.${content.effect}`)} · {content.intensity}%
      </small>
    </div>
  )
}

function Spec({ label, value }: { label: string; value: string }) {
  return (
    <article>
      <small>{label}</small>
      <strong>{value}</strong>
    </article>
  )
}

function createTemplateSamples(t: TFunction): TemplateSample[] {
  return [
    {
      key: "ITEM_DROP",
      label: t("personalization.preview.samples.ITEM_DROP"),
      values: {
        player: "north",
        item: t("personalization.preview.samples.item"),
        rarity: t("personalization.preview.samples.mythical"),
        itemType: t("personalization.preview.samples.sword"),
      },
      art: <ItemTypeArt itemType="SWORD" size={38} />,
    },
    {
      key: "BOOSTER",
      label: t("personalization.preview.samples.BOOSTER"),
      values: {
        player: "vex",
        booster: t("personalization.preview.samples.luck"),
      },
      art: <BoosterArt boosterType="LUCK" size={38} />,
    },
    {
      key: "DUNGEON",
      label: t("personalization.preview.samples.DUNGEON"),
      values: {
        dungeon: t("personalization.preview.samples.expert"),
      },
      art: <DungeonArt size={38} />,
    },
    {
      key: "RAID",
      label: t("personalization.preview.samples.RAID"),
      values: {
        locations: "2 · 4 · 7",
      },
      art: <RaidArt size={38} />,
    },
    {
      key: "GLOBAL",
      label: t("personalization.preview.samples.GLOBAL"),
      values: {
        event: t("personalization.preview.samples.eclipse"),
      },
      art: <GlobalEventArt eventType="ECLIPSE" size={38} />,
    },
  ]
}

function renderTemplate(template: string, values: Record<string, string>) {
  return template.replace(
    /\{([a-zA-Z][a-zA-Z0-9_]*)}/g,
    (_, key: string) => values[key] ?? `{${key}}`,
  )
}
