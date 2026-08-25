import type { EventKind } from "../../model"

type EventKindMicroArtProps = {
  kind: "ALL" | EventKind
}

export function EventKindMicroArt({ kind }: EventKindMicroArtProps) {
  if (kind === "ALL") {
    return (
      <svg className="event-kind-micro-art" viewBox="0 0 34 24" aria-hidden="true">
        <rect className="is-dim" x="3" y="5" width="6" height="6" rx="1" />
        <rect x="14" y="2" width="7" height="7" rx="1" />
        <rect className="is-soft" x="24" y="8" width="7" height="7" rx="1" />
        <rect className="is-dim" x="9" y="15" width="7" height="6" rx="1" />
        <rect x="20" y="16" width="6" height="5" rx="1" />
      </svg>
    )
  }

  if (kind === "ITEM_DROP") {
    return (
      <svg className="event-kind-micro-art" viewBox="0 0 34 24" aria-hidden="true">
        <rect className="is-soft" x="3" y="17" width="9" height="3" />
        <rect x="10" y="13" width="4" height="4" />
        <rect x="13" y="9" width="4" height="4" />
        <rect x="16" y="5" width="4" height="4" />
        <rect className="is-highlight" x="19" y="2" width="5" height="5" />
        <rect className="is-dim" x="23" y="15" width="8" height="5" rx="1" />
      </svg>
    )
  }

  if (kind === "BOOSTER") {
    return (
      <svg className="event-kind-micro-art" viewBox="0 0 34 24" aria-hidden="true">
        <rect className="is-dim" x="5" y="6" width="8" height="13" rx="2" />
        <rect className="is-soft" x="8" y="3" width="14" height="4" rx="1" />
        <rect x="18" y="5" width="6" height="6" />
        <rect x="15" y="10" width="6" height="5" />
        <rect className="is-highlight" x="12" y="14" width="6" height="6" />
        <rect className="is-soft" x="24" y="16" width="5" height="3" />
      </svg>
    )
  }

  if (kind === "GLOBAL") {
    return (
      <svg className="event-kind-micro-art" viewBox="0 0 34 24" aria-hidden="true">
        <rect className="is-dim" x="4" y="11" width="4" height="4" />
        <rect className="is-soft" x="9" y="6" width="4" height="4" />
        <rect x="14" y="3" width="10" height="4" />
        <rect x="12" y="7" width="14" height="10" rx="2" />
        <rect className="is-highlight" x="16" y="9" width="7" height="6" rx="1" />
        <rect className="is-soft" x="26" y="8" width="4" height="4" />
        <rect className="is-dim" x="25" y="17" width="5" height="3" />
      </svg>
    )
  }

  return (
    <svg className="event-kind-micro-art" viewBox="0 0 34 24" aria-hidden="true">
      <rect className="is-dim" x="3" y="16" width="28" height="5" rx="1" />
      <rect x="6" y="10" width="7" height="7" />
      <rect x="21" y="10" width="7" height="7" />
      <rect className="is-soft" x="12" y="6" width="10" height="11" />
      <rect className="is-highlight" x="15" y="3" width="4" height="5" />
      <rect className="is-dim" x="8" y="12" width="3" height="3" />
      <rect className="is-dim" x="23" y="12" width="3" height="3" />
    </svg>
  )
}
