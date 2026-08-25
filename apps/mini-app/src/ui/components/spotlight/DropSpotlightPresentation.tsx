import { motion } from "framer-motion"
import type { NotificationPackBroadcastTemplate } from "@/shared/realtime/realtime.types"
import { ItemTypeArt } from "@/ui/components/events/EventArt"
import type { BetItemType, DropSpotlightPresentationMode } from "@/ui/model"
import { Glyph } from "@/ui/shared/MnemosIcon"
import "./drop-spotlight-preview.css"

const supportedSpotlightItemTypes: BetItemType[] = ["SWORD", "AURA", "PET", "BOOK", "JEWELRY"]

export type SpotlightPresentationTemplate = Pick<
  NotificationPackBroadcastTemplate,
  "emoji" | "soloTitle" | "soloBody" | "compact"
>

type DropSpotlightPresentationProps = {
  presentationMode: DropSpotlightPresentationMode
  itemName: string
  droppedFor: string
  itemRarity: string
  itemType: string
  template?: SpotlightPresentationTemplate | null
  embedded?: boolean
}

type DropSpotlightPreviewProps = Omit<DropSpotlightPresentationProps, "embedded"> & {
  size: "THUMBNAIL" | "CARD" | "DETAIL" | "STUDIO"
}

export function DropSpotlightPresentation({
  presentationMode,
  itemName,
  droppedFor,
  itemRarity,
  itemType,
  template,
  embedded = false,
}: DropSpotlightPresentationProps) {
  const values = {
    player: droppedFor,
    item: itemName,
    rarity: itemRarity,
    itemType,
  }
  const fallbackBody = `${droppedFor} · ${itemRarity} · ${itemType}`
  const title = renderedTemplate(template?.soloTitle, values) || itemName
  const bodyTemplate = presentationMode === "COMPACT" ? template?.compact : template?.soloBody
  const body = renderedTemplate(bodyTemplate, values) || fallbackBody
  const compact = presentationMode === "COMPACT"
  const resolvedItemType = resolveSpotlightItemType(itemType)
  const className = [
    "realtime-spotlight",
    compact ? "is-compact" : "is-full",
    embedded ? "is-embedded" : "",
  ]
    .filter(Boolean)
    .join(" ")

  return (
    <aside
      className={className}
      data-rarity={itemRarity.toLowerCase()}
      role={embedded ? undefined : "status"}
      aria-live={embedded ? undefined : "polite"}
      aria-atomic={embedded ? undefined : "true"}
    >
      <span className="realtime-spotlight__scan" aria-hidden="true" />
      <span className="realtime-spotlight__corners" aria-hidden="true">
        <i />
        <i />
        <i />
        <i />
      </span>

      <motion.span
        className="realtime-spotlight__visual"
        aria-hidden="true"
        animate={{ y: [0, -3, 0], rotate: [0, -0.7, 0.7, 0] }}
        transition={{ duration: 2.8, repeat: Infinity, ease: "easeInOut" }}
      >
        {resolvedItemType ? (
          <ItemTypeArt itemType={resolvedItemType} size={compact ? 40 : 78} />
        ) : (
          <Glyph name="item-drop" size={compact ? 20 : 32} />
        )}
        {template?.emoji ? <b>{template.emoji}</b> : null}
      </motion.span>

      <span className="realtime-spotlight__copy">
        <small>
          <i /> MNEMOS SPOTLIGHT · {itemRarity}
        </small>
        <strong>{title}</strong>
        {compact ? <em>{body}</em> : <p>{body}</p>}
        {!compact ? (
          <span className="realtime-spotlight__meta">
            <b>{droppedFor}</b>
            <i />
            <em>{itemType}</em>
          </span>
        ) : null}
      </span>

      <span className="realtime-spotlight__signal" aria-hidden="true">
        <i />
        <i />
        <i />
      </span>
      <span className="realtime-spotlight__lifetime" aria-hidden="true" />
    </aside>
  )
}

export function DropSpotlightPreview({ size, ...presentation }: DropSpotlightPreviewProps) {
  if (size === "STUDIO") {
    return <ItemDropStudioPreview {...presentation} />
  }

  return (
    <div className={`drop-spotlight-preview drop-spotlight-preview--${size.toLowerCase()}`}>
      <div className="drop-spotlight-preview__stage">
        <DropSpotlightPresentation {...presentation} embedded />
      </div>
    </div>
  )
}

function ItemDropStudioPreview({
  presentationMode,
  itemName,
  droppedFor,
  itemRarity,
  itemType,
  template,
}: DropSpotlightPresentationProps) {
  const values = {
    player: droppedFor,
    item: itemName,
    rarity: itemRarity,
    itemType,
  }
  const title = renderedTemplate(template?.soloTitle, values) || itemName
  const bodyTemplate = presentationMode === "COMPACT" ? template?.compact : template?.soloBody
  const body =
    renderedTemplate(bodyTemplate, values) || `${droppedFor} · ${itemRarity} · ${itemType}`
  const resolvedItemType = resolveSpotlightItemType(itemType)

  return (
    <div className="item-drop-studio-preview" data-rarity={itemRarity.toLowerCase()}>
      <span className="item-drop-studio-preview__art" aria-hidden="true">
        {resolvedItemType ? (
          <ItemTypeArt itemType={resolvedItemType} size={42} />
        ) : (
          <Glyph name="item-drop" size={22} />
        )}
      </span>

      <span className="item-drop-studio-preview__copy">
        <small>{droppedFor}</small>
        <strong>
          {template?.emoji ? <b>{template.emoji}</b> : null}
          {title}
        </strong>
        <em>{body}</em>
      </span>

      <i className="item-drop-studio-preview__meta">
        {itemRarity} · {itemType}
      </i>
    </div>
  )
}

function resolveSpotlightItemType(value: string): BetItemType | null {
  return supportedSpotlightItemTypes.includes(value as BetItemType) ? (value as BetItemType) : null
}

function renderedTemplate(template: string | undefined, values: Record<string, string>): string {
  if (!template) {
    return ""
  }

  return template
    .replace(/\{([a-zA-Z][a-zA-Z0-9_]*)}/g, (_, key: string) => values[key] ?? `{${key}}`)
    .trim()
}
