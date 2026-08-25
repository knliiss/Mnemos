import { AnimatePresence } from "framer-motion"
import { useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  createBoosterSubscription,
  createDungeonSubscription,
  createGlobalSubscription,
  createRaidSubscription,
  deleteSubscription,
} from "@/shared/api/subscriptionApi"
import { BoosterArt, DungeonArt, GlobalEventArt, RaidArt } from "../../components/events/EventArt"
import { BOOSTER_TYPES, DUNGEON_TYPES, GLOBAL_EVENT_TYPES } from "../../contracts"
import { useMnemosData } from "../../data/MnemosDataProvider"
import type { BoosterType, DungeonType, GlobalEventType, Subscription } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"
import { SubscriptionCreateSheet } from "./SubscriptionCreateSheet"
import { SubscriptionRule } from "./SubscriptionRule"
import { useSubscriptionItemTypes } from "./useSubscriptionItemTypes"
import "./notification-settings.css"

const raidLocations = Array.from({ length: 28 }, (_, index) => index + 1)

export function Subscriptions({ back }: { back: () => void }) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [creatingDrop, setCreatingDrop] = useState(false)
  const [busyKey, setBusyKey] = useState<string | null>(null)
  const mutationLocked = useRef(false)

  const subscriptions = data.subscriptions
  const itemTypesByName = useSubscriptionItemTypes(subscriptions)
  const dropRules = useMemo(
    () => subscriptions.filter((subscription) => subscription.type === "ITEM_DROP"),
    [subscriptions],
  )
  const raidRules = useMemo(
    () => subscriptions.filter((subscription) => subscription.type === "RAID"),
    [subscriptions],
  )
  const raidActiveLocations = useMemo(() => {
    if (raidRules.some((subscription) => subscription.locations.length === 0)) {
      return new Set(raidLocations)
    }

    return new Set(raidRules.flatMap((subscription) => subscription.locations))
  }, [raidRules])

  async function mutate(key: string, mutation: () => Promise<void>) {
    if (mutationLocked.current) {
      return
    }

    mutationLocked.current = true
    setBusyKey(key)

    try {
      await data.mutateSubscriptions(mutation)
    } finally {
      mutationLocked.current = false
      setBusyKey(null)
    }
  }

  async function toggleBooster(value: BoosterType) {
    const matches = subscriptions.filter(
      (subscription) => subscription.type === "BOOSTER" && subscription.boosterType === value,
    )

    await mutate(`BOOSTER:${value}`, async () => {
      if (matches.length > 0) {
        for (const subscription of matches) {
          await deleteSubscription(subscription.id)
        }
        return
      }

      await createBoosterSubscription(value)
    })
  }

  async function toggleDungeon(value: DungeonType) {
    const matches = subscriptions.filter(
      (subscription) => subscription.type === "DUNGEON" && subscription.dungeonType === value,
    )

    await mutate(`DUNGEON:${value}`, async () => {
      if (matches.length > 0) {
        for (const subscription of matches) {
          await deleteSubscription(subscription.id)
        }
        return
      }

      await createDungeonSubscription(value)
    })
  }

  async function toggleGlobal(value: GlobalEventType) {
    const matches = subscriptions.filter(
      (subscription) => subscription.type === "GLOBAL" && subscription.eventType === value,
    )

    await mutate(`GLOBAL:${value}`, async () => {
      if (matches.length > 0) {
        for (const subscription of matches) {
          await deleteSubscription(subscription.id)
        }
        return
      }

      await createGlobalSubscription(value)
    })
  }

  async function toggleRaidLocation(location: number) {
    const nextLocations = new Set(raidActiveLocations)

    if (nextLocations.has(location)) {
      nextLocations.delete(location)
    } else {
      nextLocations.add(location)
    }

    await mutate(`RAID:${location}`, async () => {
      for (const subscription of raidRules) {
        await deleteSubscription(subscription.id)
      }

      if (nextLocations.size > 0) {
        await createRaidSubscription([...nextLocations].sort((left, right) => left - right))
      }
    })
  }

  async function removeDrop(subscription: Subscription) {
    if (subscription.type !== "ITEM_DROP") {
      return
    }

    await mutate(`DROP:${subscription.id}`, () => deleteSubscription(subscription.id))
  }

  if (data.loadingSubscriptions && subscriptions.length === 0) {
    return (
      <div className="screen notification-settings-screen">
        <PageHeader title={t("settings.notifications")} back={back} />
        <FeatureLoading label={t("events.subscriptions.loading")} />
      </div>
    )
  }

  return (
    <div className="screen notification-settings-screen">
      <PageHeader title={t("settings.notifications")} back={back} />

      <section className="notification-settings-intro">
        <img src="/ui-illustrations/mnemos-mascot-event.svg" alt="" aria-hidden="true" />
        <span>
          <small>{t("events.subscriptions.eyebrow")}</small>
          <strong>{t("events.subscriptions.mascotTitle")}</strong>
          <p>{t("events.subscriptions.mascotCopy")}</p>
        </span>
      </section>

      {data.subscriptionsError ? (
        <section className="feature-empty notification-settings-error">
          <strong>{t("events.subscriptions.errorTitle")}</strong>
          <small>{t("events.subscriptions.errorCopy")}</small>
          <button type="button" onClick={() => void data.refreshSubscriptions()}>
            {t("events.subscriptions.retry")}
          </button>
        </section>
      ) : (
        <>
          <NotificationGroup title={t("events.kinds.GLOBAL")} tone="global">
            {GLOBAL_EVENT_TYPES.map((value) => {
              const enabled = subscriptions.some(
                (subscription) =>
                  subscription.type === "GLOBAL" && subscription.eventType === value,
              )

              return (
                <NotificationToggle
                  key={value}
                  label={t(`events.globals.${value}`)}
                  enabled={enabled}
                  busy={busyKey === `GLOBAL:${value}`}
                  disabled={busyKey !== null}
                  art={<GlobalEventArt eventType={value} size={39} />}
                  onToggle={() => void toggleGlobal(value)}
                />
              )
            })}
          </NotificationGroup>

          <NotificationGroup title={t("events.kinds.BOOSTER")} tone="booster">
            {BOOSTER_TYPES.map((value) => {
              const enabled = subscriptions.some(
                (subscription) =>
                  subscription.type === "BOOSTER" && subscription.boosterType === value,
              )

              return (
                <NotificationToggle
                  key={value}
                  label={t(`events.boosters.${value}`)}
                  enabled={enabled}
                  busy={busyKey === `BOOSTER:${value}`}
                  disabled={busyKey !== null}
                  art={<BoosterArt boosterType={value} size={39} />}
                  onToggle={() => void toggleBooster(value)}
                />
              )
            })}
          </NotificationGroup>

          <NotificationGroup title={t("events.kinds.DUNGEON")} tone="dungeon">
            {DUNGEON_TYPES.map((value) => {
              const enabled = subscriptions.some(
                (subscription) =>
                  subscription.type === "DUNGEON" && subscription.dungeonType === value,
              )

              return (
                <NotificationToggle
                  key={value}
                  label={t(`events.dungeons.${value}`)}
                  enabled={enabled}
                  busy={busyKey === `DUNGEON:${value}`}
                  disabled={busyKey !== null}
                  art={<DungeonArt size={39} />}
                  onToggle={() => void toggleDungeon(value)}
                />
              )
            })}
          </NotificationGroup>

          <section className="notification-settings-group notification-settings-raid">
            <header>
              <span className="notification-settings-group__art">
                <RaidArt size={42} />
              </span>
              <span>
                <small>{t("events.kinds.RAID")}</small>
                <strong>{t("events.subscriptions.locations")}</strong>
              </span>
              <b>{raidActiveLocations.size}/28</b>
            </header>

            <div className="notification-settings-locations">
              {raidLocations.map((location) => {
                const enabled = raidActiveLocations.has(location)

                return (
                  <button
                    type="button"
                    key={location}
                    className={enabled ? "is-active" : undefined}
                    aria-pressed={enabled}
                    disabled={busyKey !== null}
                    onClick={() => void toggleRaidLocation(location)}
                  >
                    <span>{location}</span>
                    <i />
                  </button>
                )
              })}
            </div>
          </section>

          <section className="notification-settings-group notification-settings-drops">
            <header>
              <span>
                <small>{t("events.kinds.ITEM_DROP")}</small>
                <strong>{t("events.subscriptions.rules")}</strong>
                <p>{t("events.subscriptions.rulesCopy")}</p>
              </span>
              <button
                type="button"
                className="notification-settings-drops__add"
                disabled={busyKey !== null}
                onClick={() => setCreatingDrop(true)}
              >
                <Glyph name="plus" size={16} />
                {t("events.subscriptions.new")}
              </button>
            </header>

            <div className="notification-settings-drop-rules">
              {dropRules.map((subscription) => (
                <SubscriptionRule
                  key={subscription.id}
                  subscription={subscription}
                  itemTypesByName={itemTypesByName}
                  deleting={busyKey === `DROP:${subscription.id}`}
                  disabled={busyKey !== null}
                  onRemove={() => void removeDrop(subscription)}
                />
              ))}

              {dropRules.length === 0 ? (
                <button
                  type="button"
                  className="notification-settings-drop-empty"
                  onClick={() => setCreatingDrop(true)}
                >
                  <Glyph name="plus" size={16} />
                  <span>{t("events.subscriptions.emptyCopy")}</span>
                </button>
              ) : null}
            </div>
          </section>
        </>
      )}

      <AnimatePresence>
        {creatingDrop ? (
          <SubscriptionCreateSheet fixedType="ITEM_DROP" onClose={() => setCreatingDrop(false)} />
        ) : null}
      </AnimatePresence>
    </div>
  )
}

function NotificationGroup({
  title,
  tone,
  children,
}: {
  title: string
  tone: string
  children: React.ReactNode
}) {
  return (
    <section className={`notification-settings-group is-${tone}`}>
      <header>
        <span>
          <small>{title}</small>
          <strong>{title}</strong>
        </span>
      </header>
      <div className="notification-settings-options">{children}</div>
    </section>
  )
}

function NotificationToggle({
  label,
  enabled,
  busy,
  disabled,
  art,
  onToggle,
}: {
  label: string
  enabled: boolean
  busy: boolean
  disabled: boolean
  art: React.ReactNode
  onToggle(): void
}) {
  return (
    <button
      type="button"
      className={`notification-settings-toggle ${enabled ? "is-active" : ""}`}
      aria-pressed={enabled}
      disabled={disabled}
      onClick={onToggle}
    >
      <span className="notification-settings-toggle__art">{art}</span>
      <strong>{label}</strong>
      <span className="notification-settings-switch" aria-hidden="true">
        <i className={busy ? "is-busy" : undefined} />
      </span>
    </button>
  )
}
