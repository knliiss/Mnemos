import type { TFunction } from "i18next"
import { AnimatePresence } from "framer-motion"
import { useEffect, useMemo, useRef, useState } from "react"
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
import {
  BoosterArt,
  DungeonArt,
  GlobalEventArt,
  ItemTypeArt,
  RaidArt,
} from "../../components/events/EventArt"
import {
  BOOSTER_TYPES,
  DUNGEON_TYPES,
  GLOBAL_EVENT_TYPES,
  SUBSCRIPTION_TYPES,
} from "../../contracts"
import { useMnemosData } from "../../data/MnemosDataProvider"
import { subscriptionVisualTone } from "../../eventVisualTone"
import type {
  BetItemType,
  BoosterType,
  DungeonType,
  GlobalEventType,
  Subscription,
  SubscriptionType,
} from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { BottomSheet, ChoiceGrid, Field, PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"

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

export function Subscriptions({ back }: { back: () => void }) {
  const { t } = useTranslation()
  const data = useMnemosData()
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
  const [itemSearch, setItemSearch] = useState<{
    query: string
    items: ItemCatalogResponse[]
  }>({ query: "", items: [] })
  const [itemTypesByName, setItemTypesByName] = useState<Record<string, BetItemType>>({})
  const mutationLocked = useRef(false)
  const mountedRef = useRef(true)

  const subscriptions = data.subscriptions
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
  const cleanRaidInput = raidInput.trim()
  const raidInputValue = Number(cleanRaidInput)
  const raidInputValid = !cleanRaidInput || (Number.isInteger(raidInputValue) && raidInputValue > 0)
  const createValid = (type !== "ITEM_DROP" || itemDropValid) && (type !== "RAID" || raidInputValid)
  const itemSuggestions = itemSearch.query === cleanItemName ? itemSearch.items : []

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
          if (cancelled) {
            return
          }

          setItemSearch({ query: cleanItemName, items })
          setItemTypesByName((current) => ({
            ...current,
            ...Object.fromEntries(items.map((item) => [normalizeItemName(item.name), item.type])),
          }))
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

  useEffect(() => {
    const unknownNames = Array.from(
      new Set(
        subscriptions
          .filter(
            (subscription): subscription is Extract<Subscription, { type: "ITEM_DROP" }> =>
              subscription.type === "ITEM_DROP" && Boolean(subscription.itemName),
          )
          .map((subscription) => subscription.itemName as string)
          .filter((name) => !itemTypesByName[normalizeItemName(name)]),
      ),
    )

    if (!unknownNames.length) {
      return
    }

    let cancelled = false

    void Promise.all(
      unknownNames.map(async (name) => {
        const matches = await searchItemCatalog(name, 8)
        const exact = matches.find(
          (item) => normalizeItemName(item.name) === normalizeItemName(name),
        )

        return exact ? ([normalizeItemName(name), exact.type] as const) : null
      }),
    )
      .then((entries) => {
        if (cancelled) {
          return
        }

        const resolved = entries.filter(
          (entry): entry is readonly [string, BetItemType] => entry !== null,
        )

        if (resolved.length) {
          setItemTypesByName((current) => ({
            ...current,
            ...Object.fromEntries(resolved),
          }))
        }
      })
      .catch((error) => {
        if (!cancelled) {
          handleError(error)
        }
      })

    return () => {
      cancelled = true
    }
  }, [itemTypesByName, subscriptions])

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
    if (!raidInputValid || !cleanRaidInput) {
      return
    }

    if (!raidLocations.includes(raidInputValue)) {
      setRaidLocations((current) => [...current, raidInputValue])
    }

    setRaidInput("")
  }

  const resetDraft = () => {
    setDroppedFor("")
    setItemName("")
    setItemSearch({ query: "", items: [] })
    setBoosterType("LUCK")
    setDungeonType("EASY")
    setGlobalType("ECLIPSE")
    setRaidLocations([])
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
        resetDraft()
        setCreating(false)
      }
    } finally {
      mutationLocked.current = false
      if (mountedRef.current) {
        setBusy(false)
      }
    }
  }

  const remove = async (subscriptionId: string) => {
    if (mutationLocked.current) {
      return
    }

    mutationLocked.current = true
    setDeletingId(subscriptionId)

    try {
      await data.removeSubscription(subscriptionId)
    } finally {
      mutationLocked.current = false
      if (mountedRef.current) {
        setDeletingId(null)
      }
    }
  }

  return (
    <div className="screen subscriptions-screen">
      <PageHeader title={t("events.subscriptions.title")} back={back} />

      <section className="subscription-summary">
        <span className="subscription-summary__icon">
          <Glyph name="bell" size={22} />
        </span>
        <span>
          <strong>{subscriptions.length}</strong>
          <small>{t("events.subscriptions.activeRules")}</small>
        </span>
        <button
          type="button"
          disabled={busy || deletingId !== null}
          onClick={() => setCreating(true)}
        >
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
          {data.loadingSubscriptions && !subscriptions.length ? (
            <FeatureLoading label={t("events.subscriptions.loading")} />
          ) : null}

          {!data.loadingSubscriptions && data.subscriptionsError ? (
            <div className="feature-empty">
              <Glyph name="bell" size={24} />
              <strong>{t("events.subscriptions.errorTitle")}</strong>
              <small>{t("events.subscriptions.errorCopy")}</small>
              <button type="button" onClick={() => void data.refreshSubscriptions()}>
                {t("events.subscriptions.retry")}
              </button>
            </div>
          ) : null}

          {subscriptions.map((subscription) => (
            <article
              className={`subscription-rule event-row-surface event-surface--${subscriptionVisualTone(subscription)}`}
              key={subscription.id}
            >
              <SubscriptionRuleIcon subscription={subscription} itemTypesByName={itemTypesByName} />
              <span className="subscription-rule__copy">
                <strong>{subscriptionLabels[subscription.type]}</strong>
                <small>{description(t, subscription)}</small>
              </span>
              <button
                type="button"
                aria-label={t("events.subscriptions.delete")}
                disabled={busy || deletingId !== null}
                onClick={() => void remove(subscription.id)}
              >
                {deletingId === subscription.id ? <span>…</span> : <Glyph name="close" size={15} />}
              </button>
            </article>
          ))}

          {!data.loadingSubscriptions && !data.subscriptionsError && !subscriptions.length ? (
            <div className="feature-empty">
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
                            key={item.id}
                            type="button"
                            onClick={() => {
                              setItemName(item.name)
                              setItemSearch({ query: item.name, items: [] })
                            }}
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
                      onChange={(event) => setRaidInput(event.target.value)}
                      placeholder="№"
                    />
                    <button
                      type="button"
                      aria-label={t("events.subscriptions.addLocation")}
                      disabled={!cleanRaidInput || !raidInputValid}
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
                type="button"
                className="primary-action"
                disabled={busy || !createValid}
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

function normalizeItemName(value: string) {
  return value.trim().toLowerCase()
}

function description(t: TFunction, subscription: Subscription) {
  if (subscription.type === "ITEM_DROP") {
    if (subscription.droppedFor && subscription.itemName) {
      return `${subscription.droppedFor} · ${subscription.itemName}`
    }

    if (subscription.droppedFor) {
      return t("events.subscriptions.playerRule", { player: subscription.droppedFor })
    }

    return t("events.subscriptions.itemRule", { item: subscription.itemName })
  }

  if (subscription.type === "BOOSTER") {
    return t(`events.boosters.${subscription.boosterType}`)
  }

  if (subscription.type === "DUNGEON") {
    return t(`events.dungeons.${subscription.dungeonType}`)
  }

  if (subscription.type === "GLOBAL") {
    return t(`events.globals.${subscription.eventType}`)
  }

  return subscription.locations.length
    ? t("events.subscriptions.locationRule", {
        locations: subscription.locations.join(", "),
      })
    : t("events.subscriptions.allLocations")
}

function SubscriptionRuleIcon({
  subscription,
  itemTypesByName,
}: {
  subscription: Subscription
  itemTypesByName: Record<string, BetItemType>
}) {
  if (subscription.type === "ITEM_DROP" && subscription.itemName) {
    const itemType = itemTypesByName[normalizeItemName(subscription.itemName)]

    if (itemType) {
      return <ItemTypeArt itemType={itemType} size={36} />
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
      <Glyph name="item-drop" size={18} />
    </span>
  )
}
