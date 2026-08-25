import type { ReactElement } from "react"
import { useState } from "react"
import { useTranslation } from "react-i18next"
import {
  BoosterArt,
  DungeonArt,
  GlobalEventArt,
  ItemTypeArt,
  RaidArt,
} from "@/ui/components/events/EventArt"
import type {
  NotificationTemplateKey,
  NotificationTemplateVariant,
  Personalization,
} from "@/ui/model"
import {
  notificationTemplateKeys,
  renderNotificationTemplate,
  selectedNotificationTemplate,
  valuesForVariant,
  variantLabel,
  withNotificationEmoji,
} from "./notificationPackPreview"
import "./notification-pack-context-preview.css"

type NotificationPackContextPreviewProps = {
  item: Personalization
  size: "THUMBNAIL" | "CARD" | "DETAIL" | "STUDIO"
}

export function NotificationPackContextPreview({
  item,
  size,
}: NotificationPackContextPreviewProps) {
  const { t } = useTranslation()
  const [selectedType, setSelectedType] = useState<NotificationTemplateKey>("ITEM_DROP")
  const [selectedVariantId, setSelectedVariantId] = useState<string | null>(null)
  const content = item.content

  if (!content || content.kind !== "NOTIFICATION_PACK") {
    return null
  }

  const template = content.templates[selectedType]
  const variants = template.variants ?? []
  const selectedVariant =
    selectedVariantId === null
      ? null
      : variants.find((variant) => variant.id === selectedVariantId) ?? null
  const message = selectedNotificationTemplate(template, selectedVariant?.id ?? null)
  const values = valuesForVariant(selectedType, selectedVariant)
  const title = withNotificationEmoji(
    message.emoji,
    renderNotificationTemplate(content, message.soloTitle, values),
  )
  const body = renderNotificationTemplate(content, message.soloBody, values)

  return (
    <section
      className={`notification-pack-context-preview notification-pack-context-preview--${size.toLowerCase()}`}
    >
      <span className="notification-pack-context-preview__ambient" aria-hidden="true" />

      <nav
        className="notification-pack-context-preview__events"
        aria-label={t("creator.editor.eventTemplate")}
      >
        {notificationTemplateKeys.map((type) => (
          <button
            type="button"
            key={type}
            className={type === selectedType ? "is-active" : ""}
            onClick={() => {
              setSelectedType(type)
              setSelectedVariantId(null)
            }}
            title={t(`creator.editor.notificationTypes.${type}`)}
            aria-label={t(`creator.editor.notificationTypes.${type}`)}
          >
            {eventArt(type, size === "THUMBNAIL" ? 22 : 28)}
          </button>
        ))}
      </nav>

      <article
        className="notification-pack-context-preview__message"
        data-template={selectedType.toLowerCase()}
      >
        <span className="notification-pack-context-preview__art" aria-hidden="true">
          {eventArt(selectedType, size === "DETAIL" ? 46 : 38)}
        </span>

        <span className="notification-pack-context-preview__copy">
          <small>{t(`creator.editor.notificationTypes.${selectedType}`)}</small>
          <strong>{title}</strong>
          <em>{body}</em>
        </span>
      </article>

      {variants.length ? (
        <nav
          className="notification-pack-context-preview__variants"
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
    </section>
  )
}

function eventArt(type: NotificationTemplateKey, size: number): ReactElement {
  if (type === "ITEM_DROP") {
    return <ItemTypeArt itemType="SWORD" size={size} />
  }

  if (type === "BOOSTER") {
    return <BoosterArt boosterType="LUCK" size={size} />
  }

  if (type === "DUNGEON") {
    return <DungeonArt size={size} />
  }

  if (type === "RAID") {
    return <RaidArt size={size} />
  }

  return <GlobalEventArt eventType="ECLIPSE" size={size} />
}
