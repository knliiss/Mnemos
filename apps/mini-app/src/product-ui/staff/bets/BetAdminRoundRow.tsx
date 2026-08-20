import { useTranslation } from "react-i18next"
import type { EventBetRoundResponse } from "@/shared/api/betApi"
import { GlobalEventArt } from "../../components/events/EventArt"
import {
  betAdminEventLabel,
  formatBetAdminDate,
} from "./betAdminPresentation"

type Props = {
  round: EventBetRoundResponse
  onClick: () => void
}

export function BetAdminRoundRow({ round, onClick }: Props) {
  const { t, i18n } = useTranslation()

  return (
    <button className="bet-admin-round-row" onClick={onClick}>
      <span>
        <GlobalEventArt eventType={round.eventType} size={48} />
      </span>
      <span>
        <small>{betAdminEventLabel(t, round.eventType)}</small>
        <strong>{round.status}</strong>
        <em>{formatBetAdminDate(round.scheduledAt, i18n.language)}</em>
      </span>
      <span>
        <small>{t("betAdmin.yesNo")}</small>
        <strong>
          {round.yesOdds.toFixed(2)} / {round.noOdds.toFixed(2)}
        </strong>
      </span>
      <b>›</b>
    </button>
  )
}
