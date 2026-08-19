import type { RealtimeEvent } from "./realtime.types"

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

      default:
        return null
    }
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
