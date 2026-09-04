import { useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import type { UserBanDetails } from "@/shared/api/httpClient"
import "./ban-screen.css"

type Props = {
  ban: UserBanDetails
  onRefresh(): Promise<void> | void
}

type Countdown = {
  days: number
  hours: number
  minutes: number
  seconds: number
}

export function BanScreen({ ban, onRefresh }: Props) {
  const { i18n, t } = useTranslation()
  const refreshRequested = useRef(false)
  const [currentTime, setCurrentTime] = useState(() => Date.now())

  const countdown = useMemo(
    () => calculateCountdown(ban.expiresAt, currentTime),
    [ban.expiresAt, currentTime],
  )

  const expiresAtLabel = useMemo(() => {
    if (!ban.expiresAt) {
      return null
    }

    return new Intl.DateTimeFormat(i18n.resolvedLanguage ?? i18n.language, {
      dateStyle: "medium",
      timeStyle: "medium",
    }).format(new Date(ban.expiresAt))
  }, [ban.expiresAt, i18n.language, i18n.resolvedLanguage])

  useEffect(() => {
    refreshRequested.current = false

    if (!ban.expiresAt) {
      return
    }

    const timer = window.setInterval(() => {
      const now = Date.now()
      const next = calculateCountdown(ban.expiresAt, now)

      setCurrentTime(now)

      if (next && isFinished(next) && !refreshRequested.current) {
        refreshRequested.current = true
        void Promise.resolve(onRefresh())
      }
    }, 1_000)

    return () => {
      window.clearInterval(timer)
    }
  }, [ban.expiresAt, onRefresh])

  return (
    <main className="ban-screen" role="alert" aria-live="polite">
      <section className="ban-screen__panel">
        <div className="ban-screen__illustration" aria-hidden="true">
          <span className="ban-screen__tape ban-screen__tape--left" />
          <span className="ban-screen__tape ban-screen__tape--right" />
          <img src="/ui-illustrations/mnemos-mascot-police.svg" alt="" />
        </div>

        <div className="ban-screen__copy">
          <span className="ban-screen__eyebrow">{t("ban.eyebrow")}</span>
          <h1>{t("ban.title")}</h1>
          <p>{t("ban.description")}</p>
        </div>

        <dl className="ban-screen__details">
          <div className="ban-screen__detail">
            <dt>{t("ban.reason")}</dt>
            <dd>{ban.reason || t("ban.reasonUnavailable")}</dd>
          </div>

          <div className="ban-screen__detail">
            <dt>{t("ban.remaining")}</dt>
            <dd>
              {countdown ? (
                <CountdownView countdown={countdown} />
              ) : (
                <span className="ban-screen__permanent">{t("ban.permanent")}</span>
              )}
            </dd>
          </div>

          {expiresAtLabel ? (
            <div className="ban-screen__detail ban-screen__detail--compact">
              <dt>{t("ban.expiresAt")}</dt>
              <dd>{expiresAtLabel}</dd>
            </div>
          ) : null}
        </dl>

        <small className="ban-screen__hint">{t("ban.hint")}</small>
      </section>
    </main>
  )
}

function CountdownView({ countdown }: { countdown: Countdown }) {
  const { t } = useTranslation()
  const units = [
    [countdown.days, t("ban.days")],
    [countdown.hours, t("ban.hours")],
    [countdown.minutes, t("ban.minutes")],
    [countdown.seconds, t("ban.seconds")],
  ] as const

  return (
    <span className="ban-countdown" aria-label={t("ban.remaining")}>
      {units.map(([value, label]) => (
        <span className="ban-countdown__unit" key={label}>
          <strong>{String(value).padStart(2, "0")}</strong>
          <small>{label}</small>
        </span>
      ))}
    </span>
  )
}

function calculateCountdown(expiresAt: string | null, currentTime: number): Countdown | null {
  if (!expiresAt) {
    return null
  }

  const remainingSeconds = Math.max(0, Math.floor((Date.parse(expiresAt) - currentTime) / 1_000))
  const days = Math.floor(remainingSeconds / 86_400)
  const hours = Math.floor((remainingSeconds % 86_400) / 3_600)
  const minutes = Math.floor((remainingSeconds % 3_600) / 60)
  const seconds = remainingSeconds % 60

  return { days, hours, minutes, seconds }
}

function isFinished(countdown: Countdown): boolean {
  return countdown.days === 0 && countdown.hours === 0 && countdown.minutes === 0 && countdown.seconds === 0
}
