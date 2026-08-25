import { DropSpotlightPreview } from "../../components/spotlight/DropSpotlightPresentation"
import type { Personalization } from "../../model"
import { renderNotificationTemplate, valuesForVariant } from "./notificationPackPreview"

type Props = {
  item: Personalization
  size: "CARD" | "DETAIL" | "STUDIO" | "THUMBNAIL"
}

export function NotificationPackSpotlightPreview({ item, size }: Props) {
  const content = item.content

  if (!content || content.kind !== "NOTIFICATION_PACK") {
    return null
  }

  const template = content.templates.ITEM_DROP
  const values = valuesForVariant("ITEM_DROP", null)
  const spotlightTemplate = {
    emoji: template.emoji,
    soloTitle: renderNotificationTemplate(content, template.soloTitle, values),
    soloBody: renderNotificationTemplate(content, template.soloBody, values),
    compact: renderNotificationTemplate(content, template.compact, values),
  }

  return (
    <DropSpotlightPreview
      size={size}
      presentationMode="FULL"
      itemName={values.item ?? "Вечный клинок"}
      droppedFor={values.player ?? "north"}
      itemRarity={values.rarity ?? "MYTHICAL"}
      itemType={values.itemType ?? "SWORD"}
      template={spotlightTemplate}
    />
  )
}
