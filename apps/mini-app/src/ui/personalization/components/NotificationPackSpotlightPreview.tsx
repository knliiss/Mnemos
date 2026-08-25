import { useTranslation } from "react-i18next"
import { DropSpotlightPreview } from "@/ui/components/spotlight/DropSpotlightPresentation"
import type { Personalization } from "@/ui/model"

type NotificationPackSpotlightPreviewProps = {
  item: Personalization
  size: "THUMBNAIL" | "CARD" | "DETAIL"
}

export function NotificationPackSpotlightPreview({
  item,
  size,
}: NotificationPackSpotlightPreviewProps) {
  const { t } = useTranslation()
  const content = item.content

  if (!content || content.kind !== "NOTIFICATION_PACK") {
    return null
  }

  return (
    <DropSpotlightPreview
      size={size}
      presentationMode={size === "THUMBNAIL" ? "COMPACT" : "FULL"}
      itemName={t("personalization.preview.samples.item")}
      droppedFor="north"
      itemRarity="MYTHICAL"
      itemType="SWORD"
      template={content.templates.ITEM_DROP}
    />
  )
}
