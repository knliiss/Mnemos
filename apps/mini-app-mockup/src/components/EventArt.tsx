import type { FeedEvent } from "../model"

type Props = {
  event: FeedEvent
  size?: number
}

export function EventArt({ event, size = 46 }: Props) {
  const variant = getVariant(event)

  return (
    <span
      className={`event-art event-art--${variant}`}
      style={{ width: size, height: size }}
      aria-hidden="true"
    >
      <svg viewBox="0 0 48 48" fill="none">
        {renderVariant(variant)}
      </svg>
    </span>
  )
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

function getVariant(event: FeedEvent): Variant {
  if (event.kind === "ITEM_DROP") {
    if (event.meta === "AURA") return "drop-aura"
    if (event.meta === "PET") return "drop-pet"
    if (event.meta === "BOOK") return "drop-book"
    if (event.meta === "JEWELRY") return "drop-jewelry"
    return "drop-sword"
  }

  if (event.kind === "BOOSTER") {
    if (event.title === "MONEY") return "booster-money"
    if (event.title === "DAMAGE") return "booster-damage"
    if (event.title === "POWER") return "booster-power"
    return "booster-luck"
  }

  if (event.kind === "RAID") return "raid"

  if (event.title === "MOON") return "global-moon"
  if (event.title === "DARKNESS") return "global-darkness"
  if (event.title === "EXPLOSION") return "global-explosion"
  return "global-eclipse"
}

function renderVariant(variant: Variant) {
  switch (variant) {
    case "drop-sword":
      return (
        <>
          <path className="event-art__soft" d="M24 8 31 15 20 32l-5 1 1-5 17-17Z" />
          <path className="event-art__line" d="m27.8 11.8 5.2 5.2M17 28l3 3M14 35l6-6" />
        </>
      )
    case "drop-aura":
      return (
        <>
          <circle className="event-art__soft" cx="24" cy="24" r="6" />
          <path className="event-art__line" d="M13 24c2.1-6.2 5.8-9.3 11-9.3S32.9 17.8 35 24M10 24c2.8 9.3 7.5 14 14 14s11.2-4.7 14-14" />
        </>
      )
    case "drop-pet":
      return (
        <>
          <path className="event-art__soft" d="M17 20c0-5 3.1-8 7-8s7 3 7 8v6c0 5-3 8-7 8s-7-3-7-8v-6Z" />
          <circle className="event-art__fill" cx="21" cy="23" r="1.4" />
          <circle className="event-art__fill" cx="27" cy="23" r="1.4" />
          <path className="event-art__line" d="M19 14 15 10v8M29 14l4-4v8M21 29c2 1.5 4 1.5 6 0" />
        </>
      )
    case "drop-book":
      return (
        <>
          <path className="event-art__soft" d="M13 12h10c3 0 5 1.3 5 4v20c0-2.2-1.7-3.2-5-3.2H13V12Z" />
          <path className="event-art__line" d="M35 12H25c-3 0-5 1.3-5 4v20c0-2.2 1.7-3.2 5-3.2h10V12ZM24 16v17" />
        </>
      )
    case "drop-jewelry":
      return (
        <>
          <circle className="event-art__soft" cx="24" cy="27" r="8" />
          <path className="event-art__line" d="m18 15 6-6 6 6-6 6-6-6ZM18 15h12" />
        </>
      )
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
