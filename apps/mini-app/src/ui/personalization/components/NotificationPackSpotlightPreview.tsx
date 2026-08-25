import type { ReactElement } from "react"
import { useTranslation } from "react-i18next"
import {
  BoosterArt,
  DungeonArt,
  GlobalEventArt,
  ItemTypeArt,
  RaidArt,
} from "@/ui/components/events/EventArt"
import type { NotificationPackContent, NotificationTemplateKey, Personalization } from "@/ui/model"
import "./notification-pack-context-preview.css"

type NotificationPackSpotlightPreviewProps = {
  item: Personalization
  size: "THUMBNAIL" | "CARD" | "DETAIL" | "STUDIO"
}

type PreviewSample = {
  key: NotificationTemplateKey
  values: Record<string, string>
}

export function NotificationPackSpotlightPreview({
  item,
  size,
}: NotificationPackSpotlightPreviewProps) {
  const { t } = useTranslation()
  const content = item.content

  if (!content || content.kind !== "NOTIFICATION_PACK") {
    return null
  }

  const samples: PreviewSample[] = [
    {
      key: "ITEM_DROP",
      values: {
        player: "north",
        item: t("personalization.preview.samples.item"),
        rarity: t("personalization.preview.samples.mythical"),
        itemType: t("personalization.preview.samples.sword"),
      },
    },
    {
      key: "BOOSTER",
      values: {
        player: "vex",
        booster: t("personalization.preview.samples.luck"),
      },
    },
    {
      key: "DUNGEON",
      values: {
        dungeon: t("personalization.preview.samples.expert"),
      },
    },
    {
      key: "RAID",
      values: {
        locations: "2 · 4 · 7",
      },
    },
    {
      key: "GLOBAL",
      values: {
        event: t("personalization.preview.samples.eclipse"),
      },
    },
  ]

  return (
    <div
      className={`notification-pack-context-preview notification-pack-context-preview--${size.toLowerCase()}`}
    >
      <span className="notification-pack-context-preview__ambient" aria-hidden="true" />

      {samples.map((sample) => (
        <NotificationPreviewCard key={sample.key} content={content} sample={sample} />
      ))}
    </div>
  )
}

function NotificationPreviewCard({
  content,
  sample,
}: {
  content: NotificationPackContent
  sample: PreviewSample
}) {
  const { t } = useTranslation()
  const template = content.templates[sample.key]
  const title = withEmoji(template.emoji, renderTemplate(template.soloTitle, sample.values))
  const body = renderTemplate(template.soloBody, sample.values)

  return (
    <article
      className="notification-pack-context-preview__message"
      data-template={sample.key.toLowerCase()}
    >
      <span className="notification-pack-context-preview__art" aria-hidden="true">
        {eventArt(sample.key)}
      </span>

      <span className="notification-pack-context-preview__copy">
        <small>{t(`creator.editor.notificationTypes.${sample.key}`)}</small>
        <strong>{title}</strong>
        <em>{body}</em>
      </span>
    </article>
  )
}

function eventArt(type: NotificationTemplateKey): ReactElement {
  if (type === "ITEM_DROP") {
    return <ItemTypeArt itemType="SWORD" size={34} />
  }

  if (type === "BOOSTER") {
    return <BoosterArt boosterType="LUCK" size={34} />
  }

  if (type === "DUNGEON") {
    return <DungeonArt size={34} />
  }

  if (type === "RAID") {
    return <RaidArt size={34} />
  }

  return <GlobalEventArt eventType="ECLIPSE" size={34} />
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
