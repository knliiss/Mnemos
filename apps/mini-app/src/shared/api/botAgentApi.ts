import { api } from "@/shared/api/httpClient"

export type BotAgent = {
  id: string
  name: string
  osUsername: string | null
  machineName: string | null
  fingerprint: string | null
  firstSeenAt: string | null
  lastSeenAt: string | null
  disconnectedAt: string | null
  online: boolean
  revoked: boolean
}
export type BotAccount = {
  id: string
  username: string
  state: string
  activity: string
  realm: string | null
  location: string | null
  x: number | null
  y: number | null
  z: number | null
  autoDungeon: boolean
  swapper: boolean
  autoReconnect: boolean
  targetRealm: number | null
  dungeons: string[]
  proxy: string
  proxyStatus: string
  proxyHealthy: boolean
  lastError: string | null
}
export type AgentCreation = { id: string; name: string; accessKey: string }
type AgentResult<T> = { status: number; body: T | { error?: string; message?: string } }

export const getBotAgents = () => api<BotAgent[]>("/api/v1/realtime/admin/bot-agents")
export const createBotAgent = () =>
  api<AgentCreation>("/api/v1/realtime/admin/bot-agents", { method: "POST" })
export const renameBotAgent = (id: string, name: string) =>
  api<{ ok: boolean }>(`/api/v1/realtime/admin/bot-agents/${id}/rename`, {
    method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ name }),
  })
export const revokeBotAgent = (id: string) =>
  api<{ ok: boolean }>(`/api/v1/realtime/admin/bot-agents/${id}`, { method: "DELETE" })

export async function workerRequest<T = unknown>(
  id: string, method: "GET" | "POST", path: string, body?: unknown,
): Promise<T> {
  const result = await api<AgentResult<T>>(`/api/v1/realtime/admin/bot-agents/${id}/request`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ method, path, body }),
  })
  if (result.status < 200 || result.status >= 300) {
    const error = result.body as { error?: string; message?: string }
    throw new Error(error.message ?? error.error ?? `C7x worker returned HTTP ${result.status}`)
  }
  return result.body as T
}
export const botAccounts = (id: string) =>
  workerRequest<BotAccount[]>(id, "GET", "/api/accounts")
export const botOverview = (id: string) =>
  workerRequest<Record<string, number | string | boolean>>(id, "GET", "/api/overview")
export const botAction = (id: string, accountId: string, action: string, extra = {}) =>
  workerRequest(id, "POST", `/api/account/${accountId}/action`, { action, ...extra })
export const botSettings = (id: string, accountId: string, settings: Record<string, unknown>) =>
  workerRequest(id, "POST", `/api/account/${accountId}/settings`, settings)
export const botManager = (id: string, data: Record<string, unknown>) =>
  workerRequest(id, "POST", "/api/manager", data)
export const botChat = (id: string, accountId: string, message: string) =>
  workerRequest(id, "POST", `/api/account/${accountId}/chat`, { message })
export const botLogs = (id: string, accountId: string, after = 0) =>
  workerRequest<{
    entries: { sequence: number; timestamp: string; category: string; message: string }[]
    lastSequence: number
  }>(id, "GET", `/api/account/${accountId}/logs?after=${after}&limit=80`)
