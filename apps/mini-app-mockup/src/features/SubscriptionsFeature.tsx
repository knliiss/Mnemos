import { AnimatePresence } from "framer-motion"
import { useMemo, useState } from "react"
import {
  BOOSTER_TYPES,
  DUNGEON_TYPES,
  GLOBAL_EVENT_TYPES,
  SUBSCRIPTION_TYPES,
} from "../contracts"
import { initialSubscriptions } from "../mockData"
import type {
  BoosterType,
  DungeonType,
  GlobalEventType,
  Subscription,
  SubscriptionType,
} from "../model"
import { boosterLabels, dungeonLabels, globalEventLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { BottomSheet, ChoiceGrid, Field, IconButton, PageHeader } from "../shared/Primitives"

const subscriptionLabels: Record<SubscriptionType, string> = {
  ITEM_DROP: "Дроп",
  BOOSTER: "Бустер",
  DUNGEON: "Данж",
  RAID: "Рейд",
  GLOBAL: "Глобал",
}

const itemCatalog = [
  { name: "Eternal Blade", type: "Sword" },
  { name: "Moon Necklace", type: "Jewelry" },
  { name: "Black Flame", type: "Aura" },
  { name: "Observer", type: "Pet" },
  { name: "Aincrad Notes", type: "Book" },
]

function description(subscription: Subscription) {
  if (subscription.type === "ITEM_DROP") {
    return [subscription.droppedFor, subscription.itemName].filter(Boolean).join(" · ")
  }

  if (subscription.type === "BOOSTER") return boosterLabels[subscription.boosterType]
  if (subscription.type === "DUNGEON") return dungeonLabels[subscription.dungeonType]
  if (subscription.type === "GLOBAL") return globalEventLabels[subscription.eventType]

  return subscription.locations.length ? subscription.locations.join(" · ") : "Усі"
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
    <div className="screen">
      <PageHeader
        title="Підписки"
        back={back}
        actions={<IconButton icon="plus" label="Додати підписку" onClick={() => setCreating(true)} />}
      />

      <div className="plain-list">
        {subscriptions.map((subscription) => (
          <div className="subscription-row" key={subscription.id}>
            <span className="subscription-row__type">{subscriptionLabels[subscription.type]}</span>
            <span className="subscription-row__copy">{description(subscription)}</span>
            <button
              className="micro-action micro-action--danger"
              aria-label="Видалити"
              onClick={() => setSubscriptions(subscriptions.filter((item) => item.id !== subscription.id))}
            >
              <Glyph name="trash" size={16} />
            </button>
          </div>
        ))}
      </div>

      <AnimatePresence>
        {creating ? (
          <BottomSheet title="Підписка" onClose={() => setCreating(false)}>
            <div className="form-stack">
              <Field label="Тип">
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
                    label="Гравець"
                    hint={cleanDroppedFor && !droppedForValid ? "4–20 символів: літери, цифри, _" : undefined}
                  >
                    <input
                      value={droppedFor}
                      onChange={(event) => setDroppedFor(event.target.value)}
                      placeholder="нікнейм"
                      minLength={4}
                      maxLength={20}
                    />
                  </Field>

                  <Field label="Предмет" hint="Гравець або предмет — достатньо одного">
                    <input
                      value={itemName}
                      onChange={(event) => setItemName(event.target.value)}
                      placeholder="пошук"
                      maxLength={24}
                    />
                    {itemSuggestions.length ? (
                      <div className="field-suggestions">
                        {itemSuggestions.map((item) => (
                          <button key={item.name} type="button" onClick={() => setItemName(item.name)}>
                            <strong>{item.name}</strong>
                            <small>{item.type}</small>
                          </button>
                        ))}
                      </div>
                    ) : null}
                  </Field>
                </>
              ) : null}

              {type === "BOOSTER" ? (
                <Field label="Бустер">
                  <ChoiceGrid value={boosterType} options={BOOSTER_TYPES} onChange={setBoosterType} labels={boosterLabels} />
                </Field>
              ) : null}

              {type === "DUNGEON" ? (
                <Field label="Данж">
                  <ChoiceGrid value={dungeonType} options={DUNGEON_TYPES} onChange={setDungeonType} labels={dungeonLabels} />
                </Field>
              ) : null}

              {type === "GLOBAL" ? (
                <Field label="Подія">
                  <ChoiceGrid value={globalType} options={GLOBAL_EVENT_TYPES} onChange={setGlobalType} labels={globalEventLabels} />
                </Field>
              ) : null}

              {type === "RAID" ? (
                <Field label="Локації" hint="Порожньо — усі">
                  <div className="inline-input">
                    <input
                      type="number"
                      min="1"
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
                        onClick={() => setRaidLocations(raidLocations.filter((value) => value !== location))}
                      >
                        {location}
                        <Glyph name="close" size={12} />
                      </button>
                    ))}
                  </div>
                </Field>
              ) : null}

              <button className="primary-action" disabled={createDisabled} onClick={create}>Додати</button>
            </div>
          </BottomSheet>
        ) : null}
      </AnimatePresence>
    </div>
  )
}
