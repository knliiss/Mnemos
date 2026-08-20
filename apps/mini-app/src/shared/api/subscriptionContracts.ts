import type { BoosterType, DungeonType, GlobalEventType } from "@/ui/model"

export type SubscriptionResponse =
  | {
      id: string
      type: "ITEM_DROP"
      createdAt: string
      details: {
        droppedFor: string | null
        itemName: string | null
      }
    }
  | {
      id: string
      type: "BOOSTER"
      createdAt: string
      details: {
        boosterType: BoosterType
      }
    }
  | {
      id: string
      type: "DUNGEON"
      createdAt: string
      details: {
        dungeonType: DungeonType
      }
    }
  | {
      id: string
      type: "GLOBAL"
      createdAt: string
      details: {
        eventType: GlobalEventType
      }
    }
  | {
      id: string
      type: "RAID"
      createdAt: string
      details: {
        locations: number[]
      }
    }
