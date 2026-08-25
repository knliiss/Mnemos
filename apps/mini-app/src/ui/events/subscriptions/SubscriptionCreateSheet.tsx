import { useTranslation } from "react-i18next"
import {
  BOOSTER_TYPES,
  DUNGEON_TYPES,
  GLOBAL_EVENT_TYPES,
  SUBSCRIPTION_TYPES,
} from "../../contracts"
import type { BoosterType, DungeonType, GlobalEventType, SubscriptionType } from "../../model"
import { BottomSheet, ChoiceGrid, Field } from "../../shared/Primitives"
import { SubscriptionItemDropFields } from "./SubscriptionItemDropFields"
import { SubscriptionRaidFields } from "./SubscriptionRaidFields"
import { useSubscriptionCreateDraft } from "./useSubscriptionCreateDraft"

type SubscriptionCreateSheetProps = {
  onClose(): void
  fixedType?: SubscriptionType
}

export function SubscriptionCreateSheet({
  onClose,
  fixedType,
}: SubscriptionCreateSheetProps) {
  const { t } = useTranslation()
  const draft = useSubscriptionCreateDraft(onClose, fixedType)

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

  return (
    <BottomSheet
      title={fixedType === "ITEM_DROP" ? t("events.kinds.ITEM_DROP") : t("events.subscriptions.newTitle")}
      onClose={() => !draft.busy && onClose()}
    >
      <div className="form-stack subscription-form">
        {!fixedType ? (
          <Field label={t("events.subscriptions.track")}>
            <ChoiceGrid
              value={draft.type}
              options={SUBSCRIPTION_TYPES}
              onChange={draft.setType}
              labels={subscriptionLabels}
            />
          </Field>
        ) : null}

        {draft.type === "ITEM_DROP" ? (
          <SubscriptionItemDropFields
            droppedFor={draft.droppedFor}
            itemName={draft.itemName}
            cleanDroppedFor={draft.cleanDroppedFor}
            droppedForValid={draft.droppedForValid}
            itemSuggestions={draft.itemSuggestions}
            onDroppedForChange={draft.setDroppedFor}
            onItemNameChange={draft.setItemName}
            onItemSelect={draft.selectItem}
          />
        ) : null}

        {draft.type === "BOOSTER" ? (
          <Field label={t("events.subscriptions.boosterType")}>
            <ChoiceGrid
              value={draft.boosterType}
              options={BOOSTER_TYPES}
              onChange={draft.setBoosterType}
              labels={boosterLabels}
            />
          </Field>
        ) : null}

        {draft.type === "DUNGEON" ? (
          <Field label={t("events.subscriptions.difficulty")}>
            <ChoiceGrid
              value={draft.dungeonType}
              options={DUNGEON_TYPES}
              onChange={draft.setDungeonType}
              labels={dungeonLabels}
            />
          </Field>
        ) : null}

        {draft.type === "GLOBAL" ? (
          <Field label={t("events.subscriptions.event")}>
            <ChoiceGrid
              value={draft.globalType}
              options={GLOBAL_EVENT_TYPES}
              onChange={draft.setGlobalType}
              labels={globalLabels}
            />
          </Field>
        ) : null}

        {draft.type === "RAID" ? (
          <SubscriptionRaidFields
            raidInput={draft.raidInput}
            raidLocations={draft.raidLocations}
            cleanRaidInput={draft.cleanRaidInput}
            raidInputValid={draft.raidInputValid}
            onRaidInputChange={draft.setRaidInput}
            onAddLocation={draft.addRaidLocation}
            onRemoveLocation={draft.removeRaidLocation}
          />
        ) : null}

        <button
          type="button"
          className="primary-action"
          disabled={draft.busy || !draft.createValid}
          onClick={() => void draft.create()}
        >
          {draft.busy ? t("events.subscriptions.saving") : t("events.subscriptions.save")}
        </button>
      </div>
    </BottomSheet>
  )
}
