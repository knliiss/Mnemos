import type {
  DropSpotlightBroadcast,
  GameEventBroadcast,
  RealtimeBroadcast,
  RealtimeEvent,
} from "./realtime.types"

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i
const INSTANT_PATTERN = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{1,9})?Z$/

export function parseRealtimeEvent(data: string): RealtimeEvent | null {
  try {
    const candidate = JSON.parse(data) as unknown

    if (!isRealtimeEventEnvelope(candidate)) {
      return null
    }

    switch (candidate.type) {
      case "wallet.transaction.completed":
        return isWalletCompletedPayload(candidate.payload) && candidate.operationId === null
          ? (candidate as RealtimeEvent)
          : null

      case "wallet.transaction.failed":
        return isWalletFailedPayload(candidate.payload) && candidate.operationId === null
          ? (candidate as RealtimeEvent)
          : null

      case "bet.operation.completed":
        return isBetCompletedPayload(candidate.payload) && typeof candidate.operationId === "string"
          ? (candidate as RealtimeEvent)
          : null

      case "personalization.purchase.completed":
        return isPersonalizationPurchaseCompletedPayload(candidate.payload) &&
          typeof candidate.operationId === "string"
          ? (candidate as RealtimeEvent)
          : null

      case "account.activated":
        return isAccountStatusChangedPayload(candidate.payload, "ACTIVE") &&
          candidate.operationId === null
          ? (candidate as RealtimeEvent)
          : null

      case "account.deactivated":
        return isAccountStatusChangedPayload(candidate.payload, "DEACTIVATED") &&
          candidate.operationId === null
          ? (candidate as RealtimeEvent)
          : null

      case "account.deleted":
        return isAccountStatusChangedPayload(candidate.payload, "DELETED") &&
          candidate.operationId === null
          ? (candidate as RealtimeEvent)
          : null

      case "account.owner.transferred":
        return isAccountOwnershipTransferredPayload(candidate.payload) &&
          candidate.operationId === null
          ? (candidate as RealtimeEvent)
          : null

      case "account.member.joined":
        return isAccountMemberChangedPayload(candidate.payload, "JOINED") &&
          candidate.operationId === null
          ? (candidate as RealtimeEvent)
          : null

      case "account.member.left":
        return isAccountMemberChangedPayload(candidate.payload, "LEFT") &&
          candidate.operationId === null
          ? (candidate as RealtimeEvent)
          : null

      case "account.member.removed":
        return isAccountMemberChangedPayload(candidate.payload, "REMOVED") &&
          candidate.operationId === null
          ? (candidate as RealtimeEvent)
          : null

      default:
        return null
    }
  } catch {
    return null
  }
}

export function parseRealtimeBroadcast(eventName: string, data: string): RealtimeBroadcast | null {
  try {
    const candidate = JSON.parse(data) as unknown

    if (eventName === "drop-spotlight" && isDropSpotlightBroadcast(candidate)) {
      return candidate
    }

    if (eventName === "game-event" && isGameEventBroadcast(candidate)) {
      return candidate
    }

    return null
  } catch {
    return null
  }
}

function isRealtimeEventEnvelope(value: unknown): value is {
  id: string
  type: string
  occurredAt: string
  operationId: string | null
  payload: Record<string, unknown>
} {
  return (
    isRecord(value) &&
    isUuid(value.id) &&
    typeof value.type === "string" &&
    isInstant(value.occurredAt) &&
    (isUuid(value.operationId) || value.operationId === null) &&
    isRecord(value.payload)
  )
}

function isDropSpotlightBroadcast(value: unknown): value is DropSpotlightBroadcast {
  if (
    !isRecord(value) ||
    !isUuid(value.id) ||
    value.type !== "drop-spotlight" ||
    !isNonNegativeSafeInteger(value.sequence) ||
    !isInstant(value.occurredAt) ||
    !isInstant(value.expiresAt) ||
    (value.presentationMode !== "FULL" && value.presentationMode !== "COMPACT") ||
    !isRecord(value.payload)
  ) {
    return false
  }

  const payload = value.payload

  return (
    isUuid(payload.spotlightId) &&
    isNonNegativeSafeInteger(payload.sequence) &&
    payload.sequence === value.sequence &&
    isUuid(payload.sourceAccountId) &&
    isUuid(payload.itemDropEventId) &&
    isNonBlankString(payload.droppedFor) &&
    isUuid(payload.itemId) &&
    isNonBlankString(payload.itemName) &&
    isNonBlankString(payload.itemRarity) &&
    isNonBlankString(payload.itemType) &&
    isNotificationPackSnapshot(payload.notificationPack) &&
    isInstant(payload.publishedAt) &&
    isInstant(payload.expiresAt) &&
    isNonNegativeSafeInteger(payload.minimumPresentationGapSeconds)
  )
}

function isGameEventBroadcast(value: unknown): value is GameEventBroadcast {
  if (
    !isRecord(value) ||
    !isUuid(value.id) ||
    value.type !== "game-event" ||
    !isInstant(value.occurredAt) ||
    !isRecord(value.payload)
  ) {
    return false
  }

  const payload = value.payload
  const itemDropValid = payload.itemDrop === null || isGameItemDrop(payload.itemDrop)
  const raidValid = payload.raid === null || isGameRaid(payload.raid)
  const boosterValid = payload.booster === null || isGameBooster(payload.booster)
  const globalValid = payload.global === null || isGameGlobal(payload.global)
  const populatedPayloads = [
    payload.itemDrop,
    payload.raid,
    payload.booster,
    payload.global,
  ].filter((entry) => entry !== null)

  return (
    isUuid(payload.eventId) &&
    isNonBlankString(payload.eventType) &&
    isInstant(payload.occurredAt) &&
    itemDropValid &&
    raidValid &&
    boosterValid &&
    globalValid &&
    populatedPayloads.length === 1
  )
}

