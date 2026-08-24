import { useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import type { EventBetOutcome, EventBetRoundResponse } from "@/shared/api/betApi"
import {
  listAdminEventBets,
  resolveEventBetRound,
  type AdminEventBetResponse,
} from "@/shared/api/betAdminApi"
import { handleError } from "@/shared/errors/errorHandler"
import { GlobalEventArt } from "../../components/events/EventArt"
import { EVENT_BET_OUTCOMES } from "../../contracts"
import type { BetSide } from "../../model"
import { CurrencyValue, Field, PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"
import {
  betAdminEventLabel,
  betAdminOutcomeLabel,
  betAdminRoundStatusLabel,
  formatBetAdminDate,
} from "./betAdminPresentation"

type Props = {
  round: EventBetRoundResponse | null
  loading: boolean
  back: () => void
  onResolved: () => Promise<void>
}

type BetSideFilter = "ALL" | BetSide
type BetAmountSort = "DESC" | "ASC"

export function BetAdminRoundDetail({ round, loading, back, onResolved }: Props) {
  const { t, i18n } = useTranslation()
  const [outcome, setOutcome] = useState<EventBetOutcome>("YES")
  const [reason, setReason] = useState("")
  const [busy, setBusy] = useState(false)
  const [bets, setBets] = useState<AdminEventBetResponse[]>([])
  const [betsLoading, setBetsLoading] = useState(false)
  const [betSearch, setBetSearch] = useState("")
  const [sideFilter, setSideFilter] = useState<BetSideFilter>("ALL")
  const [amountSort, setAmountSort] = useState<BetAmountSort>("DESC")
  const busyRef = useRef(false)
  const mountedRef = useRef(true)
  const roundId = round?.id ?? null

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

  useEffect(() => {
    if (!roundId) {
      setBets([])
      setBetsLoading(false)
      return
    }

    let cancelled = false
    setBetsLoading(true)

    void listAdminEventBets(roundId)
      .then((result) => {
        if (!cancelled) {
          setBets(result)
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setBets([])
          handleError(error)
        }
      })
      .finally(() => {
        if (!cancelled) {
          setBetsLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [roundId])

  const filteredBets = useMemo(() => {
    const query = betSearch.trim().toLocaleLowerCase()

    return bets
      .filter((bet) => {
        if (sideFilter !== "ALL" && bet.side !== sideFilter) {
          return false
        }

        if (!query) {
          return true
        }

        return (
          bet.accountNickname?.toLocaleLowerCase().includes(query) === true ||
          bet.accountId.toLocaleLowerCase().includes(query)
        )
      })
      .sort((left, right) => {
        const leftAmount = left.confirmedAmount + left.pendingAmount
        const rightAmount = right.confirmedAmount + right.pendingAmount

        return amountSort === "DESC" ? rightAmount - leftAmount : leftAmount - rightAmount
      })
  }, [amountSort, betSearch, bets, sideFilter])

  const betTotals = useMemo(
    () =>
      bets.reduce(
        (totals, bet) => {
          const amount = bet.confirmedAmount + bet.pendingAmount

          totals.total += amount
          totals[bet.side] += amount

          return totals
        },
        { total: 0, YES: 0, NO: 0 },
      ),
    [bets],
  )

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
    if (!canResolve || !reason.trim() || busyRef.current) {
      return
    }

    busyRef.current = true
    setBusy(true)

    try {
      await resolveEventBetRound(round.id, outcome, reason.trim())

      if (mountedRef.current) {
        await onResolved()
      }

      if (mountedRef.current) {
        back()
      }
    } catch (error) {
      if (mountedRef.current) {
        handleError(error)
      }
    } finally {
      busyRef.current = false

      if (mountedRef.current) {
        setBusy(false)
      }
    }
  }

  return (
    <div className="screen bet-admin-screen">
      <PageHeader title={betAdminEventLabel(t, round.eventType)} back={back} />

      <section className="bet-admin-round-detail-hero">
        <GlobalEventArt eventType={round.eventType} size={72} />
        <span>
          <small>{betAdminRoundStatusLabel(t, round.status)}</small>
          <strong>{formatBetAdminDate(round.scheduledAt, i18n.language)}</strong>
          <em>
            {t("betAdmin.closing", {
              date: formatBetAdminDate(round.closesAt, i18n.language),
            })}
          </em>
        </span>
      </section>

      <section className="bet-admin-kpis bet-admin-kpis--round">
        <article>
          <small>{t("betAdmin.players")}</small>
          <strong>{bets.length}</strong>
          <em>{t("betAdmin.bettors")}</em>
        </article>
        <article>
          <small>{t("betAdmin.yes")}</small>
          <strong>× {round.yesOdds.toFixed(2)}</strong>
          <em>{betTotals.YES.toLocaleString()}</em>
        </article>
        <article>
          <small>{t("betAdmin.no")}</small>
          <strong>× {round.noOdds.toFixed(2)}</strong>
          <em>{betTotals.NO.toLocaleString()}</em>
        </article>
        <article>
          <small>{t("betAdmin.result")}</small>
          <strong>{round.outcome ? betAdminOutcomeLabel(t, round.outcome) : "—"}</strong>
          <em>{betTotals.total.toLocaleString()}</em>
        </article>
      </section>

      <section className="bet-admin-ledger">
        <header className="bet-admin-ledger__header">
          <span>
            <small>{t("betAdmin.betsLedgerEyebrow")}</small>
            <strong>{t("betAdmin.betsLedger")}</strong>
            <em>{t("betAdmin.betsLedgerDescription", { count: bets.length })}</em>
          </span>
          <CurrencyValue currency="COIN" value={betTotals.total} compact />
        </header>

        <div className="bet-admin-ledger__filters">
          <input
            value={betSearch}
            onChange={(event) => setBetSearch(event.target.value)}
            placeholder={t("betAdmin.playerSearchPlaceholder")}
            aria-label={t("betAdmin.playerSearchPlaceholder")}
          />

          <div className="bet-admin-ledger__filter-choices" role="group">
            {(["ALL", "YES", "NO"] as const).map((value) => (
              <button
                key={value}
                type="button"
                className={sideFilter === value ? "is-active" : ""}
                onClick={() => setSideFilter(value)}
              >
                {value === "ALL" ? t("betAdmin.all") : value === "YES" ? t("betAdmin.yes") : t("betAdmin.no")}
              </button>
            ))}
          </div>

          <button
            type="button"
            className="bet-admin-ledger__sort"
            onClick={() => setAmountSort((value) => (value === "DESC" ? "ASC" : "DESC"))}
          >
            {t("betAdmin.amount")} {amountSort === "DESC" ? "↓" : "↑"}
          </button>
        </div>

        {betsLoading ? (
          <FeatureLoading />
        ) : filteredBets.length ? (
          <div className="bet-admin-ledger__rows">
            {filteredBets.map((bet) => {
              const amount = bet.confirmedAmount + bet.pendingAmount

              return (
                <article key={bet.id} className="bet-admin-ledger-row">
                  <span className="bet-admin-ledger-row__identity">
                    <strong>{bet.accountNickname || t("betAdmin.unknownPlayer")}</strong>
                    <small>{bet.accountId}</small>
                  </span>
                  <span className={`bet-admin-side-badge is-${bet.side.toLowerCase()}`}>
                    {bet.side === "YES" ? t("betAdmin.yes") : t("betAdmin.no")}
                  </span>
                  <span className="bet-admin-ledger-row__status">
                    {t(`bets.betStatus.${bet.status}`, { defaultValue: bet.status })}
                  </span>
                  <span className="bet-admin-ledger-row__amount">
                    <CurrencyValue currency="COIN" value={amount} compact />
                    {bet.pendingAmount > 0 ? (
                      <small>{t("betAdmin.pendingAmount", { amount: bet.pendingAmount })}</small>
                    ) : null}
                  </span>
                </article>
              )
            })}
          </div>
        ) : (
          <p className="inline-hint inline-hint--left">{t("betAdmin.noMatchingBets")}</p>
        )}
      </section>

      {canResolve ? (
        <section className="bet-admin-form-block">
          <header>
            <small>{t("betAdmin.manualResolutionEyebrow")}</small>
            <strong>{t("betAdmin.manualResolution")}</strong>
          </header>

          <div className="bet-admin-choice">
            {EVENT_BET_OUTCOMES.map((value) => (
              <button
                type="button"
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
            type="button"
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
