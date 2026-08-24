import type {
  RealtimeBroadcast,
  RealtimeBroadcastListener,
  RealtimeEvent,
  RealtimeEventListener,
} from "./realtime.types"

export type RealtimeConnectionState = "CONNECTED" | "DISCONNECTED"

const eventListeners = new Set<RealtimeEventListener>()
const broadcastListeners = new Set<RealtimeBroadcastListener>()
const connectionListeners = new Set<(state: RealtimeConnectionState) => void>()

export const realtimeEvents = {
  publish(event: RealtimeEvent) {
    eventListeners.forEach((listener) => listener(event))
  },

  subscribe(listener: RealtimeEventListener) {
    eventListeners.add(listener)

    return () => {
      eventListeners.delete(listener)
    }
  },
}

export const realtimeBroadcasts = {
  publish(event: RealtimeBroadcast) {
    broadcastListeners.forEach((listener) => listener(event))
  },

  subscribe(listener: RealtimeBroadcastListener) {
    broadcastListeners.add(listener)

    return () => {
      broadcastListeners.delete(listener)
    }
  },
}

export const realtimeConnectionEvents = {
  publish(state: RealtimeConnectionState) {
    connectionListeners.forEach((listener) => listener(state))
  },

  subscribe(listener: (state: RealtimeConnectionState) => void) {
    connectionListeners.add(listener)

    return () => {
      connectionListeners.delete(listener)
    }
  },
}
