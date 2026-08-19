import { AnimatePresence } from "framer-motion"
import { useMemo, useState } from "react"
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
import { subscriptionVisualTone } from "../eventVisualTone"
import { initialSubscriptions } from "../mockData"
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

    if (item) {
      return <ItemTypeArt itemType={item.type} size={36} />
    }
  }

  if (subscription.type === "BOOSTER") {
    return <BoosterArt boosterType={subscription.boosterType} size={36} />
  }

  if (subscription.type === "DUNGEON") {
    return <DungeonArt size={36} />
  }

  if (subscription.type === "GLOBAL") {
    return <GlobalEventArt eventType={subscription.eventType} size={36} />
  }

  if (subscription.type === "RAID") {
    return <RaidArt size={36} />
  }

  return (
    <span className="subscription-rule__icon">
      <Glyph name={subscriptionIcons[subscription.type]} size={18} />
    </span>
  )
}

export function SubscriptionsPage({ back }: { back: () => void }) {
  const [subscriptions, setSubscriptions] = useState(initialSubscriptions)
  const [creating, setCreating] = useState(false)
  const [type, setType] = useState<SubscriptionType>("ITEM_DROP")
  const [droppedFor, setDroppedFor] = useState("")
  const [itemName, setItemName] = useState("")
  const [boosterType, setBoosterType] = useState<BoosterType>("LUCK")
  const [dungeonType, setDungeonType] = useState<DungeonType>("EASY")
  const [globalType, setGlobalType] = useState<GlobalEventType>("ECLIPSE")
  const [raidLocations, setRaidLocations] = useState<number[]>([])
  const [raidInput, setRaidInput] = useState("")

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
    const query = itemName.trim().toLowerCase()
    if (query.length < 2) return []

    return itemCatalog
      .filter((item) => item.name.toLowerCase().includes(query))
      .slice(0, 4)
  }, [itemName])

  const counts = useMemo(() => {
    return SUBSCRIPTION_TYPES.map((subscriptionType) => ({
      type: subscriptionType,
      count: subscriptions.filter((subscription) => subscription.type === subscriptionType).length,
    })).filter((entry) => entry.count > 0)
  }, [subscriptions])

  const addRaidLocation = () => {
    const value = Number(raidInput)

    if (Number.isInteger(value) && value > 0 && !raidLocations.includes(value)) {
      setRaidLocations([...raidLocations, value])
    }

    setRaidInput("")
  }

  const create = () => {
    const id = `sub-${Date.now()}`
    let next: Subscription

    if (type === "ITEM_DROP") {
      if (!itemDropValid) return

      next = {
        id,
        type,
        droppedFor: cleanDroppedFor || undefined,
        itemName: cleanItemName || undefined,
      }
    } else if (type === "BOOSTER") {
      next = { id, type, boosterType }
    } else if (type === "DUNGEON") {
      next = { id, type, dungeonType }
    } else if (type === "GLOBAL") {
      next = { id, type, eventType: globalType }
    } else {
      next = { id, type, locations: raidLocations }
    }

    setSubscriptions([next, ...subscriptions])
    setCreating(false)
  }

  const createDisabled = type === "ITEM_DROP" && !itemDropValid

  return (
    <div className="screen subscriptions-redesign-screen">
      <PageHeader title="Підписки" back={back} />

      <section className="subscription-summary">
        <span className="subscription-summary__icon"><Glyph name="bell" size={22} /></span>
        <span><strong>{subscriptions.length}</strong><small>активних правил</small></span>
        <button onClick={() => setCreating(true)}><Glyph name="plus" size={17} />Нове</button>
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
          {subscriptions.map((subscription) => {
            const tone = subscriptionVisualTone(subscription)

            return (
              <article
                className={`subscription-rule event-row-surface event-surface--${tone}`}
                key={subscription.id}
              >
                <SubscriptionRuleIcon subscription={subscription} />
                <span className="subscription-rule__copy">
                  <strong>{subscriptionLabels[subscription.type]}</strong>
                  <small>{description(subscription)}</small>
                </span>
                <button aria-label="Видалити" onClick={() => setSubscriptions(subscriptions.filter((item) => item.id !== subscription.id))}>
                  <Glyph name="close" size={15} />
                </button>
              </article>
            )
          })}
        </div>
      </section>

      <AnimatePresence>
        {creating ? (
          <BottomSheet title="Нова підписка" onClose={() => setCreating(false)}>
            <div className="form-stack subscription-form-redesign">
              <Field label="Що відстежувати">
                <ChoiceGrid value={type} options={SUBSCRIPTION_TYPES} onChange={setType} labels={subscriptionLabels} />
              </Field>

              {type === "ITEM_DROP" ? (
                <>
                  <Field label="Гравець" hint={cleanDroppedFor && !droppedForValid ? "4–20 символів: літери, цифри, _" : "Можна залишити порожнім, якщо вибраний предмет"}>
                    <input value={droppedFor} onChange={(event) => setDroppedFor(event.target.value)} placeholder="nickname" minLength={4} maxLength={20} />
                  </Field>
                  <Field label="Предмет" hint="Гравець або предмет — достатньо одного">
                    <input value={itemName} onChange={(event) => setItemName(event.target.value)} placeholder="назва предмета" maxLength={24} />
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

              {type === "BOOSTER" ? <Field label="Тип бустера"><ChoiceGrid value={boosterType} options={BOOSTER_TYPES} onChange={setBoosterType} labels={boosterLabels} /></Field> : null}
              {type === "DUNGEON" ? <Field label="Складність"><ChoiceGrid value={dungeonType} options={DUNGEON_TYPES} onChange={setDungeonType} labels={dungeonLabels} /></Field> : null}
              {type === "GLOBAL" ? <Field label="Подія"><ChoiceGrid value={globalType} options={GLOBAL_EVENT_TYPES} onChange={setGlobalType} labels={globalEventLabels} /></Field> : null}

              {type === "RAID" ? (
                <Field label="Локації" hint="Порожньо — всі локації">
                  <div className="inline-input">
                    <input type="number" min="1" step="1" value={raidInput} onChange={(event) => setRaidInput(event.target.value)} placeholder="№" />
                    <button type="button" aria-label="Додати локацію" onClick={addRaidLocation}><Glyph name="plus" size={17} /></button>
                  </div>
                  <div className="token-list">
                    {raidLocations.map((location) => (
                      <button key={location} type="button" onClick={() => setRaidLocations(raidLocations.filter((value) => value !== location))}>
                        {location}<Glyph name="close" size={12} />
                      </button>
                    ))}
                  </div>
                </Field>
              ) : null}

              <button className="primary-action" disabled={createDisabled} onClick={create}>Зберегти правило</button>
            </div>
          </BottomSheet>
        ) : null}
      </AnimatePresence>
    </div>
  )
}
