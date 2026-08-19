import { getToken } from "@/features/auth/api/auth"
import { runtimeConfig } from "@/shared/config/runtimeConfig"
import { realtimeEvents } from "./realtimeEventBus"
import { parseRealtimeEvent } from "./realtimeEventParser"
import type { RealtimeEvent } from "./realtime.types"

const INITIAL_RECONNECT_DELAY_MS = 1_000
const MAX_RECONNECT_DELAY_MS = 15_000

class RealtimeClient {
  private abortController: AbortController | null = null

  start(): () => void {
    if (this.abortController) {
      return () => this.stop()
    }

    const controller = new AbortController()
    this.abortController = controller

    void this.connectUntilStopped(controller.signal)

    return () => this.stop()
  }

  private stop() {
    this.abortController?.abort()
    this.abortController = null
  }

  private async connectUntilStopped(signal: AbortSignal) {
    let reconnectDelay = INITIAL_RECONNECT_DELAY_MS

    while (!signal.aborted) {
      try {
        await this.connect(signal)
        reconnectDelay = INITIAL_RECONNECT_DELAY_MS
      } catch (error) {
        if (signal.aborted || isAbortError(error)) {
          return
        }
      }

      await waitForReconnect(withJitter(reconnectDelay), signal)
      reconnectDelay = Math.min(reconnectDelay * 2, MAX_RECONNECT_DELAY_MS)
    }
  }

  private async connect(signal: AbortSignal) {
    const token = await getToken()
    const storageKey = resolveStorageKey(token)
    const lastEventId = readLastEventId(storageKey)
    const response = await fetch(`${runtimeConfig.apiUrl}/api/v1/realtime/events`, {
      cache: "no-store",
      headers: {
        Accept: "text/event-stream",
        Authorization: `Bearer ${token}`,
        ...(lastEventId ? { "Last-Event-ID": lastEventId } : {}),
      },
      signal,
    })

    if (!response.ok) {
      throw new Error(`Realtime connection failed with HTTP ${response.status}`)
    }

    if (!response.body) {
      throw new Error("Realtime connection did not provide a response stream")
    }

    await consumeEventStream(
      response.body,
      (event) => {
        storeLastEventId(storageKey, event.id)
        realtimeEvents.publish(event)
      },
      signal,
    )
  }
}

async function consumeEventStream(
  stream: ReadableStream<Uint8Array>,
  onEvent: (event: RealtimeEvent) => void,
  signal: AbortSignal,
) {
  const reader = stream.getReader()
  const decoder = new TextDecoder()
  let buffer = ""
  let eventName = "message"
  let dataLines: string[] = []

  const dispatch = () => {
    if (eventName !== "connected" && dataLines.length > 0) {
      const event = parseRealtimeEvent(dataLines.join("\n"))

      if (event) {
        onEvent(event)
      }
    }

    eventName = "message"
    dataLines = []
  }

  try {
    while (!signal.aborted) {
      const { done, value } = await reader.read()

      if (done) {
        break
      }

      buffer += decoder.decode(value, { stream: true })

      let lineBreakIndex = buffer.indexOf("\n")

      while (lineBreakIndex >= 0) {
        const line = buffer.slice(0, lineBreakIndex).replace(/\r$/, "")
        buffer = buffer.slice(lineBreakIndex + 1)

        if (line === "") {
          dispatch()
        } else if (line.startsWith("event:")) {
          eventName = line.slice("event:".length).trimStart()
        } else if (line.startsWith("data:")) {
          dataLines.push(line.slice("data:".length).trimStart())
        }

        lineBreakIndex = buffer.indexOf("\n")
      }
    }

    dispatch()
  } finally {
    reader.releaseLock()
  }
}

function resolveStorageKey(token: string): string {
  const subject = resolveTokenSubject(token) ?? "current-user"
  return `mnemos-realtime-last-event-id:${subject}`
}

function resolveTokenSubject(token: string): string | null {
  try {
    const encodedPayload = token.split(".")[1]
    const normalizedPayload = encodedPayload
      .replace(/-/g, "+")
      .replace(/_/g, "/")
      .padEnd(Math.ceil(encodedPayload.length / 4) * 4, "=")
    const payload = JSON.parse(atob(normalizedPayload)) as { sub?: unknown }

    return typeof payload.sub === "string" ? payload.sub : null
  } catch {
    return null
  }
}

function readLastEventId(storageKey: string): string | null {
  try {
    return localStorage.getItem(storageKey)
  } catch {
    return null
  }
}

function storeLastEventId(storageKey: string, eventId: string) {
  try {
    localStorage.setItem(storageKey, eventId)
  } catch {
    // Replay is a resilience feature. A blocked storage API must not stop the live stream.
  }
}

function isAbortError(error: unknown): boolean {
  return error instanceof DOMException && error.name === "AbortError"
}

function waitForReconnect(delayMs: number, signal: AbortSignal): Promise<void> {
  return new Promise((resolve) => {
    let timeout: number | null = null

    const finish = () => {
      if (timeout !== null) {
        window.clearTimeout(timeout)
      }

      signal.removeEventListener("abort", finish)
      resolve()
    }

    if (signal.aborted) {
      finish()
      return
    }

    timeout = window.setTimeout(finish, delayMs)
    signal.addEventListener("abort", finish, { once: true })
  })
}

function withJitter(delayMs: number): number {
  const jitterMultiplier = 0.8 + Math.random() * 0.4
  return Math.round(delayMs * jitterMultiplier)
}

export const realtimeClient = new RealtimeClient()
