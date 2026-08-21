import { useEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import type { ItemCatalogResponse } from "@/shared/api/backendContracts"
import { searchItemCatalog } from "@/shared/api/eventApi"
import {
  createBoosterSubscription,
  createDungeonSubscription,
  createGlobalSubscription,
  createItemDropSubscription,
  createRaidSubscription,
} from "@/shared/api/subscriptionApi"
import { handleError } from "@/shared/errors/errorHandler"
import { ItemTypeArt } from "../../components/events/EventArt"
import {
  BOOSTER_TYPES,
  DUNGEON_TYPES,
  GLOBAL_EVENT_TYPES,
  SUBSCRIPTION_TYPES,
} from "../../contracts"
import { useMnemosData } from "../../data/MnemosDataProvider"
import type {
  BoosterType,
  DungeonType,
  GlobalEventType,
  SubscriptionType,
} from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { BottomSheet, ChoiceGrid, Field } from "../../shared/Primitives"

type SubscriptionCreateSheetProps = {
  onClose(): void
}

export function SubscriptionCreateSheet({ onClose }: SubscriptionCreateSheetProps) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [busy, setBusy] = useState(false)
  const [type, setType] = useState<SubscriptionType>("ITEM_DROP")
  const [droppedFor, setDroppedFor] = useState("")
  const [itemName, setItemName] = useState("")
  const [boosterType, setBoosterType] = useState<BoosterType>("LUCK")
  const [dungeonType, setDungeonType] = useState<DungeonType>("EASY")
  const [globalType, setGlobalType] = useState<GlobalEventType>("ECLIPSE")
  const [raidLocations, setRaidLocations] = useState<number[]>([])
  const [raidInput, setRaidInput] = useState("")
  const [itemSearch, setItemSearch] = useState<{
    query: string
    items: ItemCatalogResponse[]
  }>({ query: "", items: [] })
  const mutationLocked = useRef(false)
  const mountedRef = useRef(true)

  const cleanDroppedFor = droppedFor.trim()
  const cleanItemName = itemName.trim()
  const cleanRaidInput = raidInput.trim()
  const raidInputValue = Number(cleanRaidInput)

  const droppedForValid =
    !cleanDroppedFor ||
    (cleanDroppedFor.length >= 4 &&
      cleanDroppedFor.length <= 20 &&
      /^[\p{L}0-9_]+$/u.test(cleanDroppedFor))
  const itemNameValid = !cleanItemName || cleanItemName.length <= 24
  const itemDropValid =
    droppedForValid && itemNameValid && Boolean(cleanDroppedFor || cleanItemName)
  const raidInputValid = !cleanRaidInput || (Number.isInteger(raidInputValue) && raidInputValue > 0)
  const createValid = (type !== "ITEM_DROP" || itemDropValid) && (type !== "RAID" || raidInputValid)
  const itemSuggestions = itemSearch.query === cleanItemName ? itemSearch.items : []

  const subscriptionLabels = Object.fromEntries(
    SUBSCRIPTION_TYPES.map((value) => [value, t(`events.kinds.${value}`)]),
  ) as Record<SubscriptionType, string>
  const boosterLabels = Object.fromEntries(
    BOOSTER_TYPES.map((value) => [value, t(`events.boosters.${value}`)]),
  ) as Record<BoosterType, string>
  const dungeonLabels = Object.fromEntries(
    DUNGEON_TYPES.map((value) => [value, t(`events.dungeons.${value}`)]),
  ) as Record<DungeonType, string>
  const globalLabels = Object.fromEntries(
    GLOBAL_EVENT_TYPES.map((value) => [value, t(`events.globals.${value}`)]),
  ) as Record<GlobalEventType, string>

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

  useEffect(() => {
    if (cleanItemName.length < 2) {
      return
    }

    let cancelled = false
    const timeout = window.setTimeout(() => {
      void searchItemCatalog(cleanItemName, 8)
        .then((items) => {
          if (!cancelled) {
            setItemSearch({ query: cleanItemName, items })
          }
        })
        .catch((error) => {
          if (!cancelled) {
            handleError(error)
          }
        })
    }, 220)

    return () => {
      cancelled = true
      window.clearTimeout(timeout)
    }
  }, [cleanItemName])

  const addRaidLocation = () => {
    if (!raidInputValid || !cleanRaidInput) {
      return
    }

    if (!raidLocations.includes(raidInputValue)) {
      setRaidLocations((current) => [...current, raidInputValue])
    }

    setRaidInput("")
  }

  const create = async () => {
    if (mutationLocked.current || !createValid) {
      return
    }

    mutationLocked.current = true
    setBusy(true)

    try {
      const submittedRaidLocations =
        cleanRaidInput && raidInputValid
          ? Array.from(new Set([...raidLocations, raidInputValue]))
          : raidLocations

      const saved = await data.mutateSubscriptions(async () => {
        if (type === "ITEM_DROP") {
          await createItemDropSubscription(cleanDroppedFor || undefined, cleanItemName || undefined)
        } else if (type === "BOOSTER") {
          await createBoosterSubscription(boosterType)
        } else if (type === "DUNGEON") {
          await createDungeonSubscription(dungeonType)
        } else if (type === "GLOBAL") {
          await createGlobalSubscription(globalType)
        } else {
          await createRaidSubscription(submittedRaidLocations)
        }
      })

      if (saved && mountedRef.current) {
        onClose()
      }
    } finally {
      mutationLocked.current = false

      if (mountedRef.current) {
        setBusy(false)
      }
    }
  }

  return (
    <BottomSheet title={t("events.subscriptions.newTitle")} onClose={() => !busy && onClose()}>
      <div className="form-stack subscription-form">
        <Field label={t("events.subscriptions.track")}>
          <ChoiceGrid
            value={type}
            options={SUBSCRIPTION_TYPES}
            onChange={setType}
            labels={subscriptionLabels}
          />
        </Field>

        {type === "ITEM_DROP" ? (
          <ItemDropFields
            droppedFor={droppedFor}
            itemName={itemName}
            cleanDroppedFor={cleanDroppedFor}
            droppedForValid={droppedForValid}
            itemSuggestions={itemSuggestions}
            onDroppedForChange={setDroppedFor}
            onItemNameChange={setItemName}
            onItemSelect={(item) => {
              setItemName(item.name)
              setItemSearch({ query: item.name, items: [] })
            }}
          />
        ) : null}

        {type === "BOOSTER" ? (
          <Field label={t("events.subscriptions.boosterType")}>
            <ChoiceGrid
              value={boosterType}
              options={BOOSTER_TYPES}
              onChange={setBoosterType}
              labels={boosterLabels}
            />
          </Field>
        ) : null}

        {type === "DUNGEON" ? (
          <Field label={t("events.subscriptions.difficulty")}>
            <ChoiceGrid
              value={dungeonType}
              options={DUNGEON_TYPES}
              onChange={setDungeonType}
              labels={dungeonLabels}
            />
          </Field>
        ) : null}

        {type === "GLOBAL" ? (
          <Field label={t("events.subscriptions.event")}>
            <ChoiceGrid
              value={globalType}
              options={GLOBAL_EVENT_TYPES}
              onChange={setGlobalType}
              labels={globalLabels}
            />
          </Field>
        ) : null}

        {type === "RAID" ? (
          <RaidFields
            raidInput={raidInput}
            raidLocations={raidLocations}
            cleanRaidInput={cleanRaidInput}
            raidInputValid={raidInputValid}
            onRaidInputChange={setRaidInput}
            onAddLocation={addRaidLocation}
            onRemoveLocation={(location) =>
              setRaidLocations((current) => current.filter((value) => value !== location))
            }
          />
        ) : null}

        <button
          type="button"
          className="primary-action"
          disabled={busy || !createValid}
          onClick={() => void create()}
        >
          {busy ? t("events.subscriptions.saving") : t("events.subscriptions.save")}
        </button>
      </div>
    </BottomSheet>
  )
}

