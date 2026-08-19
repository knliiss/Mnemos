import type { TFunction } from "i18next"
import { AnimatePresence } from "framer-motion"
import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  createBoosterSubscription,
  createDungeonSubscription,
  createGlobalSubscription,
  createItemDropSubscription,
  createRaidSubscription,
} from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import {
  BoosterArt,
  DungeonArt,
  GlobalEventArt,
  ItemTypeArt,
  RaidArt,
} from "../components/EventArt"
import { BOOSTER_TYPES, DUNGEON_TYPES, GLOBAL_EVENT_TYPES, SUBSCRIPTION_TYPES } from "../contracts"
import { useProductData } from "../data/ProductDataProvider"
import { subscriptionVisualTone } from "../eventVisualTone"
import type {
  BetItemType,
  BoosterType,
  DungeonType,
  GlobalEventType,
  Subscription,
  SubscriptionType,
} from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { BottomSheet, ChoiceGrid, Field, PageHeader } from "../shared/Primitives"

const subscriptionIcons: Record<
  SubscriptionType,
  "item-drop" | "booster" | "dungeon" | "raid" | "global"
> = {
  ITEM_DROP: "item-drop",
  BOOSTER: "booster",
  DUNGEON: "dungeon",
  RAID: "raid",
  GLOBAL: "global",
}

type ItemCatalogEntry = {
  name: string
  type: BetItemType
}

const itemCatalog: ItemCatalogEntry[] = [
  { name: "Eternal Blade", type: "SWORD" },
  { name: "Moon Necklace", type: "JEWELRY" },
  { name: "Black Flame", type: "AURA" },
  { name: "Observer", type: "PET" },
  { name: "Aincrad Notes", type: "BOOK" },
]

