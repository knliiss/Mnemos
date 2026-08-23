import { i18n } from "@/shared/i18n"
import { inferErrorTone, normalizeErrorCode, type ErrorTone } from "./errorClassification"
import type { ErrorEventPayload } from "./errorEventBus"

const toneMessageKeys = {
  validation: "errorNotifications.tones.validation.message",
  access: "errorNotifications.tones.access.message",
  conflict: "errorNotifications.tones.conflict.message",
  "not-found": "errorNotifications.tones.notFound.message",
  waiting: "errorNotifications.tones.waiting.message",
  internal: "errorNotifications.tones.internal.message",
} as const satisfies Record<ErrorTone, string>

export function resolveUserFacingErrorMessage(
  payload: ErrorEventPayload,
  language = i18n.resolvedLanguage ?? "uk",
): string {
  const translate = i18n.getFixedT(language)
  const code = normalizeErrorCode(payload.code)

  if (code) {
    const translatedCodeMessage = translate(`errorNotifications.codes.${code}.message`, {
      defaultValue: "",
    }).trim()

    if (translatedCodeMessage) {
      return translatedCodeMessage
    }
  }

  const tone = inferErrorTone(payload)

  return translate(toneMessageKeys[tone])
}
