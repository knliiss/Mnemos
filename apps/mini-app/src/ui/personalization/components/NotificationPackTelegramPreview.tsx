import type { TFunction } from "i18next"
import { useTranslation } from "react-i18next"
import type { NotificationPackContent, NotificationTemplateKey } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import "./notification-pack-telegram-preview.css"

type TelegramSample = {
  key: NotificationTemplateKey
  values: Record<string, string>
  time: string
}

export function NotificationPackTelegramPreview({ content }: { content: NotificationPackContent }) {
  const { t } = useTranslation()
  const samples = createTelegramSamples(t)
  const aggregateText = [
    content.mixedAggregateTitle,
    ...samples.slice(0, 3).map((sample) => {
      const template = content.templates[sample.key]

      return withEmoji(template.emoji, renderTemplate(template.compact, sample.values))
    }),
  ].join("\n\n")

  return (
    <section className="notification-pack-telegram-preview">
      <header className="notification-pack-telegram-preview__header">
        <span className="notification-pack-telegram-preview__mark">
          <Glyph name="send" size={16} />
        </span>
        <span>
          <small>TELEGRAM</small>
          <strong>Mnemos</strong>
        </span>
        <em>{t("personalization.preview.templateCount", { count: samples.length })}</em>
      </header>

      <div className="notification-pack-telegram-preview__chat">
        {samples.map((sample) => {
          const template = content.templates[sample.key]
          const message = [
            withEmoji(template.emoji, renderTemplate(template.soloTitle, sample.values)),
            renderTemplate(template.soloBody, sample.values),
          ].join("\n")

          return <TelegramBubble key={sample.key} text={message} time={sample.time} />
        })}

        <div className="notification-pack-telegram-preview__divider">
          <span>{t("personalization.preview.mixedAggregation")}</span>
        </div>

        <TelegramBubble text={aggregateText} time="00:42" />
      </div>
    </section>
  )
}

function TelegramBubble({ text, time }: { text: string; time: string }) {
  return (
    <article className="notification-pack-telegram-preview__message">
      <div className="notification-pack-telegram-preview__bubble">
        {text.split("\n").map((line, index) => (
          <span key={`${index}-${line}`}>{line || "\u00a0"}</span>
        ))}
        <time>{time}</time>
      </div>
    </article>
  )
}

function createTelegramSamples(t: TFunction): TelegramSample[] {
  return [
    {
      key: "ITEM_DROP",
      values: {
        player: "north",
        item: t("personalization.preview.samples.item"),
        rarity: t("personalization.preview.samples.mythical"),
        itemType: t("personalization.preview.samples.sword"),
      },
      time: "00:37",
    },
    {
      key: "BOOSTER",
      values: {
        player: "vex",
        booster: t("personalization.preview.samples.luck"),
      },
      time: "00:38",
    },
    {
      key: "DUNGEON",
      values: {
        dungeon: t("personalization.preview.samples.expert"),
      },
      time: "00:39",
    },
    {
      key: "RAID",
      values: {
        locations: "2 · 4 · 7",
      },
      time: "00:40",
    },
    {
      key: "GLOBAL",
      values: {
        event: t("personalization.preview.samples.eclipse"),
      },
      time: "00:41",
    },
  ]
}

function withEmoji(emoji: string | null, value: string): string {
  const normalizedEmoji = emoji?.trim()

  return normalizedEmoji ? `${normalizedEmoji} ${value}` : value
}

function renderTemplate(template: string, values: Record<string, string>): string {
  return template.replace(
    /\{([a-zA-Z][a-zA-Z0-9_]*)}/g,
    (_, key: string) => values[key] ?? `{${key}}`,
  )
}
