import { useTranslation } from "react-i18next"
import {
  BoosterArt,
  DungeonArt,
  GlobalEventArt,
  ItemTypeArt,
  RaidArt,
} from "../../components/events/EventArt"
import { subscriptionVisualTone } from "../../eventVisualTone"
import type { BetItemType, Subscription } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import {
  normalizeSubscriptionItemName,
  subscriptionDescription,
} from "./subscriptionPresentation"

type SubscriptionRuleProps = {
  subscription: Subscription
  itemTypesByName: Record<string, BetItemType>
  deleting: boolean
  disabled: boolean
  onRemove(): void
}

export function SubscriptionRule({
  subscription,
  itemTypesByName,
  deleting,
  disabled,
  onRemove,
}: SubscriptionRuleProps) {
  const { t } = useTranslation()

  return (
    <article
      className={`subscription-rule event-row-surface event-surface--${subscriptionVisualTone(subscription)}`}
    >
      <SubscriptionRuleIcon subscription={subscription} itemTypesByName={itemTypesByName} />

      <span className="subscription-rule__copy">
        <strong>{t(`events.kinds.${subscription.type}`)}</strong>
        <small>{subscriptionDescription(t, subscription)}</small>
      </span>

      <button
        type="button"
        aria-label={t("events.subscriptions.delete")}
        disabled={disabled}
        onClick={onRemove}
      >
        {deleting ? <span>…</span> : <Glyph name="close" size={15} />}
      </button>
    </article>
  )
}

function SubscriptionRuleIcon({
  subscription,
  itemTypesByName,
}: {
  subscription: Subscription
  itemTypesByName: Record<string, BetItemType>
}) {
  if (subscription.type === "ITEM_DROP" && subscription.itemName) {
    const itemType = itemTypesByName[normalizeSubscriptionItemName(subscription.itemName)]

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
