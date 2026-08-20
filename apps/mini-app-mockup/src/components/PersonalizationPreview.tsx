import type { CSSProperties, ReactElement } from "react"
import {
  BoosterArt,
  DungeonArt,
  GlobalEventArt,
  ItemTypeArt,
  RaidArt,
} from "./EventArt"
import { personalizationContentById } from "../personalizationPreviewData"
import type {
  AvatarFrameContent,
  NickStyleContent,
  NotificationPackContent,
  NotificationTemplateKey,
  Personalization,
  ProfileEffectContent,
  ProfileThemeContent,
} from "../model"
import { Glyph } from "../shared/MnemosIcon"

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

const templateSamples: TemplateSample[] = [
  {
    key: "ITEM_DROP",
    label: "Дроп",
    values: {
      player: "north",
      item: "Eternal Blade",
      rarity: "Mythical",
      itemType: "Sword",
    },
    art: <ItemTypeArt itemType="SWORD" size={38} />,
  },
  {
    key: "BOOSTER",
    label: "Бустер",
    values: {
      player: "vex",
      booster: "Luck",
    },
    art: <BoosterArt boosterType="LUCK" size={38} />,
  },
  {
    key: "DUNGEON",
    label: "Данж",
    values: {
      dungeon: "Експертний",
    },
    art: <DungeonArt size={38} />,
  },
  {
    key: "RAID",
    label: "Рейд",
    values: {
      locations: "2 · 4 · 7",
    },
    art: <RaidArt size={38} />,
  },
  {
    key: "GLOBAL",
    label: "Глобальна",
    values: {
      event: "Затемнення",
    },
    art: <GlobalEventArt eventType="ECLIPSE" size={38} />,
  },
]

export function hydratePersonalization(item: Personalization): Personalization {
  return {
    ...item,
    content: item.content ?? personalizationContentById[item.id],
  }
}

