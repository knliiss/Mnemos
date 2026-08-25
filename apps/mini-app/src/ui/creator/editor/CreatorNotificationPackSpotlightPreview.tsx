import type { Personalization } from "../../model"
import { NotificationPackSpotlightPreview } from "../../personalization/components/NotificationPackSpotlightPreview"

type Props = {
  item: Personalization
}

export function CreatorNotificationPackSpotlightPreview({ item }: Props) {
  return <NotificationPackSpotlightPreview item={item} size="STUDIO" />
}
