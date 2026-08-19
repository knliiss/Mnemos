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

export const errorEvents = {
  publish(payload: ErrorEventPayload) {
    listeners.forEach((listener) => listener(payload))
  },

  subscribe(listener: ErrorEventListener) {
    listeners.add(listener)

    return () => listeners.delete(listener)
  },
}