function ItemDropFields({
  droppedFor,
  itemName,
  cleanDroppedFor,
  droppedForValid,
  itemSuggestions,
  onDroppedForChange,
  onItemNameChange,
  onItemSelect,
}: {
  droppedFor: string
  itemName: string
  cleanDroppedFor: string
  droppedForValid: boolean
  itemSuggestions: ItemCatalogResponse[]
  onDroppedForChange(value: string): void
  onItemNameChange(value: string): void
  onItemSelect(item: ItemCatalogResponse): void
}) {
  const { t } = useTranslation()

  return (
    <>
      <Field
        label={t("events.subscriptions.player")}
        hint={
          cleanDroppedFor && !droppedForValid
            ? t("events.subscriptions.playerInvalid")
            : t("events.subscriptions.playerHint")
        }
      >
        <input
          value={droppedFor}
          onChange={(event) => onDroppedForChange(event.target.value)}
          placeholder="nickname"
          minLength={4}
          maxLength={20}
        />
      </Field>

      <Field
        label={t("events.subscriptions.item")}
        hint={t("events.subscriptions.itemHint")}
      >
        <input
          value={itemName}
          onChange={(event) => onItemNameChange(event.target.value)}
          placeholder={t("events.subscriptions.itemPlaceholder")}
          maxLength={24}
        />

        {itemSuggestions.length ? (
          <div className="field-suggestions">
            {itemSuggestions.map((item) => (
              <button key={item.id} type="button" onClick={() => onItemSelect(item)}>
                <ItemTypeArt itemType={item.type} size={28} />
                <span>
                  <strong>{item.name}</strong>
                  <small>{t(`events.itemTypes.${item.type}`)}</small>
                </span>
              </button>
            ))}
          </div>
        ) : null}
      </Field>
    </>
  )
}

function RaidFields({
  raidInput,
  raidLocations,
  cleanRaidInput,
  raidInputValid,
  onRaidInputChange,
  onAddLocation,
  onRemoveLocation,
}: {
  raidInput: string
  raidLocations: number[]
  cleanRaidInput: string
  raidInputValid: boolean
  onRaidInputChange(value: string): void
  onAddLocation(): void
  onRemoveLocation(location: number): void
}) {
  const { t } = useTranslation()

  return (
    <Field
      label={t("events.subscriptions.locations")}
      hint={
        raidInputValid
          ? t("events.subscriptions.allLocationsHint")
          : t("events.subscriptions.locationInvalid")
      }
    >
      <div className="inline-input">
        <input
          type="number"
          min="1"
          step="1"
          value={raidInput}
          onChange={(event) => onRaidInputChange(event.target.value)}
          placeholder="№"
        />
        <button
          type="button"
          aria-label={t("events.subscriptions.addLocation")}
          disabled={!cleanRaidInput || !raidInputValid}
          onClick={onAddLocation}
        >
          <Glyph name="plus" size={17} />
        </button>
      </div>

      <div className="token-list">
        {raidLocations.map((location) => (
          <button key={location} type="button" onClick={() => onRemoveLocation(location)}>
            {location}
            <Glyph name="close" size={12} />
          </button>
        ))}
      </div>
    </Field>
  )
}
