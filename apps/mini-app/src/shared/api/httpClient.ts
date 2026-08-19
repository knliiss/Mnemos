import { getToken } from "@/features/auth/api/auth"
import { runtimeConfig } from "@/shared/config/runtimeConfig"

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
  const token = await getToken()
  const url = `${runtimeConfig.apiUrl}${path}`
  let response: Response

  try {
    response = await fetch(url, {
      ...options,
      headers: {
        Accept: "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    })
  } catch (error) {
    const reason = error instanceof Error ? `${error.name}: ${error.message}` : String(error)

    throw new NetworkError(
      `Не удалось подключиться к серверу.\n${options.method ?? "GET"} ${url}\nПричина: ${reason}`,
      error,
    )
  }

  if (!response.ok) {
    throw new ApiError(response.status, await readResponseBody(response))
  }

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
    throw new ApiError(response.status, "Сервер вернул некорректный JSON")
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

export const request = api
