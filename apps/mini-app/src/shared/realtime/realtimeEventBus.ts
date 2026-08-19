import type { RealtimeEvent, RealtimeEventListener } from "./realtime.types"

const listeners = new Set<RealtimeEventListener>()

export const realtimeEvents = {
  publish(event: RealtimeEvent) {
    listeners.forEach((listener) => listener(event))
  },

  subscribe(listener: RealtimeEventListener) {
    listeners.add(listener)

    return () => {
      listeners.delete(listener)
    }
  },
}
