export type ErrorEventPayload = {
  code?: string | null
  message?: string | null
  statusCode?: number | null
  details?: Record<string, unknown> | null
  path?: string | null
  requestId?: string | null
}

type ErrorEventListener = (payload: ErrorEventPayload) => void

const listeners = new Set<ErrorEventListener>()
const pending: ErrorEventPayload[] = []
const MAX_PENDING_ERRORS = 2

export const errorEvents = {
  publish(payload: ErrorEventPayload) {
    if (!listeners.size) {
      pending.push(payload)

      if (pending.length > MAX_PENDING_ERRORS) {
        pending.splice(0, pending.length - MAX_PENDING_ERRORS)
      }

      return
    }

    listeners.forEach((listener) => listener(payload))
  },

  subscribe(listener: ErrorEventListener) {
    listeners.add(listener)

    if (pending.length) {
      const queued = pending.splice(0, pending.length)
      queued.forEach((payload) => listener(payload))
    }

    return () => {
      listeners.delete(listener)
    }
  },
}
