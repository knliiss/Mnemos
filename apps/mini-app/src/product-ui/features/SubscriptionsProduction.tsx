import { AnimatePresence } from "framer-motion"
import { useMemo, useState } from "react"
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
import {
  BOOSTER_TYPES,
  DUNGEON_TYPES,
  GLOBAL_EVENT_TYPES,
  SUBSCRIPTION_TYPES,
} from "../contracts"
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
import {
  boosterLabels,
  dungeonLabels,
  globalEventLabels,
  itemTypeLabels,
} from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { BottomSheet, ChoiceGrid, Field, PageHeader } from "../shared/Primitives"

const subscriptionLabels: Record<SubscriptionType, string> = {
  ITEM_DROP: "Дроп",
  BOOSTER: "Бустер",
  DUNGEON: "Данж",
  RAID: "Рейд",
  GLOBAL: "Глобальна",
}

const subscriptionIcons: Record<SubscriptionType, "item-drop" | "booster" | "dungeon" | "raid" | "global"> = {
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

export function SubscriptionsProduction({ back }: { back: () => void }) {
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
  const droppedForValid = !cleanDroppedFor || (
    cleanDroppedFor.length >= 4
    && cleanDroppedFor.length <= 20
    && /^[\p{L}0-9_]+$/u.test(cleanDroppedFor)
  )
  const itemNameValid = !cleanItemName || cleanItemName.length <= 24
  const itemDropValid = droppedForValid && itemNameValid && Boolean(cleanDroppedFor || cleanItemName)

  const itemSuggestions = useMemo(() => {
    const query = cleanItemName.toLowerCase()
    if (query.length < 2) return []

    return itemCatalog
      .filter((item) => item.name.toLowerCase().includes(query))
      .slice(0, 4)
  }, [cleanItemName])

  const counts = useMemo(() => SUBSCRIPTION_TYPES
    .map((subscriptionType) => ({
      type: subscriptionType,
      count: subscriptions.filter((subscription) => subscription.type === subscriptionType).length,
    }))
    .filter((entry) => entry.count > 0), [subscriptions])

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
      <PageHeader title="Підписки" back={back} />

      <section className="subscription-summary">
        <span className="subscription-summary__icon"><Glyph name="bell" size={22} /></span>
        <span><strong>{subscriptions.length}</strong><small>активних правил</small></span>
        <button disabled={busy} onClick={() => setCreating(true)}><Glyph name="plus" size={17} />Нове</button>
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
        <header><h2>Правила</h2><small>Сповіщення приходять, коли умова збігається</small></header>
        <div>
          {subscriptions.map((subscription) => (
            <article
              className={`subscription-rule event-row-surface event-surface--${subscriptionVisualTone(subscription)}`}
              key={subscription.id}
            >
              <SubscriptionRuleIcon subscription={subscription} />
              <span className="subscription-rule__copy">
                <strong>{subscriptionLabels[subscription.type]}</strong>
                <small>{description(subscription)}</small>
              </span>
              <button
                aria-label="Видалити"
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
              <strong>Підписок ще немає</strong>
              <small>Створи правило для потрібної події.</small>
            </div>
          ) : null}
        </div>
      </section>

      <AnimatePresence>
        {creating ? (
          <BottomSheet title="Нова підписка" onClose={() => !busy && setCreating(false)}>
            <div className="form-stack subscription-form-redesign">
              <Field label="Що відстежувати">
                <ChoiceGrid value={type} options={SUBSCRIPTION_TYPES} onChange={setType} labels={subscriptionLabels} />
              </Field>

              {type === "ITEM_DROP" ? (
                <>
                  <Field
                    label="Гравець"
                    hint={cleanDroppedFor && !droppedForValid
                      ? "4–20 символів: літери, цифри, _"
                      : "Можна залишити порожнім, якщо вибраний предмет"}
                  >
                    <input
                      value={droppedFor}
                      onChange={(event) => setDroppedFor(event.target.value)}
                      placeholder="nickname"
                      minLength={4}
                      maxLength={20}
                    />
                  </Field>
                  <Field label="Предмет" hint="Гравець або предмет — достатньо одного">
                    <input
                      value={itemName}
                      onChange={(event) => setItemName(event.target.value)}
                      placeholder="назва предмета"
                      maxLength={24}
                    />
                    {itemSuggestions.length ? (
                      <div className="field-suggestions">
                        {itemSuggestions.map((item) => (
                          <button key={item.name} type="button" onClick={() => setItemName(item.name)}>
                            <ItemTypeArt itemType={item.type} size={28} />
                            <span>
                              <strong>{item.name}</strong>
                              <small>{itemTypeLabels[item.type]}</small>
                            </span>
                          </button>
                        ))}
                      </div>
                    ) : null}
                  </Field>
                </>
              ) : null}

              {type === "BOOSTER" ? (
                <Field label="Тип бустера">
                  <ChoiceGrid value={boosterType} options={BOOSTER_TYPES} onChange={setBoosterType} labels={boosterLabels} />
                </Field>
              ) : null}

              {type === "DUNGEON" ? (
                <Field label="Складність">
                  <ChoiceGrid value={dungeonType} options={DUNGEON_TYPES} onChange={setDungeonType} labels={dungeonLabels} />
                </Field>
              ) : null}

              {type === "GLOBAL" ? (
                <Field label="Подія">
                  <ChoiceGrid value={globalType} options={GLOBAL_EVENT_TYPES} onChange={setGlobalType} labels={globalEventLabels} />
                </Field>
              ) : null}

              {type === "RAID" ? (
                <Field label="Локації" hint="Порожньо — всі локації">
                  <div className="inline-input">
                    <input
                      type="number"
                      min="1"
                      step="1"
                      value={raidInput}
                      onChange={(event) => setRaidInput(event.target.value)}
                      placeholder="№"
                    />
                    <button type="button" aria-label="Додати локацію" onClick={addRaidLocation}>
                      <Glyph name="plus" size={17} />
                    </button>
                  </div>
                  <div className="token-list">
                    {raidLocations.map((location) => (
                      <button
                        key={location}
                        type="button"
                        onClick={() => setRaidLocations((current) => current.filter((value) => value !== location))}
                      >
                        {location}<Glyph name="close" size={12} />
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
                {busy ? "Зберігаємо…" : "Зберегти правило"}
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

function description(subscription: Subscription) {
  if (subscription.type === "ITEM_DROP") {
    if (subscription.droppedFor && subscription.itemName) return `${subscription.droppedFor} · ${subscription.itemName}`
    if (subscription.droppedFor) return `Гравець: ${subscription.droppedFor}`
    return `Предмет: ${subscription.itemName}`
  }

  if (subscription.type === "BOOSTER") return boosterLabels[subscription.boosterType]
  if (subscription.type === "DUNGEON") return dungeonLabels[subscription.dungeonType]
  if (subscription.type === "GLOBAL") return globalEventLabels[subscription.eventType]

  return subscription.locations.length ? `Локації ${subscription.locations.join(", ")}` : "Усі локації"
}

function SubscriptionRuleIcon({ subscription }: { subscription: Subscription }) {
  if (subscription.type === "ITEM_DROP") {
    const item = findItem(subscription.itemName)

    if (item) return <ItemTypeArt itemType={item.type} size={36} />
  }

  if (subscription.type === "BOOSTER") return <BoosterArt boosterType={subscription.boosterType} size={36} />
  if (subscription.type === "DUNGEON") return <DungeonArt size={36} />
  if (subscription.type === "GLOBAL") return <GlobalEventArt eventType={subscription.eventType} size={36} />
  if (subscription.type === "RAID") return <RaidArt size={36} />

  return (
    <span className="subscription-rule__icon">
      <Glyph name={subscriptionIcons[subscription.type]} size={18} />
    </span>
  )
}
