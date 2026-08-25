import { useSyncExternalStore } from "react"
import type { GlobalEventType } from "@/ui/model"
import type { GameEventBroadcast } from "./realtime.types"

export const GLOBAL_EVENT_LIFETIME_MS = 5 * 60 * 1_000
const MAX_SIMULTANEOUS_GLOBAL_EVENTS = 3
const supportedGlobalEventTypes: GlobalEventType[] = ["DARKNESS", "MOON", "ECLIPSE", "EXPLOSION"]

export type ActiveGlobalEvent = {
  eventId: string
  type: GlobalEventType
  startedAt: string
  expiresAt: string
}

type Listener = () => void

let activeEvents: ActiveGlobalEvent[] = []
let expiryTimer: number | null = null
const listeners = new Set<Listener>()

export function ingestGlobalEventBroadcast(broadcast: GameEventBroadcast) {
  const global = broadcast.payload.global

  if (
    broadcast.payload.eventType !== "GLOBAL_STARTED" ||
    !global ||
    !isGlobalEventType(global.type)
  ) {
    return
  }

  const nextEvent = createActiveGlobalEvent(
    broadcast.payload.eventId,
    global.type,
    broadcast.payload.occurredAt,
  )

  if (!nextEvent) {
    return
  }

  setActiveEvents(mergeActiveGlobalEvents(activeEvents, [nextEvent], Date.now()))
}

export function hydrateGlobalEventPresence(events: ActiveGlobalEvent[]) {
  setActiveEvents(mergeActiveGlobalEvents(activeEvents, events, Date.now()))
}

export function createActiveGlobalEvent(
  eventId: string,
  type: GlobalEventType,
  startedAt: string,
): ActiveGlobalEvent | null {
  const startedAtMs = Date.parse(startedAt)

  if (!Number.isFinite(startedAtMs)) {
    return null
  }

  return {
    eventId,
    type,
    startedAt,
    expiresAt: new Date(startedAtMs + GLOBAL_EVENT_LIFETIME_MS).toISOString(),
  }
}

export function useActiveGlobalEvents(): ActiveGlobalEvent[] {
  return useSyncExternalStore(subscribe, getSnapshot, getSnapshot)
}

export function reduceActiveGlobalEvents(
  current: ActiveGlobalEvent[],
  nextEvent: ActiveGlobalEvent,
  now: number,
): ActiveGlobalEvent[] {
  return mergeActiveGlobalEvents(current, [nextEvent], now)
}

export function mergeActiveGlobalEvents(
  current: ActiveGlobalEvent[],
  incoming: ActiveGlobalEvent[],
  now: number,
): ActiveGlobalEvent[] {
  const newestByType = new Map<GlobalEventType, ActiveGlobalEvent>()

  for (const event of [...current, ...incoming]) {
    if (Date.parse(event.expiresAt) <= now) {
      continue
    }

    const existing = newestByType.get(event.type)

    if (!existing || Date.parse(event.startedAt) > Date.parse(existing.startedAt)) {
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
  const nextExpiry = Math.min(...activeEvents.map((event) => Date.parse(event.expiresAt)))
  const delay = Math.max(0, nextExpiry - now) + 10

  expiryTimer = window.setTimeout(() => {
    expiryTimer = null
    const currentTime = Date.now()

    setActiveEvents(activeEvents.filter((event) => Date.parse(event.expiresAt) > currentTime))
  }, delay)
}

function sameEvents(left: ActiveGlobalEvent[], right: ActiveGlobalEvent[]): boolean {
  return (
    left.length === right.length &&
    left.every(
      (event, index) =>
        event.eventId === right[index]?.eventId && event.expiresAt === right[index]?.expiresAt,
    )
  )
}

function isGlobalEventType(value: string): value is GlobalEventType {
  return supportedGlobalEventTypes.includes(value as GlobalEventType)
}
