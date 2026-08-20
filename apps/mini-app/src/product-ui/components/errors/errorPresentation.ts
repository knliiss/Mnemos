import { i18n } from "@/shared/i18n"
import type { ErrorMascotVariant } from "./ErrorMascot"

export type BackendErrorPayload = {
  code?: string | null
  message?: string | null
  statusCode?: number | null
  details?: Record<string, unknown> | null
  path?: string | null
  requestId?: string | null
}

export type NoticeTone = ErrorMascotVariant

export type NoticePresentation = {
  tone: NoticeTone
  mascot: ErrorMascotVariant
  eyebrow: string
  title: string
  message: string
}

const toneOverrides: Record<string, NoticeTone> = {
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

const translationKeys = {
  validation: {
    eyebrow: "errorNotifications.tones.validation.eyebrow",
    title: "errorNotifications.tones.validation.title",
    message: "errorNotifications.tones.validation.message",
  },
  access: {
    eyebrow: "errorNotifications.tones.access.eyebrow",
    title: "errorNotifications.tones.access.title",
    message: "errorNotifications.tones.access.message",
  },
  conflict: {
    eyebrow: "errorNotifications.tones.conflict.eyebrow",
    title: "errorNotifications.tones.conflict.title",
    message: "errorNotifications.tones.conflict.message",
  },
  "not-found": {
    eyebrow: "errorNotifications.tones.notFound.eyebrow",
    title: "errorNotifications.tones.notFound.title",
    message: "errorNotifications.tones.notFound.message",
  },
  waiting: {
    eyebrow: "errorNotifications.tones.waiting.eyebrow",
    title: "errorNotifications.tones.waiting.title",
    message: "errorNotifications.tones.waiting.message",
  },
  internal: {
    eyebrow: "errorNotifications.tones.internal.eyebrow",
    title: "errorNotifications.tones.internal.title",
    message: "errorNotifications.tones.internal.message",
  },
} as const satisfies Record<NoticeTone, Record<"eyebrow" | "title" | "message", string>>

function normalizeCode(code?: string | null) {
  return code?.trim().toUpperCase() || null
}

function inferTone(payload: BackendErrorPayload): NoticeTone {
  const code = normalizeCode(payload.code)
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

export function resolveErrorPresentation(
  payload: BackendErrorPayload,
  language = i18n.resolvedLanguage ?? "uk",
): NoticePresentation {
  const tone = inferTone(payload)
  const keys = translationKeys[tone]
  const translate = i18n.getFixedT(language)

  return {
    tone,
    mascot: tone,
    eyebrow: translate(keys.eyebrow),
    title: translate(keys.title),
    message: translate(keys.message),
  }
}
