import { getToken } from "@/features/auth/api/auth"
import { runtimeConfig } from "@/shared/config/runtimeConfig"
import { userBanEvents, type UserBanDetails } from "@/shared/session/userBanEvents"

export type { UserBanDetails } from "@/shared/session/userBanEvents"

export interface ApiErrorBody {
  message?: string
  statusCode?: number
  code?: string
  details?: Record<string, unknown>
  path?: string
  requestId?: string
  timestamp?: string
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly body: ApiErrorBody | string,
  ) {
    super(typeof body === "string" ? body : (body.message ?? `HTTP ${status}`))
    this.name = "ApiError"
  }
}

export class NetworkError extends Error {
  constructor(
    message: string,
    public readonly cause?: unknown,
  ) {
    super(message)
    this.name = "NetworkError"
  }
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await authenticatedResponse(path, options)

  if (response.status === 204) {
    return null as T
  }

  const text = await response.text()

  if (!text) {
    return null as T
  }

  try {
    return JSON.parse(text) as T
  } catch {
    throw new ApiError(response.status, "Server returned invalid JSON")
  }
}

export async function apiBlob(path: string, options: RequestInit = {}): Promise<Blob> {
  const response = await authenticatedResponse(path, options)

  return response.blob()
}

export function getUserBanDetails(error: unknown): UserBanDetails | null {
  if (!(error instanceof ApiError) || error.status !== 403 || typeof error.body === "string") {
    return null
  }

  if (error.body.code !== "USER_BANNED" || !error.body.details) {
    return null
  }

  const details = error.body.details
  const sanctionId = details.sanctionId
  const reason = details.reason
  const occurredAt = details.occurredAt
  const expiresAt = details.expiresAt

  if (
    typeof sanctionId !== "string" ||
    typeof reason !== "string" ||
    typeof occurredAt !== "string" ||
    (expiresAt !== undefined && expiresAt !== null && typeof expiresAt !== "string")
  ) {
    return null
  }

  return {
    sanctionId,
    reason,
    occurredAt,
    expiresAt: typeof expiresAt === "string" ? expiresAt : null,
  }
}

export function isServiceUnavailableError(error: unknown): boolean {
  return error instanceof NetworkError || (error instanceof ApiError && error.status >= 500)
}

async function authenticatedResponse(path: string, options: RequestInit): Promise<Response> {
  const url = `${runtimeConfig.apiUrl}${path}`
  let token = await getToken()
  let response = await sendRequest(url, options, token)

  if (response.status === 401) {
    token = await getToken({ forceRefresh: true })
    response = await sendRequest(url, options, token)
  }

  if (!response.ok) {
    const error = new ApiError(response.status, await readResponseBody(response))
    const ban = getUserBanDetails(error)

    if (ban) {
      userBanEvents.publish(ban)
    }

    throw error
  }

  return response
}

async function sendRequest(url: string, options: RequestInit, token: string): Promise<Response> {
  const headers = new Headers(options.headers)

  if (!headers.has("Accept")) {
    headers.set("Accept", "application/json")
  }

  headers.set("Authorization", `Bearer ${token}`)

  try {
    return await fetch(url, {
      ...options,
      headers,
    })
  } catch (error) {
    const reason = error instanceof Error ? `${error.name}: ${error.message}` : String(error)

    throw new NetworkError(
      `Network request failed.\n${options.method ?? "GET"} ${url}\nCause: ${reason}`,
      error,
    )
  }
}

async function readResponseBody(response: Response): Promise<ApiErrorBody | string> {
  const text = await response.text()

  if (!text) {
    return response.statusText || `HTTP ${response.status}`
  }

  try {
    return JSON.parse(text) as ApiErrorBody
  } catch {
    return text
  }
}