export function PersonalizationVisual({ item, small = false, detail = false }: PreviewProps) {
  const content = item.content ?? personalizationContentById[item.id]
  const className = `real-preview real-preview--${item.type.toLowerCase()} ${small ? "is-small" : ""} ${detail ? "is-detail" : ""}`.trim()

  if (!content) {
    return (
      <div className={className}>
        <Glyph name="spark" size={small ? 18 : 28} />
      </div>
    )
  }

  if (content.kind === "NOTIFICATION_PACK") {
    return <NotificationPackPreview content={content} className={className} small={small} detail={detail} />
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
  const content = item.content ?? personalizationContentById[item.id]

  if (!content) {
    return (
      <section className="visual-preview-note">
        <Glyph name="spark" size={17} />
        <span>
          <strong>Прев'ю ще не зібране</strong>
          <small>Контент персоналізації відсутній у мокових даних.</small>
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
        <Spec label="Кут" value={`${content.angleDegrees}°`} />
        <Spec label="Кольори" value={`${content.colors.length}`} />
        <Spec label="Glow" value={content.glow ? `${content.glow.strength}%` : "Немає"} />
      </section>
    )
  }

  if (content.kind === "PROFILE_THEME") {
    return (
      <section className="personalization-spec-grid">
        <Spec label="Accent" value={content.accent} />
        <Spec label="Surface" value={content.surface} />
        <Spec label="Border" value={content.border} />
      </section>
    )
  }

  if (content.kind === "AVATAR_FRAME") {
    return (
      <section className="personalization-spec-grid">
        <Spec label="Товщина" value={`${content.borderWidthPx}px`} />
        <Spec label="Radius" value={`${content.radiusPx}px`} />
        <Spec label="Колір" value={content.borderColor} />
      </section>
    )
  }

  return (
    <section className="personalization-spec-grid">
      <Spec label="Ефект" value={content.effect} />
      <Spec label="Інтенсивність" value={`${content.intensity}%`} />
      <Spec label="Основний" value={content.primaryColor} />
    </section>
  )
}

export function NotificationPackShowcase({ content }: { content: NotificationPackContent }) {
  return (
    <section className="notification-pack-showcase">
      <header className="notification-pack-showcase__header">
        <span className="notification-pack-showcase__mark">
          <Glyph name="bell" size={18} />
        </span>
        <span>
          <small>Повний preview pack</small>
          <strong>{content.mixedAggregateTitle}</strong>
        </span>
        <em>{templateSamples.length} шаблонів</em>
      </header>

      <div className="notification-pack-aggregate">
        <span className="notification-pack-aggregate__stack" aria-hidden="true">
          <i />
          <i />
          <i />
        </span>
        <span>
          <small>Змішана агрегація</small>
          <strong>{content.mixedAggregateTitle}</strong>
          <em>Коли одночасно приходять різні типи подій</em>
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
                  <i>{template.emoji ?? "без emoji"}</i>
                </span>
                <strong>{renderTemplate(template.soloTitle, sample.values)}</strong>
                <p>{renderTemplate(template.soloBody, sample.values)}</p>
                <span className="notification-pack-template__variants">
                  <em>
                    <small>compact</small>
                    {renderTemplate(template.compact, sample.values)}
                  </em>
                  <em>
                    <small>aggregate</small>
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
  const template = content.templates.ITEM_DROP
  const body = renderTemplate(template.soloBody, {
    player: "north",
    item: "Eternal Blade",
    rarity: "Mythical",
    itemType: "Sword",
  })

  return (
    <div className={`${className} notification-real-preview ${detail ? "notification-real-preview--detail" : ""}`.trim()}>
      <span className="notification-real-preview__event">
        <ItemTypeArt itemType="SWORD" size={small ? 26 : detail ? 46 : 34} />
      </span>
      <span className="notification-real-preview__copy">
        <small>ITEM DROP</small>
        <strong>{template.soloTitle}</strong>
        {!small ? <p>{body}</p> : null}
        {detail ? <em>{renderTemplate(template.compact, { player: "north", item: "Eternal Blade" })}</em> : null}
      </span>
      {!small ? <span className="notification-real-preview__emoji">{template.emoji ?? "•"}</span> : null}
    </div>
  )
}

function NickPreview({ content, className }: { content: NickStyleContent; className: string }) {
  const stops = content.colors.map((stop) => `${stop.color} ${stop.position}%`).join(", ")
  const glow = content.glow ? `0 0 ${Math.max(5, content.glow.strength / 3)}px ${content.glow.color}` : "none"

  return (
    <div className={`${className} nick-real-preview`}>
      <small>NICK STYLE</small>
      <strong
        style={{
          backgroundImage: `linear-gradient(${content.angleDegrees}deg, ${stops})`,
          textShadow: glow,
        }}
      >
        knalis
      </strong>
      <em>Приклад ніка</em>
    </div>
  )
}

function ThemePreview({ content, className }: { content: ProfileThemeContent; className: string }) {
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
        K
      </span>
      <span style={{ background: content.surface }}>
        <b style={{ color: content.text }}>knalis</b>
        <i style={{ background: content.accent }} />
      </span>
    </div>
  )
}

function FramePreview({ content, className }: { content: AvatarFrameContent; className: string }) {
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
        K
      </span>
      <small>Рамка аватара</small>
    </div>
  )
}

function EffectPreview({ content, className }: { content: ProfileEffectContent; className: string }) {
  return (
    <div
      className={`${className} effect-real-preview effect-real-preview--${content.effect.toLowerCase()}`}
      style={{
        "--effect-primary": content.primaryColor,
        "--effect-secondary": content.secondaryColor ?? content.primaryColor,
        "--effect-intensity": `${content.intensity}%`,
      } as CSSProperties}
    >
      <span>K</span>
      <small>{content.effect} · {content.intensity}%</small>
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

export function renderTemplate(template: string, values: Record<string, string>) {
  return template.replace(/\{([a-zA-Z][a-zA-Z0-9_]*)}/g, (_, key: string) => values[key] ?? `{${key}}`)
}
