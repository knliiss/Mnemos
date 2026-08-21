import { useCallback, useState } from "react"
import { useTranslation } from "react-i18next"
import { ErrorMascot } from "@/ui/components/errors/ErrorMascot"
import "./service-unavailable.css"

export type ServiceUnavailableReason = "service" | "session" | "auth"

type Props = {
  reason: ServiceUnavailableReason
  onRetry?: () => Promise<void> | void
}

type Copy = {
  eyebrow: string
  title: string
  description: string
  hint?: string
  retry: string
  retrying: string
}

const russianFallback: Record<ServiceUnavailableReason, Copy> = {
  service: {
    eyebrow: "СВЯЗЬ ПОТЕРЯНА",
    title: "Mnemos временно недоступен",
    description:
      "Сервисы Mnemos сейчас не отвечают. Мы автоматически попробуем подключиться снова.",
    retry: "Попробовать снова",
    retrying: "Проверяем соединение...",
  },
  session: {
    eyebrow: "СЕССИЯ",
    title: "Сессию Telegram нужно обновить",
    description:
      "Мы не отправляем устаревшие данные авторизации. Сначала Mnemos попробует восстановить существующую сессию.",
    hint: "Если сообщение не исчезает, закройте Mini App и откройте её снова в Telegram.",
    retry: "Проверить снова",
    retrying: "Восстанавливаем сессию...",
  },
  auth: {
    eyebrow: "АВТОРИЗАЦИЯ",
    title: "Не удалось войти в Mnemos",
    description: "Авторизацию не удалось завершить. Попробуйте ещё раз.",
    retry: "Повторить",
    retrying: "Повторяем авторизацию...",
  },
}

const translationPrefix: Record<ServiceUnavailableReason, string> = {
  service: "availability",
  session: "authRecovery",
  auth: "authFailure",
}

export function ServiceUnavailableScreen({ reason, onRetry }: Props) {
  const { t } = useTranslation()
  const [retrying, setRetrying] = useState(false)
  const fallback = russianFallback[reason]
  const prefix = translationPrefix[reason]

  const translate = useCallback(
    (key: keyof Copy, fallbackValue: string) => {
      const value = t(`${prefix}.${key}`, { defaultValue: fallbackValue })

      return typeof value === "string" && value.trim() ? value : fallbackValue
    },
    [prefix, t],
  )

  const handleRetry = useCallback(async () => {
    if (!onRetry || retrying) {
      return
    }

    setRetrying(true)

    try {
      await onRetry()
    } finally {
      setRetrying(false)
    }
  }, [onRetry, retrying])

  return (
    <main className="service-unavailable" role="alert" aria-live="polite">
      <section className="service-unavailable__panel">
        <div className="service-unavailable__illustration" aria-hidden="true">
          <span className="service-unavailable__signal service-unavailable__signal--one" />
          <span className="service-unavailable__signal service-unavailable__signal--two" />
          <span className="service-unavailable__signal service-unavailable__signal--three" />
          <ErrorMascot variant="service-unavailable" />
        </div>

        <div className="service-unavailable__copy">
          <span className="service-unavailable__eyebrow">
            {translate("eyebrow", fallback.eyebrow)}
          </span>
          <h1>{translate("title", fallback.title)}</h1>
          <p>{translate("description", fallback.description)}</p>

          {fallback.hint ? (
            <small>{translate("hint", fallback.hint)}</small>
          ) : (
            <small className="service-unavailable__automatic-retry" aria-hidden="true">
              <i />
              <i />
              <i />
            </small>
          )}
        </div>

        {onRetry ? (
          <button
            className="service-unavailable__retry"
            type="button"
            disabled={retrying}
            onClick={() => void handleRetry()}
          >
            {retrying
              ? translate("retrying", fallback.retrying)
              : translate("retry", fallback.retry)}
          </button>
        ) : null}
      </section>
    </main>
  )
}
