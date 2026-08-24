import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { listWipes, startWipe, type Wipe } from "@/shared/api/wipeApi"
import { handleError } from "@/shared/errors/errorHandler"
import { BottomSheet, PageHeader } from "../../shared/Primitives"
import "./wipe-admin.css"

function localDateValue(date: Date): string {
  const day = String(date.getDate()).padStart(2, "0")
  const month = String(date.getMonth() + 1).padStart(2, "0")
  const year = String(date.getFullYear())

  return `${day}.${month}.${year}`
}

function localTimeValue(date: Date): string {
  const hours = String(date.getHours()).padStart(2, "0")
  const minutes = String(date.getMinutes()).padStart(2, "0")

  return `${hours}:${minutes}`
}

function parseLocalDateTime(dateValue: string, timeValue: string): Date | null {
  const dateMatch = /^(\d{2})\.(\d{2})\.(\d{4})$/.exec(dateValue.trim())
  const timeMatch = /^(\d{2}):(\d{2})$/.exec(timeValue.trim())

  if (!dateMatch || !timeMatch) {
    return null
  }

  const day = Number(dateMatch[1])
  const month = Number(dateMatch[2])
  const year = Number(dateMatch[3])
  const hours = Number(timeMatch[1])
  const minutes = Number(timeMatch[2])

  if (hours > 23 || minutes > 59) {
    return null
  }

  const result = new Date(year, month - 1, day, hours, minutes, 0, 0)

  if (
    result.getFullYear() !== year ||
    result.getMonth() !== month - 1 ||
    result.getDate() !== day ||
    result.getHours() !== hours ||
    result.getMinutes() !== minutes
  ) {
    return null
  }

  return result
}

type StartValidation = "INVALID" | "FUTURE" | "BEFORE_CURRENT" | null

