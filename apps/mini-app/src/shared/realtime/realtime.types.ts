interface RealtimeEventEnvelope<TType extends string, TPayload> {
  id: string
  type: TType
  occurredAt: string
  operationId: string | null
  payload: TPayload
}

export type WalletTransactionCompletedEvent = RealtimeEventEnvelope<
  "wallet.transaction.completed",
  {
    requestId: string
    transactionId: string
    ownerType: string
    ownerId: string
    currency: string
    source: string
    sourceReferenceId: string
    amount: number
  }
>

export type WalletTransactionFailedEvent = RealtimeEventEnvelope<
  "wallet.transaction.failed",
  {
    requestId: string
    ownerType: string
    ownerId: string
    currency: string
    source: string
    sourceReferenceId: string
    amount: number
    reason: string
  }
>

export type BetOperationCompletedEvent = RealtimeEventEnvelope<
  "bet.operation.completed",
  {
    resourceType: "EVENT_BET" | "ITEM_RACE_PARTICIPANT"
    resourceId: string
    status: "CONFIRMED" | "REJECTED" | "FAILED"
    amount: number
    walletTransactionId: string | null
    failureReason: string | null
  }
>

export type PersonalizationPurchaseCompletedEvent = RealtimeEventEnvelope<
  "personalization.purchase.completed",
  {
    personalizationId: string
    status: "CONFIRMED" | "FAILED"
    walletTransactionId: string | null
    failureReason: string | null
  }
>

export type AccountStatusChangedEvent = RealtimeEventEnvelope<
  "account.activated" | "account.deactivated" | "account.deleted",
  {
    accountId: string
    status: "ACTIVE" | "DEACTIVATED" | "DELETED"
  }
>

export type AccountOwnershipTransferredEvent = RealtimeEventEnvelope<
  "account.owner.transferred",
  {
    accountId: string
    previousOwnerMemberId: string
    previousOwnerUserId: string
    newOwnerMemberId: string
    newOwnerUserId: string
  }
>

export type AccountMemberChangedEvent = RealtimeEventEnvelope<
  "account.member.joined" | "account.member.left" | "account.member.removed",
  {
    accountId: string
    memberId: string
    userId: string
    change: "JOINED" | "LEFT" | "REMOVED"
  }
>

export type SanctionChangedEvent = RealtimeEventEnvelope<
  "moderation.sanction.activated" | "moderation.sanction.deactivated",
  {
    sanctionId: string
    userId: string
    type: "BAN" | "NOTIFICATION_SUSPENSION"
    status: "ACTIVE" | "REVOKED" | "EXPIRED"
  }
>

export type NotificationPackBroadcastTemplate = {
  type: string
  emoji: string | null
  soloTitle: string
  soloBody: string
  compact: string
  aggregateTitle: string
}

export type DropSpotlightBroadcast = {
  id: string
  type: "drop-spotlight"
  sequence: number
  occurredAt: string
  expiresAt: string
  presentationMode: "FULL" | "COMPACT"
  payload: {
    spotlightId: string
    sequence: number
    sourceAccountId: string
    itemDropEventId: string
    droppedFor: string
    itemId: string
    itemName: string
    itemRarity: string
    itemType: string
    notificationPack: {
      mixedAggregateTitle: string
      templates: NotificationPackBroadcastTemplate[]
    } | null
    publishedAt: string
    expiresAt: string
    minimumPresentationGapSeconds: number
  }
}

export type GameEventBroadcast = {
  id: string
  type: "game-event"
  occurredAt: string
  payload: {
    eventId: string
    eventType: string
    occurredAt: string
    itemDrop: {
      itemId: string
      itemName: string
      itemType: string
      itemRarity: string
      droppedFor: string
    } | null
    raid: {
      locations: number[]
    } | null
    booster: {
      activatedBy: string
      type: string
    } | null
    global: {
      type: string
    } | null
  }
}

export type RealtimeEvent =
  | WalletTransactionCompletedEvent
  | WalletTransactionFailedEvent
  | BetOperationCompletedEvent
  | PersonalizationPurchaseCompletedEvent
  | AccountStatusChangedEvent
  | AccountOwnershipTransferredEvent
  | AccountMemberChangedEvent
  | SanctionChangedEvent

export type RealtimeBroadcast = DropSpotlightBroadcast | GameEventBroadcast

export type RealtimeEventListener = (event: RealtimeEvent) => void
export type RealtimeBroadcastListener = (event: RealtimeBroadcast) => void
