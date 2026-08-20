import type { RootRoute } from "../model"

type Props = {
  route: RootRoute
}

export function RootNavIllustration({ route }: Props) {
  if (route === "bets") {
    return (
      <svg viewBox="0 0 32 32" shapeRendering="crispEdges" aria-hidden="true">
        <path d="M5 8h22v5a3 3 0 0 0 0 6v5H5v-5a3 3 0 0 0 0-6V8Z" fill="currentColor" opacity=".28" />
        <rect x="7" y="10" width="18" height="12" rx="2" fill="none" stroke="currentColor" strokeWidth="2" />
        <rect x="11" y="14" width="3" height="3" fill="currentColor" />
        <rect x="16" y="14" width="6" height="2" fill="currentColor" opacity=".74" />
        <rect x="16" y="18" width="4" height="2" fill="currentColor" opacity=".5" />
      </svg>
    )
  }

  if (route === "events") {
    return (
      <svg viewBox="0 0 32 32" shapeRendering="crispEdges" aria-hidden="true">
        <rect x="14" y="13" width="4" height="4" fill="currentColor" />
        <rect x="10" y="10" width="2" height="10" fill="currentColor" opacity=".7" />
        <rect x="20" y="10" width="2" height="10" fill="currentColor" opacity=".7" />
        <rect x="6" y="7" width="2" height="16" fill="currentColor" opacity=".34" />
        <rect x="24" y="7" width="2" height="16" fill="currentColor" opacity=".34" />
        <rect x="12" y="22" width="8" height="2" fill="currentColor" opacity=".58" />
      </svg>
    )
  }

  if (route === "account") {
    return (
      <svg viewBox="0 0 32 32" shapeRendering="crispEdges" aria-hidden="true">
        <path d="M4 15 16 6l12 9v11H4V15Z" fill="currentColor" opacity=".24" />
        <path d="M7 15 16 8l9 7v9h-6v-7h-6v7H7v-9Z" fill="none" stroke="currentColor" strokeWidth="2" />
        <rect x="14" y="18" width="4" height="6" fill="currentColor" opacity=".72" />
        <rect x="5" y="14" width="3" height="3" fill="currentColor" />
        <rect x="24" y="14" width="3" height="3" fill="currentColor" />
      </svg>
    )
  }

  if (route === "search") {
    return (
      <svg viewBox="0 0 32 32" shapeRendering="crispEdges" aria-hidden="true">
        <rect x="8" y="7" width="12" height="2" fill="currentColor" opacity=".7" />
        <rect x="6" y="9" width="2" height="10" fill="currentColor" opacity=".7" />
        <rect x="20" y="9" width="2" height="10" fill="currentColor" opacity=".7" />
        <rect x="8" y="19" width="12" height="2" fill="currentColor" opacity=".7" />
        <rect x="11" y="10" width="6" height="8" fill="currentColor" opacity=".16" />
        <rect x="20" y="20" width="3" height="3" fill="currentColor" />
        <rect x="23" y="23" width="5" height="3" fill="currentColor" />
        <rect x="24" y="7" width="2" height="2" fill="currentColor" opacity=".5" />
      </svg>
    )
  }

  return (
    <svg viewBox="0 0 32 32" shapeRendering="crispEdges" aria-hidden="true">
      <path d="M8 12h16l2 14H6l2-14Z" fill="currentColor" opacity=".23" />
      <path d="M9 13h14l1.5 11h-17L9 13Z" fill="none" stroke="currentColor" strokeWidth="2" />
      <rect x="12" y="8" width="2" height="5" fill="currentColor" />
      <rect x="18" y="8" width="2" height="5" fill="currentColor" />
      <rect x="14" y="6" width="4" height="2" fill="currentColor" />
      <rect x="13" y="17" width="6" height="5" fill="currentColor" opacity=".68" />
      <rect x="25" y="8" width="2" height="2" fill="currentColor" />
    </svg>
  )
}
