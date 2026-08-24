import { useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import type { ItemRaceParticipantResponse, ItemRaceResponse } from "@/shared/api/betApi"
import { cancelItemRace, createItemRace } from "@/shared/api/betAdminApi"
import { handleError } from "@/shared/errors/errorHandler"
import { ItemTypeArt } from "../../components/events/EventArt"
import { BET_ITEM_TYPES } from "../../contracts"
import type { BetItemType } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { CurrencyValue, Field, PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"
import { betAdminItemLabel, betAdminRaceStatusLabel } from "./betAdminPresentation"

type Props = {
  races: ItemRaceResponse[]
  loading: boolean
  back: () => void
  onChanged: () => Promise<void>
}

type ParticipantFilter = "ALL" | "WINNER" | "ACTIVE" | "FINISHED"
type ParticipantSort = "DESC" | "ASC"

export function BetAdminRaceManagement({ races, loading, back, onChanged }: Props) {
  const { t } = useTranslation()
  const [formOpen, setFormOpen] = useState(false)
  const [itemName, setItemName] = useState("")
  const [itemType, setItemType] = useState<BetItemType>("SWORD")
  const [durationHours, setDurationHours] = useState("24")
  const [entryAmount, setEntryAmount] = useState("1000")
  const [busy, setBusy] = useState(false)
  const [selectedRaceId, setSelectedRaceId] = useState<string | null>(null)
  const [participantSearch, setParticipantSearch] = useState("")
  const [participantFilter, setParticipantFilter] = useState<ParticipantFilter>("ALL")
  const [participantSort, setParticipantSort] = useState<ParticipantSort>("DESC")
  const busyRef = useRef(false)
  const mountedRef = useRef(true)

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

  useEffect(() => {
    if (selectedRaceId && !races.some((race) => race.id === selectedRaceId)) {
      setSelectedRaceId(null)
    }
  }, [races, selectedRaceId])

  const selectedRace = useMemo(
    () => races.find((race) => race.id === selectedRaceId) ?? null,
    [races, selectedRaceId],
  )

  const visibleParticipants = useMemo(() => {
    if (!selectedRace) {
      return []
    }

    const query = participantSearch.trim().toLocaleLowerCase()

    return selectedRace.participants
      .filter((participant) => {
        if (query) {
          const matchesSearch =
            participant.accountNickname.toLocaleLowerCase().includes(query) ||
            participant.accountId.toLocaleLowerCase().includes(query)

          if (!matchesSearch) {
            return false
          }
        }

        if (participantFilter === "WINNER") {
          return isRaceWinner(selectedRace, participant)
        }

        if (participantFilter === "ACTIVE") {
          return ["PENDING", "ACTIVE", "PAYOUT_PENDING", "PAYOUT_FAILED"].includes(
            participant.status,
          )
        }

        if (participantFilter === "FINISHED") {
          return ["WON", "LOST", "REFUNDED"].includes(participant.status)
        }

        return true
      })
      .sort((left, right) => {
        const leftAmount = left.confirmedAmount + left.pendingAmount
        const rightAmount = right.confirmedAmount + right.pendingAmount

        return participantSort === "DESC" ? rightAmount - leftAmount : leftAmount - rightAmount
      })
  }, [participantFilter, participantSearch, participantSort, selectedRace])

  const duration = Number(durationHours)
  const entry = Number(entryAmount)
  const valid =
    itemName.trim().length > 0 &&
    itemName.trim().length <= 24 &&
    Number.isInteger(duration) &&
    duration >= 1 &&
    duration <= 168 &&
    Number.isInteger(entry) &&
    entry >= 1000 &&
    entry <= 5000

  const create = async () => {
    if (!valid || busyRef.current) {
      return
    }

    busyRef.current = true
    setBusy(true)

    try {
      await createItemRace({
        itemName: itemName.trim(),
        itemType,
        durationHours: duration,
        entryAmount: entry,
      })

      if (mountedRef.current) {
        setItemName("")
        setDurationHours("24")
        setEntryAmount("1000")
        setFormOpen(false)
        await onChanged()
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

  const cancel = async (raceId: string) => {
    if (busyRef.current) {
      return
    }

    busyRef.current = true
    setBusy(true)

    try {
      await cancelItemRace(raceId)

      if (mountedRef.current) {
        await onChanged()
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

  const toggleRace = (raceId: string) => {
    setSelectedRaceId((current) => (current === raceId ? null : raceId))
    setParticipantSearch("")
    setParticipantFilter("ALL")
    setParticipantSort("DESC")
  }

  return (
    <div className="screen bet-admin-screen">
      <PageHeader
        title={t("betAdmin.itemRace")}
        back={back}
        actions={
          <button
            type="button"
            className="bet-admin-header-action"
            onClick={() => setFormOpen((value) => !value)}
          >
            <Glyph name={formOpen ? "close" : "plus"} size={15} />
            {formOpen ? t("betAdmin.close") : t("betAdmin.new")}
          </button>
        }
      />

      {formOpen ? (
        <section className="bet-admin-form-block">
          <header>
            <small>{t("betAdmin.newRaceEyebrow")}</small>
            <strong>{t("betAdmin.newRace")}</strong>
          </header>

          <Field label={t("betAdmin.item")}>
            <input
              value={itemName}
              maxLength={24}
              onChange={(event) => setItemName(event.target.value)}
            />
          </Field>

          <Field label={t("betAdmin.type")}>
            <div className="bet-admin-choice">
              {BET_ITEM_TYPES.map((value) => (
                <button
                  type="button"
                  key={value}
                  className={itemType === value ? "is-active" : ""}
                  onClick={() => setItemType(value)}
                >
                  {betAdminItemLabel(t, value)}
                </button>
              ))}
            </div>
          </Field>

          <Field label={t("betAdmin.durationHours")}>
            <input
              type="number"
              min={1}
              max={168}
              value={durationHours}
              onChange={(event) => setDurationHours(event.target.value)}
            />
          </Field>

          <Field label={t("betAdmin.entry")}>
            <input
              type="number"
              min={1000}
              max={5000}
              value={entryAmount}
              onChange={(event) => setEntryAmount(event.target.value)}
            />
          </Field>

          <button
            type="button"
            className="primary-action"
            disabled={!valid || busy}
            onClick={() => void create()}
          >
            {busy ? t("betAdmin.creating") : t("betAdmin.create")}
          </button>
        </section>
      ) : null}

      {loading ? (
        <FeatureLoading />
      ) : (
        <section className="bet-admin-race-list">
          {races.map((race) => {
            const pool = race.participants.reduce(
              (sum, participant) => sum + participant.confirmedAmount + participant.pendingAmount,
              0,
            )
            const cancellable = race.status === "REGISTRATION" || race.status === "ACTIVE"
            const expanded = race.id === selectedRaceId
            const winner = race.participants.find((participant) => isRaceWinner(race, participant))

            return (
              <div key={race.id} className={`bet-admin-race-entry ${expanded ? "is-expanded" : ""}`}>
                <article className="bet-admin-race-card">
                  <span className="bet-admin-race-card__art">
                    <ItemTypeArt itemType={race.itemType} size={52} />
                  </span>
                  <span className="bet-admin-race-card__copy">
                    <small>{betAdminItemLabel(t, race.itemType)}</small>
                    <strong>{race.itemName}</strong>
                    <em>
                      {betAdminRaceStatusLabel(t, race.status)} ·{" "}
                      {t("betAdmin.participantsShort", { count: race.participants.length })}
                    </em>
                    {winner ? (
                      <b className="bet-admin-race-card__winner">
                        {t("betAdmin.winner")}: {winner.accountNickname}
                      </b>
                    ) : null}
                  </span>
                  <span className="bet-admin-race-card__money">
                    <CurrencyValue currency="COIN" value={pool} compact />
                    <small>{t("betAdmin.bank")}</small>
                  </span>
                  <span className="bet-admin-race-card__actions">
                    <button type="button" className="is-details" onClick={() => toggleRace(race.id)}>
                      {expanded ? t("betAdmin.hideParticipants") : t("betAdmin.viewParticipants")}
                    </button>
                    {cancellable ? (
                      <button
                        type="button"
                        className="is-cancel"
                        disabled={busy}
                        onClick={() => void cancel(race.id)}
                      >
                        {t("betAdmin.cancel")}
                      </button>
                    ) : null}
                  </span>
                </article>

                {expanded ? (
                  <section className="bet-admin-ledger bet-admin-race-ledger">
                    <header className="bet-admin-ledger__header">
                      <span>
                        <small>{t("betAdmin.raceParticipantsEyebrow")}</small>
                        <strong>{t("betAdmin.raceParticipants")}</strong>
                        <em>
                          {winner
                            ? t("betAdmin.raceWinner", { player: winner.accountNickname })
                            : t("betAdmin.raceWinnerPending")}
                        </em>
                      </span>
                      <CurrencyValue currency="COIN" value={pool} compact />
                    </header>

                    <div className="bet-admin-ledger__filters">
                      <input
                        value={participantSearch}
                        onChange={(event) => setParticipantSearch(event.target.value)}
                        placeholder={t("betAdmin.playerSearchPlaceholder")}
                        aria-label={t("betAdmin.playerSearchPlaceholder")}
                      />

                      <div className="bet-admin-ledger__filter-choices" role="group">
                        {(["ALL", "WINNER", "ACTIVE", "FINISHED"] as const).map((value) => (
                          <button
                            key={value}
                            type="button"
                            className={participantFilter === value ? "is-active" : ""}
                            onClick={() => setParticipantFilter(value)}
                          >
                            {t(`betAdmin.participantFilter${value}`)}
                          </button>
                        ))}
                      </div>

                      <button
                        type="button"
                        className="bet-admin-ledger__sort"
                        onClick={() =>
                          setParticipantSort((value) => (value === "DESC" ? "ASC" : "DESC"))
                        }
                      >
                        {t("betAdmin.amount")} {participantSort === "DESC" ? "↓" : "↑"}
                      </button>
                    </div>

                    {visibleParticipants.length ? (
                      <div className="bet-admin-ledger__rows">
                        {visibleParticipants.map((participant) => {
                          const amount = participant.confirmedAmount + participant.pendingAmount
                          const won = isRaceWinner(race, participant)

                          return (
                            <article
                              key={participant.id}
                              className={`bet-admin-ledger-row ${won ? "is-winner" : ""}`}
                            >
                              <span className="bet-admin-ledger-row__identity">
                                <strong>{participant.accountNickname}</strong>
                                <small>{participant.accountId}</small>
                              </span>
                              {won ? (
                                <span className="bet-admin-winner-badge">{t("betAdmin.winner")}</span>
                              ) : (
                                <span className="bet-admin-ledger-row__status">
                                  {t(`bets.raceParticipantStatus.${participant.status}`, {
                                    defaultValue: participant.status,
                                  })}
                                </span>
                              )}
                              <span className="bet-admin-ledger-row__amount">
                                <CurrencyValue currency="COIN" value={amount} compact />
                                {participant.pendingAmount > 0 ? (
                                  <small>
                                    {t("betAdmin.pendingAmount", {
                                      amount: participant.pendingAmount,
                                    })}
                                  </small>
                                ) : null}
                              </span>
                            </article>
                          )
                        })}
                      </div>
                    ) : (
                      <p className="inline-hint inline-hint--left">
                        {t("betAdmin.noMatchingParticipants")}
                      </p>
                    )}
                  </section>
                ) : null}
              </div>
            )
          })}

          {!races.length ? <p className="inline-hint">{t("betAdmin.noRaces")}</p> : null}
        </section>
      )}
    </div>
  )
}

function isRaceWinner(race: ItemRaceResponse, participant: ItemRaceParticipantResponse): boolean {
  return participant.status === "WON" || race.winnerAccountId === participant.accountId
}
