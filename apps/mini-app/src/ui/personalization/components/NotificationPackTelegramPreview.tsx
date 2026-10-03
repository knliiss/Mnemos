import { useState } from "react"
import { useTranslation } from "react-i18next"
import type {
  NotificationPackContent,
  NotificationTemplateKey,
  NotificationTemplateVariant,
} from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import {
  notificationTemplateKeys,
  renderNotificationTemplate,
  selectedNotificationTemplate,
  valuesForVariant,
  variantLabel,
  withNotificationEmoji,
} from "./notificationPackPreview"
import "./notification-pack-telegram-preview.css"

type PreviewMode = "SOLO" | "AGGREGATE"

export function NotificationPackTelegramPreview({ content }: { content: NotificationPackContent }) {
  const { t } = useTranslation()
  const [selectedType, setSelectedType] = useState<NotificationTemplateKey>("ITEM_DROP")
  const [selectedVariantId, setSelectedVariantId] = useState<string | null>(null)
  const [mode, setMode] = useState<PreviewMode>("SOLO")
  const availableTypes = notificationTemplateKeys.filter((type) => Boolean(content.templates[type]))
  const effectiveSelectedType = content.templates[selectedType] ? selectedType : availableTypes[0]
  const template = effectiveSelectedType ? content.templates[effectiveSelectedType] : undefined

  if (!template || !effectiveSelectedType) {
    return null
  }

  const variants = template.variants ?? []
  const selectedVariant =
    selectedVariantId === null
      ? null
      : (variants.find((variant) => variant.id === selectedVariantId) ?? null)
  const message = selectedNotificationTemplate(template, selectedVariant?.id ?? null)
  const values = valuesForVariant(effectiveSelectedType, selectedVariant)
  const soloText = [
    withNotificationEmoji(
      message.emoji,
      renderNotificationTemplate(content, message.soloTitle, values),
    ),
    renderNotificationTemplate(content, message.soloBody, values),
  ].join("\n")
  const aggregateText = [
    withNotificationEmoji(message.emoji, message.aggregateTitle),
    withNotificationEmoji(
      message.emoji,
      renderNotificationTemplate(content, message.compact, values),
    ),
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
        <em>{t(`creator.editor.notificationTypes.${effectiveSelectedType}`)}</em>
      </header>

      <div className="notification-pack-telegram-preview__controls">
        <nav aria-label={t("creator.editor.eventTemplate")}>
          {availableTypes.map((type) => (
            <button
              type="button"
              key={type}
              className={type === effectiveSelectedType ? "is-active" : ""}
              onClick={() => {
                setSelectedType(type)
                setSelectedVariantId(null)
              }}
            >
              {t(`creator.editor.notificationTypes.${type}`)}
            </button>
          ))}
        </nav>

        <div className="notification-pack-telegram-preview__mode">
          <button
            type="button"
            className={mode === "SOLO" ? "is-active" : ""}
            onClick={() => setMode("SOLO")}
          >
            {t("creator.editor.notificationModes.SOLO")}
          </button>
          <button
            type="button"
            className={mode === "AGGREGATE" ? "is-active" : ""}
            onClick={() => setMode("AGGREGATE")}
          >
            {t("creator.editor.notificationModes.AGGREGATE")}
          </button>
        </div>

        {variants.length ? (
          <nav
            className="notification-pack-telegram-preview__variants"
            aria-label={t("creator.editor.variantsTitle")}
          >
            <button
              type="button"
              className={selectedVariant === null ? "is-active" : ""}
              onClick={() => setSelectedVariantId(null)}
            >
              {t("creator.editor.defaultVariant")}
            </button>

            {variants.map((variant) => (
              <button
                type="button"
                key={variant.id}
                className={selectedVariant?.id === variant.id ? "is-active" : ""}
                onClick={() => setSelectedVariantId(variant.id)}
              >
                {variantLabel(variant as NotificationTemplateVariant)}
              </button>
            ))}
          </nav>
        ) : null}
      </div>

      <div className="notification-pack-telegram-preview__chat">
        <TelegramBubble text={mode === "SOLO" ? soloText : aggregateText} time="00:42" />
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
