import { AnimatePresence } from "framer-motion"
import { useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { SUBSCRIPTION_TYPES } from "../../contracts"
import { useMnemosData } from "../../data/MnemosDataProvider"
import type { SubscriptionType } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"
import {
  sortSubscriptions,
  type SubscriptionSort,
} from "./subscriptionPresentation"
import { SubscriptionCreateSheet } from "./SubscriptionCreateSheet"
import { SubscriptionRule } from "./SubscriptionRule"
import { useSubscriptionItemTypes } from "./useSubscriptionItemTypes"

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

const sortOptions: SubscriptionSort[] = ["NEWEST", "OLDEST", "TYPE", "NAME"]

type SubscriptionFilter = "ALL" | SubscriptionType

export function Subscriptions({ back }: { back: () => void }) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [creating, setCreating] = useState(false)
  const [deletingId, setDeletingId] = useState<string | null>(null)
  const [filter, setFilter] = useState<SubscriptionFilter>("ALL")
  const [sort, setSort] = useState<SubscriptionSort>("NEWEST")
  const mutationLocked = useRef(false)
  const mountedRef = useRef(true)

  const subscriptions = data.subscriptions
  const itemTypesByName = useSubscriptionItemTypes(subscriptions)
  const counts = useMemo(
    () =>
      SUBSCRIPTION_TYPES.map((type) => ({
        type,
        count: subscriptions.filter((subscription) => subscription.type === type).length,
      })).filter((entry) => entry.count > 0),
    [subscriptions],
  )
  const visibleSubscriptions = useMemo(() => {
    const filtered =
      filter === "ALL"
        ? subscriptions
        : subscriptions.filter((subscription) => subscription.type === filter)

    return sortSubscriptions(filtered, sort, t)
  }, [filter, sort, subscriptions, t])

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

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
        <span className="subscription-summary__art" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-event.svg" alt="" />
        </span>

        <span className="subscription-summary__copy">
          <small>{t("events.subscriptions.eyebrow")}</small>
          <strong>{t("events.subscriptions.mascotTitle")}</strong>
          <span>{t("events.subscriptions.mascotCopy")}</span>
        </span>

        <span className="subscription-summary__count">
          <strong>{subscriptions.length}</strong>
          <small>{t("events.subscriptions.activeRules")}</small>
        </span>

        <button type="button" disabled={deletingId !== null} onClick={() => setCreating(true)}>
          <Glyph name="plus" size={17} />
          {t("events.subscriptions.new")}
        </button>
      </section>

      <div className="subscription-toolbar">
        <div className="subscription-counts" aria-label={t("events.subscriptions.filterAria")}>
          <button
            type="button"
            className={filter === "ALL" ? "is-active" : undefined}
            onClick={() => setFilter("ALL")}
          >
            <Glyph name="bell" size={14} />
            {t("events.subscriptions.all")}
            <b>{subscriptions.length}</b>
          </button>

          {counts.map((entry) => (
            <button
              key={entry.type}
              type="button"
              className={filter === entry.type ? "is-active" : undefined}
              onClick={() => setFilter(entry.type)}
            >
              <Glyph name={subscriptionIcons[entry.type]} size={14} />
              {t(`events.kinds.${entry.type}`)}
              <b>{entry.count}</b>
            </button>
          ))}
        </div>

        <label className="subscription-sort">
          <span>{t("events.subscriptions.sortLabel")}</span>
          <select
            aria-label={t("events.subscriptions.sortAria")}
            value={sort}
            onChange={(event) => setSort(event.target.value as SubscriptionSort)}
          >
            {sortOptions.map((option) => (
              <option key={option} value={option}>
                {t(`events.subscriptions.sort.${option.toLowerCase()}`)}
              </option>
            ))}
          </select>
        </label>
      </div>

      <section className="subscription-rules">
        <header>
          <span>
            <h2>{t("events.subscriptions.rules")}</h2>
            <small>{t("events.subscriptions.rulesCopy")}</small>
          </span>
          <b>{t("events.subscriptions.shownRules", { count: visibleSubscriptions.length })}</b>
        </header>

        <div>
          {data.loadingSubscriptions && !subscriptions.length ? (
            <FeatureLoading label={t("events.subscriptions.loading")} />
          ) : null}

          {!data.loadingSubscriptions && data.subscriptionsError ? (
            <div className="feature-empty subscription-empty-state">
              <img src="/ui-illustrations/mnemos-mascot-404.svg" alt="" aria-hidden="true" />
              <strong>{t("events.subscriptions.errorTitle")}</strong>
              <small>{t("events.subscriptions.errorCopy")}</small>
              <button type="button" onClick={() => void data.refreshSubscriptions()}>
                {t("events.subscriptions.retry")}
              </button>
            </div>
          ) : null}

          {visibleSubscriptions.map((subscription) => (
            <SubscriptionRule
              key={subscription.id}
              subscription={subscription}
              itemTypesByName={itemTypesByName}
              deleting={deletingId === subscription.id}
              disabled={deletingId !== null}
              onRemove={() => void remove(subscription.id)}
            />
          ))}

          {!data.loadingSubscriptions && !data.subscriptionsError && !visibleSubscriptions.length ? (
            <div className="feature-empty subscription-empty-state">
              <img src="/ui-illustrations/mnemos-mascot-search.svg" alt="" aria-hidden="true" />
              <strong>{t("events.subscriptions.emptyTitle")}</strong>
              <small>{t("events.subscriptions.emptyCopy")}</small>
            </div>
          ) : null}
        </div>
      </section>

      <AnimatePresence>
        {creating ? <SubscriptionCreateSheet onClose={() => setCreating(false)} /> : null}
      </AnimatePresence>
    </div>
  )
}
