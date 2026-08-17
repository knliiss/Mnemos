import type { CSSProperties, ReactElement } from "react"

export type GlyphName = "events" | "home" | "account" | "person" | "search" | "bell" | "bet" | "members" | "roles" | "wallet" | "store" | "creator" | "moderation" | "admin" | "settings" | "invite" | "daily" | "back" | "more" | "plus" | "check" | "close" | "send" | "edit" | "trash" | "swap" | "clock" | "item-drop" | "booster" | "global" | "raid" | "dungeon" | "external" | "spark" | "like" | "dislike" | "chat"

export function Glyph({ name, size = 20, className }: { name: GlyphName; size?: number; className?: string }) {
  const common = {
    width: size,
    height: size,
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: 1.55,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
    className: `glyph ${className ?? ""}`,
    "aria-hidden": true,
  }

  const paths: Record<GlyphName, ReactElement> = {
    events: <><path d="M4 17.5h3.6l2.1-8.7 3.4 11 2.1-7.3H20"/><path d="M5 5.5h14"/></>,
    home: <><path d="m4.5 11.2 7.5-6 7.5 6v8.1H4.5z"/><path d="M9.3 19.3v-5.5h5.4v5.5"/></>,
    account: <><rect x="4.5" y="5.5" width="12.5" height="13" rx="2"/><path d="M8 9h5.5M8 12.5h3.5M17 8.5h2.5v8H17"/></>,
    person: <><circle cx="12" cy="8" r="3"/><path d="M5.5 19c.8-4 3-6 6.5-6s5.7 2 6.5 6"/></>,
    search: <><circle cx="10.2" cy="10.2" r="5.8"/><path d="m14.7 14.7 4.8 4.8"/></>,
    bell: <><path d="M6.5 10.5c0-3.8 2.1-6 5.5-6s5.5 2.2 5.5 6v4.2l1.5 2H5l1.5-2z"/><path d="M10 19.1c.6.9 1.2 1.3 2 1.3s1.4-.4 2-1.3"/></>,
    bet: <><rect x="4.5" y="5" width="15" height="14" rx="3"/><path d="m8 12 2.4 2.4L16.5 8.5"/></>,
    members: <><circle cx="9" cy="8" r="2.6"/><circle cx="16.4" cy="9.2" r="2"/><path d="M3.8 19c.6-4 2.3-6 5.2-6s4.6 2 5.2 6M14 14.2c2.7-.2 4.5 1.4 5.2 4.1"/></>,
    roles: <><path d="M12 3.8 18.5 7v5.2c0 4.2-2.4 6.8-6.5 8-4.1-1.2-6.5-3.8-6.5-8V7z"/><path d="m9.3 12 1.8 1.8 3.8-4"/></>,
    wallet: <><path d="M4 7.5h15.2v11H5.5A1.5 1.5 0 0 1 4 17z"/><path d="M4 8V6.5A1.5 1.5 0 0 1 5.5 5H17"/><path d="M15.5 12h4v3h-4a1.5 1.5 0 0 1 0-3Z"/></>,
    store: <><path d="M6.2 8.2h11.6l1 11H5.2z"/><path d="M9 8.2V6.8a3 3 0 0 1 6 0v1.4"/></>,
    creator: <><path d="M4.8 17.8 8 6.2l8-2 3.2 8-8 7.6z"/><path d="m8 6.2 5.2 5.2 3-7.2M13.2 11.4l6 1"/><circle cx="13.2" cy="11.4" r="1.4"/></>,
    moderation: <><path d="M12 3.8 18.5 7v5.2c0 4.2-2.4 6.8-6.5 8-4.1-1.2-6.5-3.8-6.5-8V7z"/><path d="M8.5 12h7M12 8.5v7"/></>,
    admin: <><path d="M5 6h14M7 6v13M17 6v13M4 19h16"/><path d="M9.5 10h5M9.5 14h5"/></>,
    settings: <><path d="M4 7h10M18 7h2M4 17h2M10 17h10M14 4v6M6 14v6"/><circle cx="14" cy="7" r="2"/><circle cx="8" cy="17" r="2"/></>,
    invite: <><path d="M4 12h11"/><path d="m12 8 4 4-4 4"/><path d="M18 5h2v14h-2"/></>,
    daily: <><path d="M5 9h14v10H5zM4 9h16V6H4zM12 6v13M8.2 6C6.3 6 5.4 4.8 6 3.8c.8-1.3 3.2-.2 6 2.2M15.8 6c1.9 0 2.8-1.2 2.2-2.2-.8-1.3-3.2-.2-6 2.2"/></>,
    back: <path d="m14.5 5-7 7 7 7"/>,
    more: <><circle cx="5" cy="12" r="1" fill="currentColor" stroke="none"/><circle cx="12" cy="12" r="1" fill="currentColor" stroke="none"/><circle cx="19" cy="12" r="1" fill="currentColor" stroke="none"/></>,
    plus: <><path d="M12 5v14M5 12h14"/></>,
    check: <path d="m5 12.5 4.2 4.2L19 7"/>,
    close: <><path d="M6 6l12 12M18 6 6 18"/></>,
    send: <><path d="m4 4 16 8-16 8 3-8zM7 12h13"/></>,
    edit: <><path d="M5 18.5 6 14l9.8-9.8 4 4L10 18zM14.8 5.2l4 4"/></>,
    trash: <><path d="M6.5 8h11l-.8 12h-9.4zM5 5.5h14M9 5.5V4h6v1.5M10 11v5M14 11v5"/></>,
    swap: <><path d="M5 8h13l-3-3M19 16H6l3 3"/></>,
    clock: <><circle cx="12" cy="12" r="8"/><path d="M12 7.5V12l3.2 2"/></>,
    "item-drop": <><path d="M12 3.5 18.5 7v7L12 20.5 5.5 14V7z"/><path d="M5.5 7 12 11l6.5-4M12 11v9.5"/></>,
    booster: <path d="m13 3-7 10h5l-1 8 8-12h-5z"/>,
    global: <><circle cx="12" cy="12" r="8"/><path d="M4.5 9h15M4.5 15h15M12 4c2.4 2.2 3.4 4.8 3.4 8s-1 5.8-3.4 8c-2.4-2.2-3.4-4.8-3.4-8S9.6 6.2 12 4Z"/></>,
    raid: <><path d="M5 17.5 9.5 6l2.5 5 2.5-5L19 17.5z"/><path d="M7 14h10"/></>,
    dungeon: <><path d="M5 20V8l7-4 7 4v12M8 20v-7h8v7"/><path d="M9 9h.1M15 9h.1"/></>,
    external: <><path d="M13 5h6v6M19 5l-8 8"/><path d="M18 14v5H5V6h5"/></>,
    spark: <path d="M12 3.2 13.7 9 19 12l-5.3 3L12 20.8 10.3 15 5 12l5.3-3z"/>,
    like: <><path d="M8.2 10.2 11 4.8c.5-1 1.9-.8 2.1.3l.5 3.1h3.9c1.4 0 2.3 1.3 1.8 2.6l-2 6.2a2.4 2.4 0 0 1-2.3 1.7H8.2z"/><path d="M4.5 10.2h3.7v8.5H4.5z"/></>,
    dislike: <><path d="M8.2 13.8 11 19.2c.5 1 1.9.8 2.1-.3l.5-3.1h3.9c1.4 0 2.3-1.3 1.8-2.6l-2-6.2A2.4 2.4 0 0 0 15 5.3H8.2z"/><path d="M4.5 5.3h3.7v8.5H4.5z"/></>,
    chat: <><path d="M5 5.5h14v10H9l-4 3z"/><path d="M8 9h8M8 12h5"/></>,
  }

  return <svg {...common}>{paths[name]}</svg>
}

export function CurrencyMark({ currency, size = 18, style }: { currency: "COIN" | "PRIME"; size?: number; style?: CSSProperties }) {
  if (currency === "COIN") {
    return (
      <svg className="currency-glyph" style={style} width={size} height={size} viewBox="0 0 24 24" aria-label="Coin">
        <circle cx="12" cy="12" r="8.3" fill="none" stroke="currentColor" strokeWidth="1.6" />
        <path d="M14.8 8.6c-.8-.8-1.7-1.2-2.8-1.2-2.3 0-4 1.8-4 4.6s1.7 4.6 4 4.6c1.1 0 2-.4 2.8-1.2" fill="none" stroke="currentColor" strokeWidth="1.6" />
      </svg>
    )
  }

  return (
    <svg className="currency-glyph currency-glyph--prime" style={style} width={size} height={size} viewBox="0 0 24 24" aria-label="Prime">
      <path d="M12 3.3 19.2 12 12 20.7 4.8 12z" fill="none" stroke="currentColor" strokeWidth="1.55" />
      <path d="M12 3.3 14.1 12 12 20.7 9.9 12z" fill="none" stroke="currentColor" strokeWidth="1.15" />
    </svg>
  )
}
