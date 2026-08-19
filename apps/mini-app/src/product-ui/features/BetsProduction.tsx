import type { TFunction } from "i18next"
import { AnimatePresence, motion } from "framer-motion"
import { useCallback, useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  increaseEventBet,
  increaseItemRaceBet,
  joinItemRace,
  listEventBetRounds,
  listItemRaces,
  listMyItemRacePositions,
  placeEventBet,
  type EventBetRoundResponse,
  type ItemRaceParticipantResponse,
  type ItemRaceResponse,
} from "@/shared/api/betApi"
import { handleError } from "@/shared/errors/errorHandler"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import { GlobalEventArt, ItemTypeArt } from "../components/EventArt"
import { BET_SIDES } from "../contracts"
import { useProductData } from "../data/ProductDataProvider"
import { globalEventVisualTone } from "../eventVisualTone"
import type { Account, BetSide, Route } from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { BottomSheet, CurrencyValue, Field } from "../shared/Primitives"

type Props = {
  activeAccount: Account
  navigate: (route: Route) => void
}

type BetMode = "EVENTS" | "MY" | "RACES"

type SelectedRace = {
  race: ItemRaceResponse
  position: ItemRaceParticipantResponse | null
}

const betModes: BetMode[] = ["EVENTS", "MY", "RACES"]

