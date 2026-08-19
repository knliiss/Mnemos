import { ApiError, NetworkError } from "@/shared/api/httpClient"
import { AppError } from "./AppError"
import { errorEvents, type ErrorEventPayload } from "./errorEventBus"

function formatApiError(error: ApiError): string {
  const body = error.body
  if (typeof body === "string") {
    return body
  }

  const lines = [`HTTP ${error.status}`]

  if (body.code) {
    lines.push(`Code: ${body.code}`)
  }
  if (body.message) {
    lines.push(`Message: ${body.message}`)
  }
  if (body.path) {
    lines.push(`Path: ${body.path}`)
  }
  if (body.requestId) {
    lines.push(`Request ID: ${body.requestId}`)
  }
  if (body.details && Object.keys(body.details).length) {
    lines.push(`Details: ${JSON.stringify(body.details)}`)
  }

  return lines.join("\n")
}

function toErrorEventPayload(error: unknown, normalized: AppError): ErrorEventPayload {
  if (error instanceof ApiError) {
    const body = error.body

    if (typeof body === "string") {
      return {
        message: body,
        statusCode: error.status,
      }
    }

    return {
      code: body.code,
      message: body.message,
      statusCode: body.statusCode ?? error.status,
      details: body.details,
      path: body.path,
      requestId: body.requestId,
    }
  }

  if (error instanceof NetworkError || normalized.kind === "network") {
    return {
      code: "NETWORK_UNAVAILABLE",
      message: normalized.message,
      statusCode: 503,
    }
  }

  if (normalized.kind === "auth") {
    return {
      code: "AUTHENTICATION_REQUIRED",
      message: normalized.message,
      statusCode: 401,
    }
  }

  if (normalized.kind === "validation") {
    return {
      code: "CLIENT_VALIDATION_FAILED",
      message: normalized.message,
      statusCode: 400,
    }
  }

  return {
    code: "CLIENT_UNEXPECTED_ERROR",
    message: normalized.message,
    statusCode: 500,
  }
}

export function normalizeError(error: unknown): AppError {
  if (error instanceof AppError) {
    return error
  }
  if (error instanceof ApiError) {
    return new AppError("api", formatApiError(error), error)
  }
  if (error instanceof NetworkError) {
    return new AppError("network", error.message, error)
  }
  if (error instanceof Error) {
    return new AppError("unexpected", error.message || "Unknown error", error)
  }

  return new AppError("unexpected", String(error), error)
}

export function handleError(error: unknown): AppError {
  const normalized = normalizeError(error)

  console.error(normalized, normalized.cause)
  errorEvents.publish(toErrorEventPayload(error, normalized))

  return normalized
}

export async function executeAction<T>(action: () => Promise<T>): Promise<T | undefined> {
  try {
    return await action()
  } catch (error) {
    handleError(error)
    return undefined
  }
}

export function installGlobalErrorHandlers() {
  window.addEventListener("error", (event) => handleError(event.error ?? event.message))
  window.addEventListener("unhandledrejection", (event) => {
    event.preventDefault()
    handleError(event.reason)
  })
}
