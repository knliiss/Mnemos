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
  const selectedIndex = wipes.findIndex((wipe) => wipe.id === wipeId)
  const previousWipe = selectedIndex > 0 ? wipes[selectedIndex - 1] : null
  const nextWipe = selectedIndex >= 0 && selectedIndex < wipes.length - 1 ? wipes[selectedIndex + 1] : null

  function wipeName(wipe: Wipe): string {
    if (wipe.name === "Legacy") {
      return t("leaderboards.legacyWipe")
    }

    return wipe.name ?? t("leaderboards.wipeFallback")
  }

  if (wipes.length === 0 || !selectedWipe) {
    return <span className="leaderboard-period-switcher__empty">{t("leaderboards.noWipes")}</span>
  }

  return (
    <section className="leaderboard-period-switcher" aria-label={t("leaderboards.periodTitle")}>
      <button
        type="button"
        className="leaderboard-period-switcher__arrow"
        disabled={!previousWipe}
        aria-label={t("leaderboards.previousPeriod")}
        onClick={() => previousWipe && onChange(previousWipe.id)}
      >
        ‹
      </button>

      <div className="leaderboard-period-switcher__content">
        <span>{t("leaderboards.periodTitle")}</span>
        <strong>
          <b>#{selectedWipe.number}</b>
          {wipeName(selectedWipe)}
          {selectedWipe.active ? <i>{t("leaderboards.activeWipe")}</i> : null}
        </strong>
        <small>
          {t("leaderboards.periodStarted", {
            date: dateFormatter.format(new Date(selectedWipe.startedAt)),
          })}
        </small>
      </div>

      <button
        type="button"
        className="leaderboard-period-switcher__arrow"
        disabled={!nextWipe}
        aria-label={t("leaderboards.nextPeriod")}
        onClick={() => nextWipe && onChange(nextWipe.id)}
      >
        ›
      </button>
    </section>
  )
}
