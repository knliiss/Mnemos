interface RealtimeEventEnvelope<
    TType extends string,
    TPayload,
> {
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

export type RealtimeEvent =
    | WalletTransactionCompletedEvent
    | WalletTransactionFailedEvent
    | BetOperationCompletedEvent
    | PersonalizationPurchaseCompletedEvent

export type RealtimeEventListener = (event: RealtimeEvent) => void
