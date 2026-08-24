import { useTranslation } from "react-i18next"
import type { BetMode } from "./betPresentation"
import { CurrencyValue } from "../shared/Primitives"
import { Glyph } from "../shared/MnemosIcon"

type BetsOverviewProps = {
  nickname: string
  coin: number
  mode: BetMode
  availableRounds: number
  activeRaces: number
  myPositions: number
  onModeChange: (mode: BetMode) => void
  onWalletOpen: () => void
}

export function BetsOverview({
  nickname,
  coin,
  mode,
  availableRounds,
  activeRaces,
  myPositions,
  onModeChange,
  onWalletOpen,
}: BetsOverviewProps) {
  const { t } = useTranslation()

  return (
    <section className="bets-overview" aria-label={t("bets.overviewAria")}>
      <div className="bets-overview__head">
        <span className="bets-overview__copy">
          <small>{t("bets.eyebrow")}</small>
          <strong>{t("bets.title")}</strong>
          <span>{t("bets.subtitle", { nickname })}</span>
        </span>

        <span className="bets-overview__mark" aria-hidden="true">
          <Glyph name="bet" size={28} />
        </span>
      </div>

      <div className="bets-overview__summary">
        <button type="button" className="bets-overview__wallet" onClick={onWalletOpen}>
          <small>{t("bets.balance")}</small>
          <CurrencyValue currency="COIN" value={coin} compact />
        </button>

        <span>
          <small>{t("bets.availableRounds")}</small>
          <strong>{availableRounds}</strong>
        </span>

        <span>
          <small>{t("bets.activeRaces")}</small>
          <strong>{activeRaces}</strong>
        </span>
      </div>

      <nav className="bets-overview__modes" aria-label={t("bets.modeAria")}>
        <button
          type="button"
          className={mode === "EVENTS" ? "is-active" : ""}
          onClick={() => onModeChange("EVENTS")}
        >
          {t("bets.modes.EVENTS")}
        </button>

        <button
          type="button"
          className={mode === "MY" ? "is-active" : ""}
          onClick={() => onModeChange("MY")}
        >
          <span>{t("bets.modes.MY")}</span>
          {myPositions ? <b>{myPositions}</b> : null}
        </button>
      </nav>
    </section>
  )
}
