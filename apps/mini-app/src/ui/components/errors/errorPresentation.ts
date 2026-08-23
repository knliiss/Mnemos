import {
  inferErrorTone,
  normalizeErrorCode,
  type ErrorTone,
} from "@/shared/errors/errorClassification"
import type { ErrorEventPayload } from "@/shared/errors/errorEventBus"
import { i18n } from "@/shared/i18n"
import type { ErrorMascotVariant } from "./ErrorMascot"

export type BackendErrorPayload = ErrorEventPayload
export type NoticeTone = ErrorTone

export type NoticePresentation = {
  tone: NoticeTone
  mascot: ErrorMascotVariant
  eyebrow: string
  title: string
  message: string
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

export function resolveErrorPresentation(
  payload: BackendErrorPayload,
  language = i18n.resolvedLanguage ?? "uk",
): NoticePresentation {
  const code = normalizeErrorCode(payload.code)
  const tone = inferErrorTone(payload)
  const keys = translationKeys[tone]
  const translate = i18n.getFixedT(language)
  const fallback = {
    eyebrow: translate(keys.eyebrow),
    title: translate(keys.title),
    message: translate(keys.message),
  }

  if (!code) {
    return {
      tone,
      mascot: tone,
      ...fallback,
    }
  }

  const codePrefix = `errorNotifications.codes.${code}`

  return {
    tone,
    mascot: tone,
    eyebrow: translate(`${codePrefix}.eyebrow`, {
      defaultValue: fallback.eyebrow,
    }),
    title: translate(`${codePrefix}.title`, {
      defaultValue: fallback.title,
    }),
    message: translate(`${codePrefix}.message`, {
      defaultValue: fallback.message,
    }),
  }
}
