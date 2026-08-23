import { useEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import type { ItemRaceResponse } from "@/shared/api/betApi"
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

export function BetAdminRaceManagement({ races, loading, back, onChanged }: Props) {
  const { t } = useTranslation()
  const [formOpen, setFormOpen] = useState(false)
  const [itemName, setItemName] = useState("")
  const [itemType, setItemType] = useState<BetItemType>("SWORD")
  const [durationHours, setDurationHours] = useState("24")
  const [entryAmount, setEntryAmount] = useState("1000")
  const [busy, setBusy] = useState(false)
  const busyRef = useRef(false)
  const mountedRef = useRef(true)

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

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

            return (
              <article key={race.id} className="bet-admin-race-card">
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
                </span>
                <span className="bet-admin-race-card__money">
                  <CurrencyValue currency="COIN" value={pool} compact />
                  <small>{t("betAdmin.bank")}</small>
                </span>
                {cancellable ? (
                  <button type="button" disabled={busy} onClick={() => void cancel(race.id)}>
                    {t("betAdmin.cancel")}
                  </button>
                ) : null}
              </article>
            )
          })}

          {!races.length ? <p className="inline-hint">{t("betAdmin.noRaces")}</p> : null}
        </section>
      )}
    </div>
  )
}
