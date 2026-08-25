import { useSyncExternalStore } from "react"
import type { GlobalEventType } from "@/ui/model"

const MAX_SIMULTANEOUS_GLOBAL_EVENTS = 3

export type ActiveGlobalEvent = {
  eventId: string
  type: GlobalEventType
  startedAt: string
  endsAt: string
}

type Listener = () => void

let activeEvents: ActiveGlobalEvent[] = []
let expiryTimer: number | null = null
const listeners = new Set<Listener>()

export function replaceGlobalEventPresence(events: ActiveGlobalEvent[]) {
  setActiveEvents(normalizeActiveGlobalEvents(events, Date.now()))
}

export function reconcileGlobalEventPresence(events: ActiveGlobalEvent[]) {
  setActiveEvents(reconcileActiveGlobalEvents(events, activeEvents, Date.now()))
}

export function useActiveGlobalEvents(): ActiveGlobalEvent[] {
  return useSyncExternalStore(subscribe, getSnapshot, getSnapshot)
}

export function reconcileActiveGlobalEvents(
  serverEvents: ActiveGlobalEvent[],
  currentEvents: ActiveGlobalEvent[],
  now: number,
): ActiveGlobalEvent[] {
  const normalizedServerEvents = normalizeActiveGlobalEvents(serverEvents, now)
  const serverTypes = new Set(normalizedServerEvents.map((event) => event.type))
  const unexpiredLocalEvents = normalizeActiveGlobalEvents(currentEvents, now).filter(
    (event) => !serverTypes.has(event.type),
  )

  return normalizeActiveGlobalEvents(
    [...normalizedServerEvents, ...unexpiredLocalEvents],
    now,
  )
}

export function normalizeActiveGlobalEvents(
  events: ActiveGlobalEvent[],
  now: number,
): ActiveGlobalEvent[] {
  const newestByType = new Map<GlobalEventType, ActiveGlobalEvent>()

  for (const event of events) {
    const startedAt = Date.parse(event.startedAt)
    const endsAt = Date.parse(event.endsAt)

    if (!Number.isFinite(startedAt) || !Number.isFinite(endsAt) || endsAt <= now) {
      continue
    }

    const existing = newestByType.get(event.type)

    if (!existing || startedAt > Date.parse(existing.startedAt)) {
      newestByType.set(event.type, event)
    }
  }

  return [...newestByType.values()]
    .sort((left, right) => Date.parse(right.startedAt) - Date.parse(left.startedAt))
    .slice(0, MAX_SIMULTANEOUS_GLOBAL_EVENTS)
}

function subscribe(listener: Listener): () => void {
  listeners.add(listener)

  return () => {
    listeners.delete(listener)
  }
}

function getSnapshot(): ActiveGlobalEvent[] {
  return activeEvents
}

function setActiveEvents(nextEvents: ActiveGlobalEvent[]) {
  if (sameEvents(activeEvents, nextEvents)) {
    scheduleExpiry()
    return
  }

  activeEvents = nextEvents
  scheduleExpiry()
  listeners.forEach((listener) => listener())
}

function scheduleExpiry() {
  if (expiryTimer !== null) {
    window.clearTimeout(expiryTimer)
    expiryTimer = null
  }

  if (!activeEvents.length) {
    return
  }

  const now = Date.now()
  const nextExpiry = Math.min(...activeEvents.map((event) => Date.parse(event.endsAt)))
  const delay = Math.max(0, nextExpiry - now) + 10

  expiryTimer = window.setTimeout(() => {
    expiryTimer = null
    setActiveEvents(normalizeActiveGlobalEvents(activeEvents, Date.now()))
  }, delay)
}

function sameEvents(left: ActiveGlobalEvent[], right: ActiveGlobalEvent[]): boolean {
  return (
    left.length === right.length &&
    left.every(
      (event, index) =>
        event.eventId === right[index]?.eventId &&
        event.startedAt === right[index]?.startedAt &&
        event.endsAt === right[index]?.endsAt,
    )
  )
}
