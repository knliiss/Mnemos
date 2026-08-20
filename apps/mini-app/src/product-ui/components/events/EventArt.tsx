import type { CSSProperties } from "react"
import { useTranslation } from "react-i18next"
import type { BetItemType, BoosterType, FeedEvent, GlobalEventType } from "../../model"

type Props = {
  event: FeedEvent
  size?: number
}

export function EventArt({ event, size = 46 }: Props) {
  const rarityClass =
    event.kind === "ITEM_DROP" && event.rarity
      ? `event-art--rarity-${event.rarity.toLowerCase()}`
      : ""

  return <ArtFrame variant={getVariant(event)} size={size} extraClass={rarityClass} />
}

export function ItemTypeArt({ itemType, size = 46 }: { itemType: BetItemType; size?: number }) {
  return <ArtFrame variant={dropVariant(itemType)} size={size} />
}

export function BoosterArt({
  boosterType,
  size = 46,
}: {
  boosterType: BoosterType
  size?: number
}) {
  return <ArtFrame variant={boosterVariant(boosterType)} size={size} />
}

export function DungeonArt({ size = 46 }: { size?: number }) {
  return <ArtFrame variant="dungeon" size={size} />
}

export function RaidArt({ size = 46 }: { size?: number }) {
  return <ArtFrame variant="raid" size={size} />
}

export function GlobalEventArt({
  eventType,
  size = 46,
}: {
  eventType: GlobalEventType
  size?: number
}) {
  return <ArtFrame variant={globalVariant(eventType)} size={size} />
}

