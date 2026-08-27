import type { NotificationTemplateKey } from "../../model"
import { BoosterArt, DungeonArt, GlobalEventArt, ItemTypeArt, RaidArt } from "../events/EventArt"
import { PlayerOfflineArt } from "../people/PlayerOfflineArt"

type Props = {
  templateKey: NotificationTemplateKey
  size: number
}

export function NotificationTemplateArt({ templateKey, size }: Props) {
  if (templateKey === "ITEM_DROP") {
    return <ItemTypeArt itemType="SWORD" size={size} />
  }

  if (templateKey === "BOOSTER") {
    return <BoosterArt boosterType="LUCK" size={size} />
  }

  if (templateKey === "DUNGEON") {
    return <DungeonArt size={size} />
  }

  if (templateKey === "RAID") {
    return <RaidArt size={size} />
  }

  if (templateKey === "GLOBAL") {
    return <GlobalEventArt eventType="ECLIPSE" size={size} />
  }

  return <PlayerOfflineArt size={size} />
}
