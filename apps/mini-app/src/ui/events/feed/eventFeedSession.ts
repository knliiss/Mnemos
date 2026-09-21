import type { EventItemType } from "@/shared/api/eventApi"
import type { BoosterType, EventKind, GlobalEventType, ItemRarity } from "../../model"

export type PersistedEventFeedState = {
  filter: "ALL" | EventKind
  rarity: "ALL" | ItemRarity
  itemType: "ALL" | EventItemType
  boosterType: "ALL" | BoosterType
  globalType: "ALL" | GlobalEventType
  raidLocation: number | null
  query: string
  sort: "NEWEST" | "OLDEST"
  wipeId: string | null
}

const STORAGE_PREFIX = "mnemos:event-feed:"

function storageKey(accountId: string): string {
  return `${STORAGE_PREFIX}${accountId}`
}

export function readPersistedEventFeedState(accountId: string): PersistedEventFeedState | null {
  if (typeof window === "undefined") {
    return null
  }

  try {
    const raw = window.sessionStorage.getItem(storageKey(accountId))
    if (!raw) {
      return null
    }

    const value = JSON.parse(raw) as Partial<PersistedEventFeedState>
    if (
      typeof value.query !== "string" ||
      (value.sort !== "NEWEST" && value.sort !== "OLDEST") ||
      typeof value.filter !== "string"
    ) {
      return null
    }

    return value as PersistedEventFeedState
  } catch {
    return null
  }
}

export function persistEventFeedState(
  accountId: string,
  state: PersistedEventFeedState,
): void {
  if (typeof window === "undefined") {
    return
  }

  try {
    window.sessionStorage.setItem(storageKey(accountId), JSON.stringify(state))
  } catch {
    // Browsing must keep working even if session storage is unavailable.
  }
}
