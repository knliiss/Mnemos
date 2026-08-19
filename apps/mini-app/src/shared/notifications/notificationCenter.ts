import type { AppNotification, NotificationKind } from "./notification.types"

type Listener = (notification: AppNotification | null) => void
const listeners = new Set<Listener>()
let current: AppNotification | null = null

function publish(kind: NotificationKind, message: string, persistent = kind === "error") {
  current = { id: crypto.randomUUID(), kind, message, persistent }
  listeners.forEach((listener) => listener(current))
}

export const notifications = {
  info(message: string) {
    publish("info", message)
  },
  success(message: string) {
    publish("success", message)
  },
  warning(message: string) {
    publish("warning", message)
  },
  error(message: string) {
    publish("error", message, true)
  },
  clear() {
    current = null
    listeners.forEach((listener) => listener(null))
  },
  subscribe(listener: Listener) {
    listeners.add(listener)
    listener(current)
    return () => {
      listeners.delete(listener)
    }
  },
}