export function Subscriptions({ back }: { back: () => void }) {
  const { t } = useTranslation()
  const product = useProductData()
  const [creating, setCreating] = useState(false)
  const [busy, setBusy] = useState(false)
  const [deletingId, setDeletingId] = useState<string | null>(null)
  const [type, setType] = useState<SubscriptionType>("ITEM_DROP")
  const [droppedFor, setDroppedFor] = useState("")
  const [itemName, setItemName] = useState("")
  const [boosterType, setBoosterType] = useState<BoosterType>("LUCK")
  const [dungeonType, setDungeonType] = useState<DungeonType>("EASY")
  const [globalType, setGlobalType] = useState<GlobalEventType>("ECLIPSE")
  const [raidLocations, setRaidLocations] = useState<number[]>([])
  const [raidInput, setRaidInput] = useState("")

  const subscriptions = product.subscriptions
  const cleanDroppedFor = droppedFor.trim()
  const cleanItemName = itemName.trim()
  const droppedForValid =
    !cleanDroppedFor ||
    (cleanDroppedFor.length >= 4 &&
      cleanDroppedFor.length <= 20 &&
      /^[\p{L}0-9_]+$/u.test(cleanDroppedFor))
  const itemNameValid = !cleanItemName || cleanItemName.length <= 24
  const itemDropValid =
    droppedForValid && itemNameValid && Boolean(cleanDroppedFor || cleanItemName)

  const itemSuggestions = useMemo(() => {
    const query = cleanItemName.toLowerCase()
    if (query.length < 2) return []

    return itemCatalog.filter((item) => item.name.toLowerCase().includes(query)).slice(0, 4)
  }, [cleanItemName])

  const counts = useMemo(
    () =>
      SUBSCRIPTION_TYPES.map((subscriptionType) => ({
        type: subscriptionType,
        count: subscriptions.filter((subscription) => subscription.type === subscriptionType)
          .length,
      })).filter((entry) => entry.count > 0),
    [subscriptions],
  )

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

  const addRaidLocation = () => {
    const value = Number(raidInput)

    if (Number.isInteger(value) && value > 0 && !raidLocations.includes(value)) {
      setRaidLocations((current) => [...current, value])
    }

    setRaidInput("")
  }

  const resetDraft = () => {
    setDroppedFor("")
    setItemName("")
    setBoosterType("LUCK")
    setDungeonType("EASY")
    setGlobalType("ECLIPSE")
    setRaidLocations([])
    setRaidInput("")
  }

  const create = async () => {
    if (busy || (type === "ITEM_DROP" && !itemDropValid)) return

    setBusy(true)

    try {
      if (type === "ITEM_DROP") {
        await createItemDropSubscription(cleanDroppedFor || undefined, cleanItemName || undefined)
      } else if (type === "BOOSTER") {
        await createBoosterSubscription(boosterType)
      } else if (type === "DUNGEON") {
        await createDungeonSubscription(dungeonType)
      } else if (type === "GLOBAL") {
        await createGlobalSubscription(globalType)
      } else {
        await createRaidSubscription(raidLocations)
      }

      await product.refreshSubscriptions()
      resetDraft()
      setCreating(false)
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const remove = async (subscriptionId: string) => {
    if (deletingId) return

    setDeletingId(subscriptionId)

    try {
      await product.removeSubscription(subscriptionId)
    } catch {
      // ProductDataProvider already displays the backend error.
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <div className="screen subscriptions-redesign-screen">
      <PageHeader title={t("events.subscriptions.title")} back={back} />

      <section className="subscription-summary">
        <span className="subscription-summary__icon">
          <Glyph name="bell" size={22} />
        </span>
        <span>
          <strong>{subscriptions.length}</strong>
          <small>{t("events.subscriptions.activeRules")}</small>
        </span>
        <button disabled={busy} onClick={() => setCreating(true)}>
          <Glyph name="plus" size={17} />
          {t("events.subscriptions.new")}
        </button>
      </section>

      <div className="subscription-counts">
        {counts.map((entry) => (
          <span key={entry.type}>
            <Glyph name={subscriptionIcons[entry.type]} size={14} />
            {subscriptionLabels[entry.type]}
            <b>{entry.count}</b>
          </span>
        ))}
      </div>

      <section className="subscription-rules">
        <header>
          <h2>{t("events.subscriptions.rules")}</h2>
          <small>{t("events.subscriptions.rulesCopy")}</small>
        </header>
        <div>
          {subscriptions.map((subscription) => (
            <article
              className={`subscription-rule event-row-surface event-surface--${subscriptionVisualTone(subscription)}`}
              key={subscription.id}
            >
              <SubscriptionRuleIcon subscription={subscription} />
              <span className="subscription-rule__copy">
                <strong>{subscriptionLabels[subscription.type]}</strong>
                <small>{description(t, subscription)}</small>
              </span>
              <button
                aria-label={t("events.subscriptions.delete")}
                disabled={deletingId !== null}
                onClick={() => void remove(subscription.id)}
              >
                {deletingId === subscription.id ? <span>…</span> : <Glyph name="close" size={15} />}
              </button>
            </article>
          ))}

          {!subscriptions.length ? (
            <div className="events-r2-empty">
              <Glyph name="bell" size={24} />
              <strong>{t("events.subscriptions.emptyTitle")}</strong>
              <small>{t("events.subscriptions.emptyCopy")}</small>
            </div>
          ) : null}
        </div>
      </section>

      <AnimatePresence>
        {creating ? (
          <BottomSheet
            title={t("events.subscriptions.newTitle")}
            onClose={() => !busy && setCreating(false)}
          >
            <div className="form-stack subscription-form-redesign">
              <Field label={t("events.subscriptions.track")}>
                <ChoiceGrid
                  value={type}
                  options={SUBSCRIPTION_TYPES}
                  onChange={setType}
                  labels={subscriptionLabels}
                />
              </Field>

              {type === "ITEM_DROP" ? (
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
                      onChange={(event) => setDroppedFor(event.target.value)}
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
                      onChange={(event) => setItemName(event.target.value)}
                      placeholder={t("events.subscriptions.itemPlaceholder")}
                      maxLength={24}
                    />
                    {itemSuggestions.length ? (
                      <div className="field-suggestions">
                        {itemSuggestions.map((item) => (
                          <button
                            key={item.name}
                            type="button"
                            onClick={() => setItemName(item.name)}
                          >
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
                <Field
                  label={t("events.subscriptions.locations")}
                  hint={t("events.subscriptions.allLocationsHint")}
                >
                  <div className="inline-input">
                    <input
                      type="number"
                      min="1"
                      step="1"
                      value={raidInput}
                      onChange={(event) => setRaidInput(event.target.value)}
                      placeholder="№"
                    />
                    <button
                      type="button"
                      aria-label={t("events.subscriptions.addLocation")}
                      onClick={addRaidLocation}
                    >
                      <Glyph name="plus" size={17} />
                    </button>
                  </div>
                  <div className="token-list">
                    {raidLocations.map((location) => (
                      <button
                        key={location}
                        type="button"
                        onClick={() =>
                          setRaidLocations((current) =>
                            current.filter((value) => value !== location),
                          )
                        }
                      >
                        {location}
                        <Glyph name="close" size={12} />
                      </button>
                    ))}
                  </div>
                </Field>
              ) : null}

              <button
                className="primary-action"
                disabled={busy || (type === "ITEM_DROP" && !itemDropValid)}
                onClick={() => void create()}
              >
                {busy ? t("events.subscriptions.saving") : t("events.subscriptions.save")}
              </button>
            </div>
          </BottomSheet>
        ) : null}
      </AnimatePresence>
    </div>
  )
}

function findItem(itemName?: string) {
  if (!itemName) return null

  const normalized = itemName.trim().toLowerCase()
  return itemCatalog.find((item) => item.name.toLowerCase() === normalized) ?? null
}

function description(t: TFunction, subscription: Subscription) {
  if (subscription.type === "ITEM_DROP") {
    if (subscription.droppedFor && subscription.itemName)
      return `${subscription.droppedFor} · ${subscription.itemName}`
    if (subscription.droppedFor)
      return t("events.subscriptions.playerRule", { player: subscription.droppedFor })
    return t("events.subscriptions.itemRule", { item: subscription.itemName })
  }

  if (subscription.type === "BOOSTER") return t(`events.boosters.${subscription.boosterType}`)
  if (subscription.type === "DUNGEON") return t(`events.dungeons.${subscription.dungeonType}`)
  if (subscription.type === "GLOBAL") return t(`events.globals.${subscription.eventType}`)

  return subscription.locations.length
    ? t("events.subscriptions.locationRule", { locations: subscription.locations.join(", ") })
    : t("events.subscriptions.allLocations")
}

function SubscriptionRuleIcon({ subscription }: { subscription: Subscription }) {
  if (subscription.type === "ITEM_DROP") {
    const item = findItem(subscription.itemName)

    if (item) return <ItemTypeArt itemType={item.type} size={36} />
  }

  if (subscription.type === "BOOSTER")
    return <BoosterArt boosterType={subscription.boosterType} size={36} />
  if (subscription.type === "DUNGEON") return <DungeonArt size={36} />
  if (subscription.type === "GLOBAL")
    return <GlobalEventArt eventType={subscription.eventType} size={36} />
  if (subscription.type === "RAID") return <RaidArt size={36} />

  return (
    <span className="subscription-rule__icon">
      <Glyph name={subscriptionIcons[subscription.type]} size={18} />
    </span>
  )
}