export function OwnerCrown({ compact = false }: { compact?: boolean }) {
  const { t } = useTranslation()

  return (
    <span
      className={`owner-event-crown ${compact ? "is-compact" : ""}`}
      title={t("events.detail.ownAccount")}
    >
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
  | "dungeon"
  | "raid"
  | "global-moon"
  | "global-eclipse"
  | "global-darkness"
  | "global-explosion"

const eventAssets: Partial<Record<Variant, string>> = {
  "drop-sword": "/event-icons/sword.png",
  "drop-aura": "/event-icons/aura.png",
  "drop-pet": "/event-icons/pet.png",
  "drop-book": "/event-icons/book.png",
  "drop-jewelry": "/event-icons/jewelry.png",
  "booster-luck": "/event-icons/booster-luck.png",
  "booster-money": "/event-icons/booster-money.png",
  "booster-damage": "/event-icons/booster-damage.png",
  "booster-power": "/event-icons/booster-power.png",
  dungeon: "/event-icons/dungeon.svg",
  raid: "/event-icons/raid.png",
  "global-moon": "/event-icons/moon.png",
  "global-eclipse": "/event-icons/eclipse.png",
  "global-darkness": "/event-icons/dark.png",
  "global-explosion": "/event-icons/explosion.png",
}

function ArtFrame({
  variant,
  size,
  extraClass = "",
}: {
  variant: Variant
  size: number
  extraClass?: string
}) {
  const asset = eventAssets[variant]
  const global = variant.startsWith("global-")
  const contentSize = global
    ? Math.min(24, Math.max(16, Math.round(size * 0.52)))
    : Math.min(30, Math.max(18, Math.round(size * 0.66)))
  const style = {
    width: size,
    height: size,
    "--event-art-content-size": `${contentSize}px`,
  } as CSSProperties

  return (
    <span
      className={`event-art event-art--${variant} ${asset ? "event-art--asset" : "event-art--vectorized"} ${extraClass}`.trim()}
      style={style}
      aria-hidden="true"
    >
      {asset ? (
        <img className="event-art__image" src={asset} alt="" draggable={false} decoding="async" />
      ) : (
        <svg
          className="event-art__vector"
          viewBox="0 0 48 48"
          fill="none"
          shapeRendering="crispEdges"
        >
          {renderVariant(variant)}
        </svg>
      )}
    </span>
  )
}

function dropVariant(itemType: BetItemType): Variant {
  if (itemType === "AURA") {
    return "drop-aura"
  }
  if (itemType === "PET") {
    return "drop-pet"
  }
  if (itemType === "BOOK") {
    return "drop-book"
  }
  if (itemType === "JEWELRY") {
    return "drop-jewelry"
  }

  return "drop-sword"
}

function boosterVariant(boosterType: BoosterType): Variant {
  if (boosterType === "MONEY") {
    return "booster-money"
  }
  if (boosterType === "DAMAGE") {
    return "booster-damage"
  }
  if (boosterType === "POWER") {
    return "booster-power"
  }

  return "booster-luck"
}

function globalVariant(eventType: GlobalEventType): Variant {
  if (eventType === "MOON") {
    return "global-moon"
  }
  if (eventType === "DARKNESS") {
    return "global-darkness"
  }
  if (eventType === "EXPLOSION") {
    return "global-explosion"
  }

  return "global-eclipse"
}

function getVariant(event: FeedEvent): Variant {
  if (event.kind === "ITEM_DROP") {
    return dropVariant((event.meta as BetItemType | undefined) ?? "SWORD")
  }

  if (event.kind === "BOOSTER") {
    return boosterVariant(event.title as BoosterType)
  }

  if (event.kind === "RAID") {
    return "raid"
  }

  return globalVariant(event.title as GlobalEventType)
}

function renderVariant(variant: Variant) {
  switch (variant) {
    case "booster-money":
      return (
        <>
          <rect className="event-art__soft" x="16" y="9" width="16" height="30" rx="7" />
          <circle className="event-art__line" cx="24" cy="24" r="5" />
          <path
            className="event-art__line"
            d="M20 7h8M20 41h8M26 20.5c-.7-.6-1.4-.9-2.2-.9-1.7 0-3 1.7-3 4.4s1.3 4.4 3 4.4c.8 0 1.5-.3 2.2-.9"
          />
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
          <path
            className="event-art__line"
            d="M20 7h8M20 41h8M24 19c-4-4-8 2-3.3 4.5C16 26 20 32 24 28c4 4 8-2 3.3-4.5C32 21 28 15 24 19Z"
          />
        </>
      )
    case "dungeon":
      return (
        <>
          <path className="event-art__soft" d="M11 39V19l5-8h16l5 8v20H11Z" />
          <path className="event-art__line" d="M16 39V21l4-5h8l4 5v18M20 39V27h8v12" />
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
        <g className="event-art__pixel-symbol">
          <rect x="14" y="12" width="20" height="24" fill="currentColor" opacity=".78" />
          <rect x="12" y="16" width="24" height="16" fill="currentColor" />
          <rect x="16" y="10" width="16" height="28" fill="currentColor" />
          <rect x="16" y="14" width="4" height="4" fill="#FF6B73" opacity=".78" />
          <rect x="28" y="30" width="4" height="4" fill="#8B1326" opacity=".72" />
          <rect x="12" y="12" width="4" height="4" fill="#1B1214" opacity=".9" />
          <rect x="32" y="32" width="4" height="4" fill="#1B1214" opacity=".9" />
        </g>
      )
    case "global-darkness":
      return (
        <g className="event-art__pixel-symbol">
          <rect x="13" y="13" width="22" height="22" fill="#25292E" />
          <rect x="16" y="11" width="16" height="26" fill="#343A42" />
          <rect x="11" y="16" width="26" height="16" fill="#343A42" />
          <rect x="16" y="16" width="16" height="16" fill="currentColor" opacity=".72" />
          <rect x="18" y="16" width="10" height="3" fill="#C1C8D0" opacity=".42" />
          <rect x="30" y="29" width="3" height="3" fill="#111416" opacity=".9" />
        </g>
      )
    case "global-explosion":
      return (
        <g className="event-art__pixel-symbol">
          <rect x="18" y="10" width="12" height="28" fill="#FF8F13" />
          <rect x="12" y="16" width="24" height="16" fill="#FF8F13" />
          <rect x="15" y="13" width="18" height="22" fill="currentColor" />
          <rect x="10" y="20" width="28" height="8" fill="currentColor" />
          <rect x="18" y="16" width="12" height="12" fill="#FFD22E" />
          <rect x="20" y="14" width="5" height="5" fill="#FFF083" />
          <rect x="30" y="28" width="4" height="4" fill="#C75B0A" />
        </g>
      )
    case "global-eclipse":
      return (
        <g className="event-art__pixel-symbol">
          <rect x="18" y="8" width="12" height="4" fill="#B7D9E7" />
          <rect x="12" y="12" width="24" height="4" fill="#8FC3D5" />
          <rect x="8" y="18" width="4" height="12" fill="#8FC3D5" />
          <rect x="36" y="18" width="4" height="12" fill="#8FC3D5" />
          <rect x="12" y="32" width="24" height="4" fill="#6F9EAF" />
          <rect x="18" y="36" width="12" height="4" fill="#6F9EAF" />

          <rect x="14" y="16" width="20" height="16" fill="#D3E8EF" />
          <rect x="18" y="12" width="12" height="24" fill="#D3E8EF" />

          <rect x="18" y="16" width="16" height="16" fill="#15191D" />
          <rect x="16" y="20" width="4" height="8" fill="#15191D" />
          <rect x="20" y="14" width="8" height="4" fill="#15191D" />
          <rect x="20" y="30" width="8" height="4" fill="#15191D" />

          <rect x="30" y="18" width="4" height="10" fill="#F1FAFC" />
          <rect x="28" y="14" width="4" height="4" fill="#F1FAFC" />
          <rect x="28" y="28" width="4" height="4" fill="#A7CEDD" />
        </g>
      )
    default:
      return null
  }
}
