import { useState } from "react"
import { useTranslation } from "react-i18next"
import type {
  EventBetOutcome,
  EventBetRoundResponse,
} from "@/shared/api/betApi"
import { resolveEventBetRound } from "@/shared/api/betAdminApi"
import { handleError } from "@/shared/errors/errorHandler"
import { GlobalEventArt } from "../../components/events/EventArt"
import { EVENT_BET_OUTCOMES } from "../../contracts"
import { Field, PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"
import {
  betAdminEventLabel,
  betAdminOutcomeLabel,
  formatBetAdminDate,
} from "./betAdminPresentation"

type Props = {
  round: EventBetRoundResponse | null
  loading: boolean
  back: () => void
  onResolved: () => Promise<void>
}

export function BetAdminRoundDetail({ round, loading, back, onResolved }: Props) {
  const { t, i18n } = useTranslation()
  const [outcome, setOutcome] = useState<EventBetOutcome>("YES")
  const [reason, setReason] = useState("")
  const [busy, setBusy] = useState(false)

  if (loading) {
    return (
      <div className="screen bet-admin-screen">
        <PageHeader title={t("betAdmin.round")} back={back} />
        <FeatureLoading />
      </div>
    )
  }

  if (!round) {
    return (
      <div className="screen bet-admin-screen">
        <PageHeader title={t("betAdmin.round")} back={back} />
        <p className="inline-hint">{t("betAdmin.roundNotFound")}</p>
      </div>
    )
  }

  const canResolve =
    round.status === "AWAITING_RESULT" || round.status === "AWAITING_ADMIN_CONFIRMATION"

  const resolve = async () => {
    if (!canResolve || !reason.trim() || busy) {
      return
    }

    setBusy(true)

    try {
      await resolveEventBetRound(round.id, outcome, reason.trim())
      await onResolved()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="screen bet-admin-screen">
      <PageHeader title={betAdminEventLabel(t, round.eventType)} back={back} />

      <section className="bet-admin-round-detail-hero">
        <GlobalEventArt eventType={round.eventType} size={72} />
        <span>
          <small>{round.status}</small>
          <strong>{formatBetAdminDate(round.scheduledAt, i18n.language)}</strong>
          <em>
            {t("betAdmin.closing", {
              date: formatBetAdminDate(round.closesAt, i18n.language),
            })}
          </em>
        </span>
      </section>

      <section className="bet-admin-kpis">
        <article>
          <small>{t("betAdmin.yes")}</small>
          <strong>× {round.yesOdds.toFixed(2)}</strong>
        </article>
        <article>
          <small>{t("betAdmin.no")}</small>
          <strong>× {round.noOdds.toFixed(2)}</strong>
        </article>
        <article>
          <small>{t("betAdmin.result")}</small>
          <strong>{round.outcome ? betAdminOutcomeLabel(t, round.outcome) : "—"}</strong>
        </article>
      </section>

      {canResolve ? (
        <section className="bet-admin-form-block">
          <header>
            <small>MANUAL RESOLUTION</small>
            <strong>{t("betAdmin.manualResolution")}</strong>
          </header>

          <div className="bet-admin-choice">
            {EVENT_BET_OUTCOMES.map((value) => (
              <button
                key={value}
                className={outcome === value ? "is-active" : ""}
                onClick={() => setOutcome(value)}
              >
                <strong>{betAdminOutcomeLabel(t, value)}</strong>
              </button>
            ))}
          </div>

          <Field label={t("betAdmin.reason")}>
            <textarea
              value={reason}
              maxLength={500}
              onChange={(event) => setReason(event.target.value)}
              placeholder={t("betAdmin.reasonPlaceholder")}
            />
          </Field>

          <button
            className="primary-action"
            disabled={!reason.trim() || busy}
            onClick={() => void resolve()}
          >
            {busy ? t("betAdmin.saving") : t("betAdmin.confirmResult")}
          </button>
        </section>
      ) : null}
    </div>
  )
}
