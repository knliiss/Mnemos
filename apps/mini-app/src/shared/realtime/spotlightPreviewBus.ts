import type { DropSpotlightBroadcast } from "./realtime.types"

type SpotlightPreviewListener = (spotlight: DropSpotlightBroadcast) => void

type CreateSpotlightPreviewInput = {
  itemName: string
  droppedFor: string
}

const listeners = new Set<SpotlightPreviewListener>()
let previewSequence = 0

export const spotlightPreviews = {
  publish(spotlight: DropSpotlightBroadcast) {
    listeners.forEach((listener) => listener(spotlight))
  },

  subscribe(listener: SpotlightPreviewListener) {
    listeners.add(listener)

    return () => {
      listeners.delete(listener)
    }
  },
}

export function createSpotlightPreview({
  itemName,
  droppedFor,
}: CreateSpotlightPreviewInput): DropSpotlightBroadcast {
  const now = new Date()
  const sequence = now.getTime() * 1_000 + (previewSequence++ % 1_000)
  const spotlightId = `spotlight-preview-${sequence}`
  const publishedAt = now.toISOString()
  const expiresAt = new Date(now.getTime() + 15_000).toISOString()

  return {
    id: spotlightId,
    type: "drop-spotlight",
    sequence,
    occurredAt: publishedAt,
    expiresAt,
    presentationMode: "FULL",
    payload: {
      spotlightId,
      sequence,
      sourceAccountId: "local-preview",
      itemDropEventId: spotlightId,
      droppedFor,
      itemId: "local-preview-sword",
      itemName,
      itemRarity: "MYTHICAL",
      itemType: "SWORD",
      notificationPack: null,
      publishedAt,
      expiresAt,
      minimumPresentationGapSeconds: 0,
    },
  }
}
