import type { BetItemType, FeedEvent, GlobalEventType } from "../model"

type Props = {
  event: FeedEvent
  size?: number
}

export function EventArt({ event, size = 46 }: Props) {
  return <ArtFrame variant={getVariant(event)} size={size} />
}

export function ItemTypeArt({ itemType, size = 46 }: { itemType: BetItemType; size?: number }) {
  return <ArtFrame variant={dropVariant(itemType)} size={size} />
}

export function GlobalEventArt({ eventType, size = 46 }: { eventType: GlobalEventType; size?: number }) {
  return <ArtFrame variant={globalVariant(eventType)} size={size} />
}

export function OwnerCrown({ compact = false }: { compact?: boolean }) {
  return (
    <span className={`owner-event-crown ${compact ? "is-compact" : ""}`} title="Подія активного аккаунта">
      <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M5 8.5 8.7 12l3.3-6 3.3 6L19 8.5l-1.4 8H6.4L5 8.5Z" />
        <path d="M7 19h10" />
      </svg>
    </span>
  )
}

type Variant =
  | "drop-sword"
  | "drop-aura"
  | "drop-pet"
  | "drop-book"
  | "drop-jewelry"
  | "booster-luck"
  | "booster-money"
  | "booster-damage"
  | "booster-power"
  | "raid"
  | "global-moon"
  | "global-eclipse"
  | "global-darkness"
  | "global-explosion"

const dropAssets: Partial<Record<Variant, string>> = {
  "drop-sword": "/event-icons/sword.svg",
  "drop-aura": "/event-icons/aura.svg",
  "drop-pet": "/event-icons/pet.svg",
  "drop-book": "/event-icons/book.svg",
  "drop-jewelry": "/event-icons/jewelry.svg",
}

function ArtFrame({ variant, size }: { variant: Variant; size: number }) {
  const asset = dropAssets[variant]

  return (
    <span
      className={`event-art event-art--${variant} ${asset ? "event-art--asset" : ""}`}
      style={{ width: size, height: size }}
      aria-hidden="true"
    >
      {asset ? (
        <img className="event-art__image" src={asset} alt="" draggable={false} />
      ) : (
        <svg viewBox="0 0 48 48" fill="none">
          {renderVariant(variant)}
        </svg>
      )}
    </span>
  )
}

function dropVariant(itemType: BetItemType): Variant {
  if (itemType === "AURA") return "drop-aura"
  if (itemType === "PET") return "drop-pet"
  if (itemType === "BOOK") return "drop-book"
  if (itemType === "JEWELRY") return "drop-jewelry"
  return "drop-sword"
}

function globalVariant(eventType: GlobalEventType): Variant {
  if (eventType === "MOON") return "global-moon"
  if (eventType === "DARKNESS") return "global-darkness"
  if (eventType === "EXPLOSION") return "global-explosion"
  return "global-eclipse"
}

function getVariant(event: FeedEvent): Variant {
  if (event.kind === "ITEM_DROP") {
    return dropVariant((event.meta as BetItemType | undefined) ?? "SWORD")
  }

  if (event.kind === "BOOSTER") {
    if (event.title === "MONEY") return "booster-money"
    if (event.title === "DAMAGE") return "booster-damage"
    if (event.title === "POWER") return "booster-power"
    return "booster-luck"
  }

  if (event.kind === "RAID") return "raid"

  return globalVariant(event.title as GlobalEventType)
}

function renderVariant(variant: Variant) {
  switch (variant) {
    case "booster-money":
      return (
        <>
          <rect className="event-art__soft" x="16" y="9" width="16" height="30" rx="7" />
          <circle className="event-art__line" cx="24" cy="24" r="5" />
          <path className="event-art__line" d="M20 7h8M20 41h8M26 20.5c-.7-.6-1.4-.9-2.2-.9-1.7 0-3 1.7-3 4.4s1.3 4.4 3 4.4c.8 0 1.5-.3 2.2-.9" />
        </>
      )
    case "booster-damage":
      return (
        <>
          <rect className="event-art__soft" x="16" y="9" width="16" height="30" rx="7" />
          <path className="event-art__line" d="M20 7h8M20 41h8M20 20l8 8M28 20l-8 8" />
        </>
      )
    case "booster-power":
      return (
        <>
          <rect className="event-art__soft" x="16" y="9" width="16" height="30" rx="7" />
          <path className="event-art__line" d="M20 7h8M20 41h8m9-23-7 9h5l-7 8" />
        </>
      )
    case "booster-luck":
      return (
        <>
          <rect className="event-art__soft" x="16" y="9" width="16" height="30" rx="7" />
          <path className="event-art__line" d="M20 7h8M20 41h8M24 19c-4-4-8 2-3.3 4.5C16 26 20 32 24 28c4 4 8-2 3.3-4.5C32 21 28 15 24 19Z" />
        </>
      )
    case "raid":
      return (
        <>
          <path className="event-art__soft" d="M14 10h20v25H14z" />
          <path className="event-art__line" d="M14 10v29M34 10v29M18 18l6 4 6-4M18 29l6-4 6 4" />
        </>
      )
    case "global-moon":
      return (
        <>
          <circle className="event-art__soft" cx="24" cy="24" r="13" />
          <path className="event-art__line" d="M30 11c-6 1.5-9.3 6-9.3 12.2S24 34 30 36c-8.8 1.3-16-4-16-12.5S21.2 9.8 30 11Z" />
        </>
      )
    case "global-darkness":
      return (
        <>
          <circle className="event-art__soft" cx="24" cy="24" r="12" />
          <path className="event-art__line" d="M12 24h4M32 24h4M24 12v4M24 32v4M15.5 15.5l2.8 2.8M29.7 29.7l2.8 2.8" />
        </>
      )
    case "global-explosion":
      return (
        <>
          <path className="event-art__soft" d="m24 8 3 10 10-3-7 9 9 5-11 1-2 10-5-9-10 4 6-9-8-7 11 1 4-12Z" />
          <circle className="event-art__fill" cx="24" cy="24" r="4" />
        </>
      )
    case "global-eclipse":
    default:
      return (
        <>
          <circle className="event-art__soft" cx="24" cy="24" r="13" />
          <circle className="event-art__line" cx="24" cy="24" r="8" />
          <path className="event-art__line" d="M8 24h4M36 24h4" />
        </>
      )
  }
}