export function WipeAdmin({ back }: { back: () => void }) {
  const { t, i18n } = useTranslation()
  const initialStart = useMemo(() => new Date(), [])
  const [wipes, setWipes] = useState<Wipe[]>([])
  const [name, setName] = useState("")
  const [startDate, setStartDate] = useState(() => localDateValue(initialStart))
  const [startTime, setStartTime] = useState(() => localTimeValue(initialStart))
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [confirming, setConfirming] = useState(false)

  const locale =
    i18n.resolvedLanguage === "en" ? "en-US" : i18n.resolvedLanguage === "ru" ? "ru-RU" : "uk-UA"
  const dateTime = useMemo(
    () =>
      new Intl.DateTimeFormat(locale, {
        dateStyle: "medium",
        timeStyle: "short",
      }),
    [locale],
  )
  const current = wipes.find((wipe) => wipe.active) ?? null
  const parsedStart = useMemo(
    () => parseLocalDateTime(startDate, startTime),
    [startDate, startTime],
  )

  const startValidation = useMemo<StartValidation>(() => {
    if (!parsedStart) {
      return "INVALID"
    }

    if (parsedStart.getTime() > Date.now()) {
      return "FUTURE"
    }

    if (current && parsedStart.getTime() <= new Date(current.startedAt).getTime()) {
      return "BEFORE_CURRENT"
    }

    return null
  }, [current, parsedStart])

  const validationMessage =
    startValidation === "INVALID"
      ? t("wipeAdmin.invalidStart")
      : startValidation === "FUTURE"
        ? t("wipeAdmin.futureStart")
        : startValidation === "BEFORE_CURRENT"
          ? t("wipeAdmin.beforeCurrentStart")
          : null

  function wipeName(wipe: Wipe): string {
    if (wipe.name === "Legacy") {
      return t("leaderboards.legacyWipe")
    }

    return wipe.name ?? t("leaderboards.wipeFallback")
  }

  function useCurrentTime() {
    const now = new Date()

    setStartDate(localDateValue(now))
    setStartTime(localTimeValue(now))
  }

  async function refresh() {
    setLoading(true)

    try {
      setWipes(await listWipes())
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    let cancelled = false
    const frame = window.requestAnimationFrame(() => {
      setLoading(true)

      void listWipes()
        .then((result) => {
          if (!cancelled) {
            setWipes(result)
          }
        })
        .catch((error) => {
          if (!cancelled) {
            handleError(error)
          }
        })
        .finally(() => {
          if (!cancelled) {
            setLoading(false)
          }
        })
    })

    return () => {
      cancelled = true
      window.cancelAnimationFrame(frame)
    }
  }, [])

  async function createWipe() {
    if (!parsedStart || startValidation || saving) {
      return
    }

    setSaving(true)

    try {
      await startWipe({
        name: name.trim() || null,
        startedAt: parsedStart.toISOString(),
      })

      const now = new Date()

      setName("")
      setStartDate(localDateValue(now))
      setStartTime(localTimeValue(now))
      setConfirming(false)
      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="screen wipe-admin-screen">
      <PageHeader title={t("wipeAdmin.title")} eyebrow={t("wipeAdmin.eyebrow")} back={back} />

      <section className="wipe-admin-hero">
        <img src="/ui-illustrations/wipe-workspace.svg" alt="" aria-hidden="true" />
        <div>
          <strong>{t("wipeAdmin.heroTitle")}</strong>
          <small>{t("wipeAdmin.heroDescription")}</small>
        </div>
      </section>

      <section className="wipe-admin-current">
        <span>{t("wipeAdmin.current")}</span>
        {loading ? (
          <strong>{t("common.loading")}</strong>
        ) : current ? (
          <>
            <strong>
              #{current.number} {wipeName(current)}
            </strong>
            <small>
              {t("wipeAdmin.started", { date: dateTime.format(new Date(current.startedAt)) })}
            </small>
          </>
        ) : (
          <strong>{t("wipeAdmin.noCurrent")}</strong>
        )}
      </section>

      <form
        className="wipe-admin-form"
        onSubmit={(event) => {
          event.preventDefault()

          if (!startValidation) {
            setConfirming(true)
          }
        }}
      >
        <div className="wipe-admin-form__head">
          <strong>{t("wipeAdmin.newTitle")}</strong>
          <small>{t("wipeAdmin.newDescription")}</small>
        </div>

        <label>
          <span>{t("wipeAdmin.name")}</span>
          <input
            value={name}
            maxLength={64}
            placeholder={t("wipeAdmin.namePlaceholder")}
            onChange={(event) => setName(event.target.value)}
          />
        </label>

        <div className="wipe-admin-start">
          <div className="wipe-admin-start__head">
            <span>{t("wipeAdmin.startedAt")}</span>
            <button type="button" onClick={useCurrentTime}>
              {t("wipeAdmin.useNow")}
            </button>
          </div>

          <div className="wipe-admin-start__fields">
            <label>
              <span>{t("wipeAdmin.startDate")}</span>
              <input
                type="text"
                inputMode="numeric"
                autoComplete="off"
                maxLength={10}
                value={startDate}
                placeholder={t("wipeAdmin.startDatePlaceholder")}
                aria-invalid={Boolean(startValidation)}
                onChange={(event) => setStartDate(event.target.value)}
              />
            </label>

            <label>
              <span>{t("wipeAdmin.startTime")}</span>
              <input
                type="text"
                inputMode="numeric"
                autoComplete="off"
                maxLength={5}
                value={startTime}
                placeholder={t("wipeAdmin.startTimePlaceholder")}
                aria-invalid={Boolean(startValidation)}
                onChange={(event) => setStartTime(event.target.value)}
              />
            </label>
          </div>

          {validationMessage ? (
            <small className="wipe-admin-start__validation">{validationMessage}</small>
          ) : parsedStart ? (
            <small className="wipe-admin-start__preview">{dateTime.format(parsedStart)}</small>
          ) : null}
        </div>

        <button type="submit" disabled={Boolean(startValidation) || saving}>
          {t("wipeAdmin.start")}
        </button>
      </form>

      <section className="wipe-admin-history">
        <div className="wipe-admin-history__head">
          <strong>{t("wipeAdmin.history")}</strong>
          <small>{t("wipeAdmin.historyCount", { count: wipes.length })}</small>
        </div>

        {wipes.map((wipe) => (
          <article key={wipe.id} className={wipe.active ? "is-active" : ""}>
            <span className="wipe-admin-history__number">#{wipe.number}</span>
            <span className="wipe-admin-history__copy">
              <strong>{wipeName(wipe)}</strong>
              <small>
                {dateTime.format(new Date(wipe.startedAt))}
                {wipe.endedAt
                  ? ` — ${dateTime.format(new Date(wipe.endedAt))}`
                  : ` · ${t("wipeAdmin.active")}`}
              </small>
            </span>
          </article>
        ))}
      </section>

      {confirming && parsedStart ? (
        <BottomSheet title={t("wipeAdmin.confirmTitle")} onClose={() => setConfirming(false)}>
          <div className="wipe-admin-confirm">
            <img src="/ui-illustrations/wipe-workspace.svg" alt="" aria-hidden="true" />
            <p>{t("wipeAdmin.confirmDescription")}</p>
            <strong>{dateTime.format(parsedStart)}</strong>
            <div>
              <button type="button" onClick={() => setConfirming(false)} disabled={saving}>
                {t("common.cancel")}
              </button>
              <button
                type="button"
                className="is-danger"
                onClick={() => void createWipe()}
                disabled={saving}
              >
                {saving ? t("wipeAdmin.saving") : t("wipeAdmin.confirm")}
              </button>
            </div>
          </div>
        </BottomSheet>
      ) : null}
    </div>
  )
}
