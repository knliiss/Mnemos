import type { PlayerPresenceStatus } from "@/shared/api/playerDirectoryApi"

export function presenceStatusClassName(status: PlayerPresenceStatus): string {
  return `player-presence-status player-presence-status--${status.toLocaleLowerCase()}`
}

export function canSubscribeToPresence(status: PlayerPresenceStatus): boolean {
  return status === "ONLINE" || status === "OFFLINE"
}

export function formatPresenceTimestamp(value: string | null, locale: string): string | null {
  if (!value) {
    return null
  }

  const timestamp = Date.parse(value)

  if (!Number.isFinite(timestamp)) {
    return null
  }

  return new Intl.DateTimeFormat(locale, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(timestamp))
}

export function formatPresenceFreshness(value: string | null, now = Date.now()): number | null {
  if (!value) {
    return null
  }

  const timestamp = Date.parse(value)

  if (!Number.isFinite(timestamp)) {
    return null
  }

  return Math.max(0, Math.floor((now - timestamp) / 1000))
}
