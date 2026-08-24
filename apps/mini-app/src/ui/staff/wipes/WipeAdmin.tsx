import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { listWipes, startWipe, type Wipe } from "@/shared/api/wipeApi"
import { handleError } from "@/shared/errors/errorHandler"
import { BottomSheet, PageHeader } from "../../shared/Primitives"
import "./wipe-admin.css"

function localDateTimeValue(date: Date): string {
  const offset = date.getTimezoneOffset() * 60_000

  return new Date(date.getTime() - offset).toISOString().slice(0, 16)
}

export function WipeAdmin({ back }: { back: () => void }) {
  const { t, i18n } = useTranslation()
  const [wipes, setWipes] = useState<Wipe[]>([])
  const [name, setName] = useState("")
  const [startedAt, setStartedAt] = useState(() => localDateTimeValue(new Date()))
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
    void refresh()
  }, [])

  async function createWipe() {
    if (!startedAt || saving) {
      return
    }

    setSaving(true)

    try {
      await startWipe({
        name: name.trim() || null,
        startedAt: new Date(startedAt).toISOString(),
      })

      setName("")
      setStartedAt(localDateTimeValue(new Date()))
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
            <strong>#{current.number} {current.name ?? t("leaderboards.wipeFallback")}</strong>
            <small>{t("wipeAdmin.started", { date: dateTime.format(new Date(current.startedAt)) })}</small>
          </>
        ) : (
          <strong>{t("wipeAdmin.noCurrent")}</strong>
        )}
      </section>

      <form
        className="wipe-admin-form"
        onSubmit={(event) => {
          event.preventDefault()
          setConfirming(true)
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

        <label>
          <span>{t("wipeAdmin.startedAt")}</span>
          <input
            type="datetime-local"
            required
            value={startedAt}
            max={localDateTimeValue(new Date())}
            onChange={(event) => setStartedAt(event.target.value)}
          />
        </label>

        <button type="submit" disabled={!startedAt || saving}>
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
              <strong>{wipe.name ?? t("leaderboards.wipeFallback")}</strong>
              <small>
                {dateTime.format(new Date(wipe.startedAt))}
                {wipe.endedAt ? ` — ${dateTime.format(new Date(wipe.endedAt))}` : ` · ${t("wipeAdmin.active")}`}
              </small>
            </span>
          </article>
        ))}
      </section>

      {confirming ? (
        <BottomSheet title={t("wipeAdmin.confirmTitle")} onClose={() => setConfirming(false)}>
          <div className="wipe-admin-confirm">
            <img src="/ui-illustrations/wipe-workspace.svg" alt="" aria-hidden="true" />
            <p>{t("wipeAdmin.confirmDescription")}</p>
            <strong>{new Date(startedAt).toLocaleString(locale)}</strong>
            <div>
              <button type="button" onClick={() => setConfirming(false)} disabled={saving}>
                {t("common.cancel")}
              </button>
              <button type="button" className="is-danger" onClick={() => void createWipe()} disabled={saving}>
                {saving ? t("wipeAdmin.saving") : t("wipeAdmin.confirm")}
              </button>
            </div>
          </div>
        </BottomSheet>
      ) : null}
    </div>
  )
}
