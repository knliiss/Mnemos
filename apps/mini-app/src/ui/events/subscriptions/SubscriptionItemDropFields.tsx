import { useTranslation } from "react-i18next"
import type { ItemCatalogResponse } from "@/shared/api/backendContracts"
import { ItemTypeArt } from "../../components/events/EventArt"
import { Field } from "../../shared/Primitives"

type SubscriptionItemDropFieldsProps = {
  droppedFor: string
  itemName: string
  cleanDroppedFor: string
  droppedForValid: boolean
  itemSuggestions: ItemCatalogResponse[]
  onDroppedForChange(value: string): void
  onItemNameChange(value: string): void
  onItemSelect(item: ItemCatalogResponse): void
}

export function SubscriptionItemDropFields({
  droppedFor,
  itemName,
  cleanDroppedFor,
  droppedForValid,
  itemSuggestions,
  onDroppedForChange,
  onItemNameChange,
  onItemSelect,
}: SubscriptionItemDropFieldsProps) {
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

      <Field label={t("events.subscriptions.item")} hint={t("events.subscriptions.itemHint")}>
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
