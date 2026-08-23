import { ApiError, NetworkError } from "@/shared/api/httpClient"
import { AppError } from "./AppError"
import { errorEvents, type ErrorEventPayload } from "./errorEventBus"
import { resolveUserFacingErrorMessage } from "./userFacingErrorMessage"

function apiErrorPayload(error: ApiError): ErrorEventPayload {
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

function toErrorEventPayload(error: unknown, normalized: AppError): ErrorEventPayload {
  if (error instanceof ApiError) {
    return apiErrorPayload(error)
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

function normalizeError(error: unknown): AppError {
  if (error instanceof AppError) {
    return error
  }

  if (error instanceof ApiError) {
    const payload = apiErrorPayload(error)

    return new AppError("api", resolveUserFacingErrorMessage(payload), error)
  }

  if (error instanceof NetworkError) {
    return new AppError(
      "network",
      resolveUserFacingErrorMessage({
        code: "NETWORK_UNAVAILABLE",
        statusCode: 503,
      }),
      error,
    )
  }

  return new AppError(
    "unexpected",
    resolveUserFacingErrorMessage({
      code: "CLIENT_UNEXPECTED_ERROR",
      statusCode: 500,
    }),
    error,
  )
}

export function handleError(error: unknown): AppError {
  const normalized = normalizeError(error)

  console.error(normalized, normalized.cause)
  errorEvents.publish(toErrorEventPayload(error, normalized))

  return normalized
}

export function installGlobalErrorHandlers() {
  window.addEventListener("error", (event) => handleError(event.error ?? event.message))
  window.addEventListener("unhandledrejection", (event) => {
    event.preventDefault()
    handleError(event.reason)
  })
}
