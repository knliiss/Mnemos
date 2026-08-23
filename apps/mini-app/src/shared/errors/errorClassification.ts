import type { ErrorEventPayload } from "./errorEventBus"

export type ErrorTone = "validation" | "access" | "conflict" | "not-found" | "waiting" | "internal"

const toneOverrides: Record<string, ErrorTone> = {
  INVALID_AUTHENTICATION_PRINCIPAL: "access",
  ACCOUNT_MEMBER_KICK_FORBIDDEN: "access",
  ACTIVE_ACCOUNT_NOT_SET: "access",
  ACTIVE_ACCOUNT_REQUIRED: "access",
  ACCOUNT_DELETION_UNAVAILABLE: "conflict",
  ACCOUNT_INVITE_UNAVAILABLE: "conflict",
  ROLE_HIERARCHY_VIOLATION: "conflict",
  ROLE_HIERARCHY_INCONSISTENT: "conflict",
  SELF_SANCTION: "conflict",
  VALIDATION_FAILED: "validation",
  METHOD_NOT_ALLOWED: "validation",
  UNSUPPORTED_MEDIA_TYPE: "validation",
  NOT_ACCEPTABLE: "validation",
  CRISTALIX_AUTHORIZATION_FAILED: "waiting",
  CRISTALIX_INVALID_RESPONSE: "waiting",
  REALTIME_CONNECTION_LIMIT_EXCEEDED: "waiting",
}

export function normalizeErrorCode(code?: string | null): string | null {
  return code?.trim().toUpperCase() || null
}

export function inferErrorTone(payload: ErrorEventPayload): ErrorTone {
  const code = normalizeErrorCode(payload.code)
  const status = payload.statusCode ?? null

  if (code) {
    const override = toneOverrides[code]

    if (override) {
      return override
    }

    if (
      code.includes("ACCESS_DENIED") ||
      code.includes("PERMISSION_DENIED") ||
      code.includes("OWNER_REQUIRED") ||
      code.includes("UNAUTHORIZED") ||
      code === "AUTHENTICATION_REQUIRED" ||
      code === "ACCOUNT_NOT_ACTIVE"
    ) {
      return "access"
    }

    if (code.includes("NOT_FOUND")) {
      return "not-found"
    }

    if (
      code.includes("RATE_LIMIT") ||
      code.includes("IN_PROGRESS") ||
      code.includes("UNAVAILABLE") ||
      code.includes("CAPACITY")
    ) {
      return "waiting"
    }

    if (
      code.includes("CONFLICT") ||
      code.includes("ALREADY") ||
      code.includes("LIMIT_REACHED") ||
      code.includes("NOT_OWNED") ||
      code.includes("NOT_PURCHASABLE") ||
      code.includes("NOT_RECOVERABLE") ||
      code.includes("CANNOT_LEAVE")
    ) {
      return "conflict"
    }

    if (
      code.includes("INVALID") ||
      code.includes("MISSING") ||
      code.includes("VIOLATION") ||
      code.includes("EXCEEDED")
    ) {
      return "validation"
    }
  }

  if (status === 401 || status === 403) {
    return "access"
  }

  if (status === 404) {
    return "not-found"
  }

  if (status === 409) {
    return "conflict"
  }

  if (status === 429 || status === 502 || status === 503 || status === 504) {
    return "waiting"
  }

  if (status === 400 || status === 405 || status === 406 || status === 415 || status === 422) {
    return "validation"
  }

  return "internal"
}
