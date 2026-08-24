import { useMemo } from "react"
import { useTranslation } from "react-i18next"
import type { Wipe } from "@/shared/api/wipeApi"

type LeaderboardPeriodSelectorProps = {
  wipes: Wipe[]
  wipeId: string | null
  selectedWipe: Wipe | null
  locale: string
  onChange: (wipeId: string) => void
}

export function LeaderboardPeriodSelector({
  wipes,
  wipeId,
  selectedWipe,
  locale,
  onChange,
}: LeaderboardPeriodSelectorProps) {
  const { t } = useTranslation()
  const dateFormatter = useMemo(
    () => new Intl.DateTimeFormat(locale, { dateStyle: "medium" }),
    [locale],
  )

  function wipeName(wipe: Wipe): string {
    if (wipe.name === "Legacy") {
      return t("leaderboards.legacyWipe")
    }

    return wipe.name ?? t("leaderboards.wipeFallback")
  }

  return (
    <section className="leaderboard-period">
      <div className="leaderboard-section-heading">
        <div>
          <span>{t("leaderboards.wipe")}</span>
          <strong>{t("leaderboards.periodTitle")}</strong>
        </div>

        {selectedWipe ? (
          <small>
            {t("leaderboards.periodStarted", {
              date: dateFormatter.format(new Date(selectedWipe.startedAt)),
            })}
          </small>
        ) : null}
      </div>

      {wipes.length > 0 ? (
        <div
          className="leaderboard-choice-row leaderboard-choice-row--wipes"
          role="group"
          aria-label={t("leaderboards.periodTitle")}
        >
          {wipes.map((wipe) => {
            const active = wipe.id === wipeId

            return (
              <button
                key={wipe.id}
                type="button"
                className={active ? "is-active" : ""}
                aria-pressed={active}
                onClick={() => onChange(wipe.id)}
              >
                <b>#{wipe.number}</b>
                <span>{wipeName(wipe)}</span>
                {wipe.active ? <i>{t("leaderboards.activeWipe")}</i> : null}
              </button>
            )
          })}
        </div>
      ) : (
        <small className="leaderboard-period__empty">{t("leaderboards.noWipes")}</small>
      )}
    </section>
  )
}