export function BetsProduction({ activeAccount, navigate }: Props) {
  const { t, i18n } = useTranslation()
  const product = useProductData()
  const refreshAccount = product.refreshAccount
  const [mode, setMode] = useState<BetMode>("EVENTS")
  const [rounds, setRounds] = useState<EventBetRoundResponse[]>([])
  const [races, setRaces] = useState<ItemRaceResponse[]>([])
  const [positions, setPositions] = useState<ItemRaceParticipantResponse[]>([])
  const [selectedRound, setSelectedRound] = useState<EventBetRoundResponse | null>(null)
  const [selectedRace, setSelectedRace] = useState<SelectedRace | null>(null)
  const [side, setSide] = useState<BetSide>("YES")
  const [amount, setAmount] = useState("500")
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const locale = localeForLanguage(i18n.resolvedLanguage)

  const refresh = useCallback(async () => {
    try {
      const [roundResponses, raceResponses, positionResponses] = await Promise.all([
        listEventBetRounds(),
        listItemRaces(),
        listMyItemRacePositions(),
      ])

      setRounds(roundResponses)
      setRaces(raceResponses)
      setPositions(positionResponses)
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refresh])

  useEffect(() => realtimeEvents.subscribe((event) => {
    if (event.type !== "bet.operation.completed") return

    void refresh()
    void refreshAccount()
  }), [refreshAccount, refresh])

  const positionsByRace = useMemo(
    () => new Map(positions.map((position) => [position.raceId, position])),
    [positions],
  )
  const featured = rounds.find((round) => round.status === "OPEN") ?? rounds[0] ?? null
  const eventRounds = featured ? rounds.filter((round) => round.id !== featured.id) : rounds
  const myRounds = rounds.filter((round) => round.myBet)
  const myRaces = races.filter((race) => positionsByRace.has(race.id))
  const numericAmount = Number(amount)
  const selectedHasBet = Boolean(selectedRound?.myBet)
  const amountValid = Number.isInteger(numericAmount)
    && numericAmount >= (selectedHasBet ? 1 : 100)
    && (selectedHasBet || numericAmount <= 5_000)

  const openRound = (round: EventBetRoundResponse) => {
    setSelectedRound(round)
    setSide(round.myBet?.side ?? "YES")
    setAmount(round.myBet ? "100" : "500")
  }

  const openRace = (race: ItemRaceResponse) => {
    const position = positionsByRace.get(race.id) ?? null
    setSelectedRace({ race, position })
    setAmount(position ? "100" : String(race.entryAmount))
  }

  const submitRound = async () => {
    if (!selectedRound || !amountValid || selectedRound.status !== "OPEN" || busy) return

    setBusy(true)

    try {
      if (selectedRound.myBet) {
        await increaseEventBet(selectedRound.id, numericAmount)
      } else {
        await placeEventBet(selectedRound.id, side, numericAmount)
      }

      setSelectedRound(null)
      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const submitRace = async () => {
    if (!selectedRace || busy) return

    const { race, position } = selectedRace
    const canWrite = race.status === "REGISTRATION"
    if (!canWrite) return

    if (position && (!Number.isInteger(numericAmount) || numericAmount <= 0)) return

    setBusy(true)

    try {
      if (position) {
        await increaseItemRaceBet(race.id, numericAmount)
      } else {
        await joinItemRace(race.id)
      }

      setSelectedRace(null)
      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="screen bets-r2-screen root-screen">
      <header className="root-title-block bets-r2-title">
        <span>
          <small>MNEMOS BETS</small>
          <h1>{t("bets.title")}</h1>
          <p>{activeAccount.nickname}</p>
        </span>
        <button className="bets-r2-wallet" onClick={() => navigate({ id: "account-wallet" })}>
          <CurrencyValue currency="COIN" value={activeAccount.coin} compact />
        </button>
      </header>

      <nav className="bets-r2-mode">
        {betModes.map((value) => (
          <button key={value} className={mode === value ? "is-active" : ""} onClick={() => setMode(value)}>
            <span>{t(`bets.modes.${value}`)}</span>
            {value === "MY" && myRounds.length + myRaces.length ? <b>{myRounds.length + myRaces.length}</b> : null}
          </button>
        ))}
      </nav>

      {loading ? <div className="events-r2-loading" aria-label={t("bets.loading")}><i /><i /><i /></div> : null}

      {!loading && mode === "EVENTS" ? (
        <>
          {featured ? (
            <motion.button
              className={`bets-r2-feature event-surface--${globalEventVisualTone(featured.eventType)}`}
              whileTap={{ scale: 0.992 }}
              onClick={() => openRound(featured)}
            >
              <span className="bets-r2-feature__art"><GlobalEventArt eventType={featured.eventType} size={82} /></span>
              <span className="bets-r2-feature__copy">
                <small>{featured.status === "OPEN" ? t("bets.nextRound") : roundStatusLabel(t, featured.status)}</small>
                <strong>{globalEventLabel(t, featured.eventType)}</strong>
                <em>{t("bets.closes", { time: formatMoment(featured.closesAt, locale) })}</em>
              </span>
              <span className="bets-r2-feature__odds">
                <span><small>{t("bets.yesUpper")}</small><strong>{formatOdds(featured.yesOdds, locale)}</strong></span>
                <i />
                <span><small>{t("bets.noUpper")}</small><strong>{formatOdds(featured.noOdds, locale)}</strong></span>
              </span>
              {featured.myBet ? (
                <span className="bets-r2-feature__mine">
                  <CurrencyMark currency="COIN" size={13} />{betAmount(featured.myBet)}
                </span>
              ) : null}
            </motion.button>
          ) : (
            <EmptyBets title={t("bets.empty.roundsTitle")} note={t("bets.empty.roundsCopy")} />
          )}

          {eventRounds.length ? (
            <>
              <header className="bets-r2-section-head"><span><small>{t("bets.moreRounds")}</small><strong>{eventRounds.length}</strong></span></header>
              <section className="bets-r2-rounds">
                {eventRounds.map((round) => (
                  <BetRoundCard
                    key={round.id}
                    round={round}
                    locale={locale}
                    t={t}
                    onClick={() => openRound(round)}
                  />
                ))}
              </section>
            </>
          ) : null}
        </>
      ) : null}

      {!loading && mode === "MY" ? (
        <section className="bets-r2-rounds">
          {myRounds.map((round) => (
            <BetRoundCard
              key={round.id}
              round={round}
              locale={locale}
              t={t}
              onClick={() => openRound(round)}
            />
          ))}
          {myRaces.map((race) => (
            <BetRaceCard
              key={race.id}
              race={race}
              position={positionsByRace.get(race.id) ?? null}
              locale={locale}
              t={t}
              onClick={() => openRace(race)}
            />
          ))}
          {!myRounds.length && !myRaces.length ? (
            <EmptyBets title={t("bets.empty.myTitle")} note={t("bets.empty.myCopy")} />
          ) : null}
        </section>
      ) : null}

      {!loading && mode === "RACES" ? (
        <section className="bets-r2-races">
          {races.map((race) => (
            <BetRaceCard
              key={race.id}
              race={race}
              position={positionsByRace.get(race.id) ?? null}
              locale={locale}
              t={t}
              onClick={() => openRace(race)}
            />
          ))}
          {!races.length ? <EmptyBets title={t("bets.empty.racesTitle")} note={t("bets.empty.racesCopy")} /> : null}
        </section>
      ) : null}

      <AnimatePresence>
        {selectedRound ? (
          <BottomSheet title={globalEventLabel(t, selectedRound.eventType)} onClose={() => !busy && setSelectedRound(null)}>
            <div className="bets-r2-sheet">
              <div className="bets-r2-sheet__event">
                <GlobalEventArt eventType={selectedRound.eventType} size={56} />
                <span>
                  <small>{roundStatusLabel(t, selectedRound.status)}</small>
                  <strong>{formatMoment(selectedRound.scheduledAt, locale)}</strong>
                  <em>{t("bets.sheet.until", { time: formatMoment(selectedRound.closesAt, locale) })}</em>
                </span>
              </div>

              {!selectedRound.myBet ? (
                <div className="bets-r2-side-choice">
                  {BET_SIDES.map((value) => (
                    <button key={value} disabled={busy} className={side === value ? "is-active" : ""} onClick={() => setSide(value)}>
                      <span>{value === "YES" ? t("bets.yes") : t("bets.no")}</span>
                      <strong>× {formatOdds(value === "YES" ? selectedRound.yesOdds : selectedRound.noOdds, locale)}</strong>
                    </button>
                  ))}
                </div>
              ) : (
                <div className="bets-r2-current-position">
                  <span>
                    <small>{t("bets.sheet.yourPosition")}</small>
                    <strong>{selectedRound.myBet.side === "YES" ? t("bets.yes") : t("bets.no")}</strong>
                  </span>
                  <CurrencyValue currency="COIN" value={betAmount(selectedRound.myBet)} compact />
                </div>
              )}

              <Field label={selectedRound.myBet ? t("bets.sheet.increaseBy") : t("bets.sheet.amount")}>
                <div className="bets-r2-amount">
                  <CurrencyMark currency="COIN" size={16} />
                  <input
                    type="number"
                    min={selectedRound.myBet ? 1 : 100}
                    max={selectedRound.myBet ? undefined : 5000}
                    value={amount}
                    disabled={busy}
                    onChange={(event) => setAmount(event.target.value)}
                  />
                </div>
              </Field>

              <button
                className="bets-r2-submit"
                disabled={!amountValid || selectedRound.status !== "OPEN" || busy}
                onClick={() => void submitRound()}
              >
                {busy
                  ? t("bets.sheet.sending")
                  : selectedRound.myBet
                    ? t("bets.sheet.increase")
                    : t("bets.sheet.place")}
              </button>
            </div>
          </BottomSheet>
        ) : null}

        {selectedRace ? (
          <BottomSheet title={selectedRace.race.itemName} onClose={() => !busy && setSelectedRace(null)}>
            <div className="bets-r2-sheet">
              <div className="bets-r2-sheet__event">
                <ItemTypeArt itemType={selectedRace.race.itemType} size={56} />
                <span>
                  <small>{itemTypeLabel(t, selectedRace.race.itemType)}</small>
                  <strong>{raceStatusLabel(t, selectedRace.race.status)}</strong>
                  <em>{t("bets.race.registrationUntil", { time: formatMoment(selectedRace.race.registrationClosesAt, locale) })}</em>
                </span>
              </div>

              <div className="bets-r2-current-position">
                <span>
                  <small>{selectedRace.position ? t("bets.race.yourAmount") : t("bets.race.entry")}</small>
                  <strong>
                    {selectedRace.position
                      ? t("bets.race.activeParticipation")
                      : t("bets.race.fee", { percent: selectedRace.race.feePercent })}
                  </strong>
                </span>
                <CurrencyValue
                  currency="COIN"
                  value={selectedRace.position ? racePositionAmount(selectedRace.position) : selectedRace.race.entryAmount}
                  compact
                />
              </div>

              {selectedRace.position ? (
                <Field label={t("bets.sheet.increaseBy")}>
                  <div className="bets-r2-amount">
                    <CurrencyMark currency="COIN" size={16} />
                    <input
                      type="number"
                      min={1}
                      value={amount}
                      disabled={busy}
                      onChange={(event) => setAmount(event.target.value)}
                    />
                  </div>
                </Field>
              ) : null}

              <button
                className="bets-r2-submit"
                disabled={
                  busy
                  || selectedRace.race.status !== "REGISTRATION"
                  || (Boolean(selectedRace.position) && (!Number.isInteger(numericAmount) || numericAmount <= 0))
                }
                onClick={() => void submitRace()}
              >
                {busy
                  ? t("bets.sheet.sending")
                  : selectedRace.position
                    ? t("bets.sheet.increase")
                    : t("bets.race.join")}
              </button>
            </div>
          </BottomSheet>
        ) : null}
      </AnimatePresence>
    </div>
  )
}

function BetRoundCard({
  round,
  locale,
  t,
  onClick,
}: {
  round: EventBetRoundResponse
  locale: string
  t: TFunction
  onClick: () => void
}) {
  return (
    <motion.button
      className={`bets-r2-card event-surface--${globalEventVisualTone(round.eventType)} ${round.status !== "OPEN" ? "is-locked" : ""}`}
      whileTap={{ scale: 0.992 }}
      onClick={onClick}
    >
      <span className="bets-r2-card__art"><GlobalEventArt eventType={round.eventType} size={52} /></span>
      <span className="bets-r2-card__copy">
        <small>{globalEventLabel(t, round.eventType)}</small>
        <strong>{roundStatusLabel(t, round.status)}</strong>
        <em>{formatMoment(round.closesAt, locale)}</em>
      </span>
      <span className="bets-r2-card__odds">
        <b>{formatOdds(round.yesOdds, locale)}</b><i /><b>{formatOdds(round.noOdds, locale)}</b>
      </span>
      {round.myBet ? (
        <span className="bets-r2-card__mine"><CurrencyMark currency="COIN" size={12} />{betAmount(round.myBet)}</span>
      ) : null}
    </motion.button>
  )
}

function BetRaceCard({
  race,
  position,
  locale,
  t,
  onClick,
}: {
  race: ItemRaceResponse
  position: ItemRaceParticipantResponse | null
  locale: string
  t: TFunction
  onClick: () => void
}) {
  return (
    <motion.button className="bets-r2-race" whileTap={{ scale: 0.992 }} onClick={onClick}>
      <span className="bets-r2-race__art"><ItemTypeArt itemType={race.itemType} size={54} /></span>
      <span className="bets-r2-race__copy">
        <small>{itemTypeLabel(t, race.itemType)}</small>
        <strong>{race.itemName}</strong>
        <em>{raceStatusLabel(t, race.status)} · {t("bets.sheet.until", { time: formatMoment(race.registrationClosesAt, locale) })}</em>
      </span>
      <span className="bets-r2-race__entry">
        <small>{position ? t("bets.race.yours") : t("bets.race.entryUpper")}</small>
        <CurrencyValue currency="COIN" value={position ? racePositionAmount(position) : race.entryAmount} compact />
      </span>
    </motion.button>
  )
}

function EmptyBets({ title, note }: { title: string; note: string }) {
  return (
    <div className="bets-r2-empty">
      <Glyph name="bet" size={23} />
      <strong>{title}</strong>
      <small>{note}</small>
    </div>
  )
}

function betAmount(bet: NonNullable<EventBetRoundResponse["myBet"]>) {
  return bet.confirmedAmount + bet.pendingAmount
}

function racePositionAmount(position: ItemRaceParticipantResponse) {
  return position.confirmedAmount + position.pendingAmount
}

function globalEventLabel(t: TFunction, eventType: EventBetRoundResponse["eventType"]) {
  return t(`events.globals.${eventType}`, { defaultValue: eventType })
}

function itemTypeLabel(t: TFunction, itemType: ItemRaceResponse["itemType"]) {
  return t(`bets.itemTypes.${itemType}`, { defaultValue: itemType })
}

function formatOdds(value: number, locale: string) {
  return Number(value).toLocaleString(locale, { maximumFractionDigits: 2 })
}

function formatMoment(value: string, locale: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat(locale, {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date)
}

function roundStatusLabel(t: TFunction, status: EventBetRoundResponse["status"]) {
  return t(`bets.roundStatus.${status}`, { defaultValue: status })
}

function raceStatusLabel(t: TFunction, status: ItemRaceResponse["status"]) {
  return t(`bets.raceStatus.${status}`, { defaultValue: status })
}

function localeForLanguage(language?: string) {
  if (language === "en") return "en-US"
  if (language === "ru") return "ru-RU"
  return "uk-UA"
}