function isNotificationPackSnapshot(value: unknown): boolean {
  if (value === null) {
    return true
  }

  return (
    isRecord(value) &&
    typeof value.mixedAggregateTitle === "string" &&
    Array.isArray(value.templates) &&
    value.templates.every(isNotificationPackTemplate)
  )
}

function isNotificationPackTemplate(value: unknown): boolean {
  return (
    isRecord(value) &&
    isNonBlankString(value.type) &&
    (value.emoji === null || typeof value.emoji === "string") &&
    typeof value.soloTitle === "string" &&
    typeof value.soloBody === "string" &&
    typeof value.compact === "string" &&
    typeof value.aggregateTitle === "string"
  )
}

function isGameItemDrop(value: unknown): boolean {
  return (
    isRecord(value) &&
    isUuid(value.itemId) &&
    isNonBlankString(value.itemName) &&
    isNonBlankString(value.itemType) &&
    isNonBlankString(value.itemRarity) &&
    isNonBlankString(value.droppedFor)
  )
}

function isGameRaid(value: unknown): boolean {
  return (
    isRecord(value) &&
    Array.isArray(value.locations) &&
    value.locations.every((location) => Number.isSafeInteger(location))
  )
}

function isGameBooster(value: unknown): boolean {
  return isRecord(value) && isNonBlankString(value.activatedBy) && isNonBlankString(value.type)
}

function isGameGlobal(value: unknown): boolean {
  return isRecord(value) && isNonBlankString(value.type)
}

function isWalletCompletedPayload(payload: Record<string, unknown>): boolean {
  return hasWalletFields(payload) && isUuid(payload.transactionId)
}

function isWalletFailedPayload(payload: Record<string, unknown>): boolean {
  return hasWalletFields(payload) && isNonBlankString(payload.reason)
}

function hasWalletFields(payload: Record<string, unknown>): boolean {
  return (
    isUuid(payload.requestId) &&
    isNonBlankString(payload.ownerType) &&
    isUuid(payload.ownerId) &&
    isNonBlankString(payload.currency) &&
    isNonBlankString(payload.source) &&
    isUuid(payload.sourceReferenceId) &&
    isPositiveSafeInteger(payload.amount)
  )
}

function isBetCompletedPayload(payload: Record<string, unknown>): boolean {
  return (
    (payload.resourceType === "EVENT_BET" || payload.resourceType === "ITEM_RACE_PARTICIPANT") &&
    isUuid(payload.resourceId) &&
    (payload.status === "CONFIRMED" ||
      payload.status === "REJECTED" ||
      payload.status === "FAILED") &&
    isPositiveSafeInteger(payload.amount) &&
    isNullableUuid(payload.walletTransactionId) &&
    isNullableString(payload.failureReason)
  )
}

function isPersonalizationPurchaseCompletedPayload(payload: Record<string, unknown>): boolean {
  if (!isUuid(payload.personalizationId)) {
    return false
  }

  if (payload.status === "CONFIRMED") {
    return isUuid(payload.walletTransactionId) && payload.failureReason === null
  }

  if (payload.status === "FAILED") {
    return payload.walletTransactionId === null && isNonBlankString(payload.failureReason)
  }

  return false
}

function isAccountStatusChangedPayload(
  payload: Record<string, unknown>,
  expectedStatus: "ACTIVE" | "DEACTIVATED" | "DELETED",
): boolean {
  return isUuid(payload.accountId) && payload.status === expectedStatus
}

function isAccountOwnershipTransferredPayload(payload: Record<string, unknown>): boolean {
  return (
    isUuid(payload.accountId) &&
    isUuid(payload.previousOwnerMemberId) &&
    isUuid(payload.previousOwnerUserId) &&
    isUuid(payload.newOwnerMemberId) &&
    isUuid(payload.newOwnerUserId) &&
    payload.previousOwnerMemberId !== payload.newOwnerMemberId
  )
}

function isAccountMemberChangedPayload(
  payload: Record<string, unknown>,
  expectedChange: "JOINED" | "LEFT" | "REMOVED",
): boolean {
  return (
    isUuid(payload.accountId) &&
    isUuid(payload.memberId) &&
    isUuid(payload.userId) &&
    payload.change === expectedChange
  )
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null
}

function isNullableString(value: unknown): value is string | null {
  return value === null || isNonBlankString(value)
}

function isNonBlankString(value: unknown): value is string {
  return typeof value === "string" && value.trim().length > 0
}

function isPositiveSafeInteger(value: unknown): value is number {
  return typeof value === "number" && Number.isSafeInteger(value) && value > 0
}

function isNonNegativeSafeInteger(value: unknown): value is number {
  return typeof value === "number" && Number.isSafeInteger(value) && value >= 0
}

function isUuid(value: unknown): value is string {
  return typeof value === "string" && UUID_PATTERN.test(value)
}

function isNullableUuid(value: unknown): value is string | null {
  return value === null || isUuid(value)
}

function isInstant(value: unknown): value is string {
  return (
    typeof value === "string" && INSTANT_PATTERN.test(value) && Number.isFinite(Date.parse(value))
  )
}
