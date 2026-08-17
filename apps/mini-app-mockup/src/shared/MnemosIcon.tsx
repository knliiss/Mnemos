import type { CSSProperties, ReactElement } from "react"

export type GlyphName = "events" | "account" | "person" | "search" | "bell" | "bet" | "members" | "roles" | "wallet" | "store" | "creator" | "moderation" | "settings" | "back" | "more" | "plus" | "check" | "close" | "send" | "edit" | "trash" | "swap" | "item-drop" | "booster" | "global" | "raid" | "dungeon" | "external" | "spark"

export function Glyph({ name, size = 20, className }: { name: GlyphName; size?: number; className?: string }) {
  const common = { width: size, height: size, viewBox: "0 0 24 24", fill: "none", stroke: "currentColor", strokeWidth: 1.55, strokeLinecap: "round" as const, strokeLinejoin: "round" as const, className: `glyph ${className ?? ""}`, "aria-hidden": true }
  const paths: Record<GlyphName, ReactElement> = {
    events: <><path d="M4 7.4h5.2l2.1-3.1 3.2 15.4 2.1-8.4H20"/><path d="M4 18.2h3.5"/></>,
    account: <><rect x="4.2" y="5" width="15.6" height="14" rx="3"/><path d="M8 5V3.5M16 5V3.5M8 10h8M8 14h5"/></>,
    person: <><circle cx="12" cy="8" r="3"/><path d="M5.5 19c.8-4 3-6 6.5-6s5.7 2 6.5 6"/></>,
    search: <><circle cx="10.5" cy="10.5" r="5.7"/><path d="m15 15 4.3 4.3"/></>,
    bell: <><path d="M6.5 10.5c0-3.8 2.1-6 5.5-6s5.5 2.2 5.5 6v4.2l1.5 2H5l1.5-2z"/><path d="M10 19.1c.6.9 1.2 1.3 2 1.3s1.4-.4 2-1.3"/></>,
    bet: <><path d="M5 17.5 9.2 6.2h5.6L19 17.5"/><path d="M7 13h10M9.8 9.2h4.4"/></>,
    members: <><circle cx="9" cy="8" r="2.6"/><circle cx="16.4" cy="9.2" r="2"/><path d="M3.8 19c.6-4 2.3-6 5.2-6s4.6 2 5.2 6M14 14.2c2.7-.2 4.5 1.4 5.2 4.1"/></>,
    roles: <><path d="M12 3.2 19 6v5.2c0 4.3-2.7 7.5-7 9.6-4.3-2.1-7-5.3-7-9.6V6z"/><path d="m9 12 2 2 4-4"/></>,
    wallet: <><path d="M4 7.5h15.2v11H5.5A1.5 1.5 0 0 1 4 17z"/><path d="M4 8V6.5A1.5 1.5 0 0 1 5.5 5H17"/><path d="M15.5 12h4v3h-4a1.5 1.5 0 0 1 0-3Z"/></>,
    store: <><path d="M5 9.5h14l-1 10H6z"/><path d="M8 9.5V7a4 4 0 0 1 8 0v2.5"/><path d="M9 14h6"/></>,
    creator: <><path d="m4.5 18.7 3.1-7.8 5.5-5.5 5.5 5.5-5.5 5.5z"/><path d="m13.1 5.4 2-2 5.5 5.5-2 2M7.6 10.9l5.5 5.5"/></>,
    moderation: <><path d="M12 3.2 19 6v5.2c0 4.3-2.7 7.5-7 9.6-4.3-2.1-7-5.3-7-9.6V6z"/><path d="M8.4 12h7.2M12 8.4v7.2"/></>,
    settings: <><circle cx="12" cy="12" r="3"/><path d="M12 3.5v2M12 18.5v2M3.5 12h2M18.5 12h2M6 6l1.4 1.4M16.6 16.6 18 18M18 6l-1.4 1.4M7.4 16.6 6 18"/></>,
    back: <path d="m14.5 5-7 7 7 7"/>,
    more: <><circle cx="5" cy="12" r="1" fill="currentColor" stroke="none"/><circle cx="12" cy="12" r="1" fill="currentColor" stroke="none"/><circle cx="19" cy="12" r="1" fill="currentColor" stroke="none"/></>,
    plus: <><path d="M12 5v14M5 12h14"/></>,
    check: <path d="m5 12.5 4.2 4.2L19 7"/>,
    close: <><path d="M6 6l12 12M18 6 6 18"/></>,
    send: <><path d="m4 4 16 8-16 8 3-8zM7 12h13"/></>,
    edit: <><path d="M5 18.5 6 14l9.8-9.8 4 4L10 18zM14.8 5.2l4 4"/></>,
    trash: <><path d="M6.5 8h11l-.8 12h-9.4zM5 5.5h14M9 5.5V4h6v1.5M10 11v5M14 11v5"/></>,
    swap: <><path d="M5 8h13l-3-3M19 16H6l3 3"/></>,
    "item-drop": <><path d="M12 3.5 18.5 7v7L12 20.5 5.5 14V7z"/><path d="M5.5 7 12 11l6.5-4M12 11v9.5"/></>,
    booster: <path d="m13 3-7 10h5l-1 8 8-12h-5z"/>,
    global: <><circle cx="12" cy="12" r="8"/><path d="M4.5 9h15M4.5 15h15M12 4c2.4 2.2 3.4 4.8 3.4 8s-1 5.8-3.4 8c-2.4-2.2-3.4-4.8-3.4-8S9.6 6.2 12 4Z"/></>,
    raid: <><path d="M5 17.5 9.5 6l2.5 5 2.5-5L19 17.5z"/><path d="M7 14h10"/></>,
    dungeon: <><path d="M5 20V8l7-4 7 4v12M8 20v-7h8v7"/><path d="M9 9h.1M15 9h.1"/></>,
    external: <><path d="M13 5h6v6M19 5l-8 8"/><path d="M18 14v5H5V6h5"/></>,
    spark: <><path d="M12 3.2 13.7 9 19 12l-5.3 3L12 20.8 10.3 15 5 12l5.3-3z"/></>,
  }
  return <svg {...common}>{paths[name]}</svg>
}

export function CurrencyMark({ currency, size = 18, style }: { currency: "COIN" | "PRIME"; size?: number; style?: CSSProperties }) {
  if (currency === "COIN") return <svg className="currency-glyph" style={style} width={size} height={size} viewBox="0 0 24 24" aria-label="Coin"><circle cx="12" cy="12" r="8" fill="none" stroke="currentColor" strokeWidth="1.6"/><path d="M12 7.5 15.8 12 12 16.5 8.2 12z" fill="none" stroke="currentColor" strokeWidth="1.35"/></svg>
  return <svg className="currency-glyph currency-glyph--prime" style={style} width={size} height={size} viewBox="0 0 24 24" aria-label="Prime"><path d="M12 3.3 19.2 12 12 20.7 4.8 12z" fill="none" stroke="currentColor" strokeWidth="1.55"/><path d="M12 3.3 14.1 12 12 20.7 9.9 12z" fill="none" stroke="currentColor" strokeWidth="1.15"/></svg>
}
