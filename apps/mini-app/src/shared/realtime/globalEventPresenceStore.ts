import { useSyncExternalStore } from "react"
import type { GlobalEventType } from "@/ui/model"
import type { GameEventBroadcast } from "./realtime.types"

const GLOBAL_EVENT_LIFETIME_MS = 5 * 60 * 1_000
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

  const startedAtMs = Date.parse(broadcast.payload.occurredAt)

  if (!Number.isFinite(startedAtMs)) {
    return
  }

  const nextEvent: ActiveGlobalEvent = {
    eventId: broadcast.payload.eventId,
    type: global.type,
    startedAt: broadcast.payload.occurredAt,
    expiresAt: new Date(startedAtMs + GLOBAL_EVENT_LIFETIME_MS).toISOString(),
  }

  setActiveEvents(reduceActiveGlobalEvents(activeEvents, nextEvent, Date.now()))
}

export function useActiveGlobalEvents(): ActiveGlobalEvent[] {
  return useSyncExternalStore(subscribe, getSnapshot, getSnapshot)
}

export function reduceActiveGlobalEvents(
  current: ActiveGlobalEvent[],
  nextEvent: ActiveGlobalEvent,
  now: number,
): ActiveGlobalEvent[] {
  const stillActive = current.filter(
    (event) => Date.parse(event.expiresAt) > now && event.type !== nextEvent.type,
  )

  if (Date.parse(nextEvent.expiresAt) <= now) {
    return stillActive
  }

  return [...stillActive, nextEvent]
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
