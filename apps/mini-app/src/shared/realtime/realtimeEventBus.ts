import type {
  RealtimeBroadcast,
  RealtimeBroadcastListener,
  RealtimeEvent,
  RealtimeEventListener,
} from "./realtime.types"

const eventListeners = new Set<RealtimeEventListener>()
const broadcastListeners = new Set<RealtimeBroadcastListener>()

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
