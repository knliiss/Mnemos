import {describe, expect, it} from "vitest"
import {parseRealtimeEvent} from "./realtimeEventParser"

describe("parseRealtimeEvent", () => {
    it("accepts a typed bet completion event", () => {
        const event = parseRealtimeEvent(JSON.stringify({
            id: "019c1129-ef54-7000-8000-000000000301",
            type: "bet.operation.completed",
            occurredAt: "2026-08-15T10:00:00Z",
            operationId: "019c1129-ef54-7000-8000-000000000302",
            payload: {
                resourceType: "EVENT_BET",
                resourceId: "019c1129-ef54-7000-8000-000000000303",
                status: "CONFIRMED",
                amount: 500,
                walletTransactionId: "019c1129-ef54-7000-8000-000000000304",
                failureReason: null
            }
        }))

        expect(event?.type).toBe("bet.operation.completed")
    })

    it("accepts a personalization purchase completion event", () => {
        const event = parseRealtimeEvent(JSON.stringify({
            id: "019c1129-ef54-7000-8000-000000000305",
            type: "personalization.purchase.completed",
            occurredAt: "2026-08-15T10:00:00Z",
            operationId: "019c1129-ef54-7000-8000-000000000306",
            payload: {
                personalizationId: "019c1129-ef54-7000-8000-000000000307",
                status: "CONFIRMED",
                walletTransactionId: "019c1129-ef54-7000-8000-000000000308",
                failureReason: null
            }
        }))

        expect(event?.type).toBe("personalization.purchase.completed")
    })

    it("rejects confirmed personalization purchase without wallet transaction", () => {
        const event = parseRealtimeEvent(JSON.stringify({
            id: "019c1129-ef54-7000-8000-000000000309",
            type: "personalization.purchase.completed",
            occurredAt: "2026-08-15T10:00:00Z",
            operationId: "019c1129-ef54-7000-8000-000000000310",
            payload: {
                personalizationId: "019c1129-ef54-7000-8000-000000000311",
                status: "CONFIRMED",
                walletTransactionId: null,
                failureReason: null
            }
        }))

        expect(event).toBeNull()
    })

    it("rejects failed personalization purchase carrying wallet transaction", () => {
        const event = parseRealtimeEvent(JSON.stringify({
            id: "019c1129-ef54-7000-8000-000000000312",
            type: "personalization.purchase.completed",
            occurredAt: "2026-08-15T10:00:00Z",
            operationId: "019c1129-ef54-7000-8000-000000000313",
            payload: {
                personalizationId: "019c1129-ef54-7000-8000-000000000314",
                status: "FAILED",
                walletTransactionId: "019c1129-ef54-7000-8000-000000000315",
                failureReason: "INSUFFICIENT_BALANCE"
            }
        }))

        expect(event).toBeNull()
    })

    it("rejects a wallet event that incorrectly exposes a domain operation id", () => {
        const event = parseRealtimeEvent(JSON.stringify({
            id: "019c1129-ef54-7000-8000-000000000321",
            type: "wallet.transaction.failed",
            occurredAt: "2026-08-15T10:00:00Z",
            operationId: "019c1129-ef54-7000-8000-000000000322",
            payload: {
                requestId: "019c1129-ef54-7000-8000-000000000323",
                ownerType: "ACCOUNT",
                ownerId: "019c1129-ef54-7000-8000-000000000324",
                currency: "COIN",
                source: "BET_PLACE",
                sourceReferenceId: "019c1129-ef54-7000-8000-000000000322",
                amount: 500,
                reason: "INSUFFICIENT_BALANCE"
            }
        }))

        expect(event).toBeNull()
    })

    it("rejects malformed identifiers and unsafe monetary amounts", () => {
        const event = parseRealtimeEvent(JSON.stringify({
            id: "not-an-id",
            type: "wallet.transaction.completed",
            occurredAt: "not-an-instant",
            operationId: null,
            payload: {
                requestId: "not-an-id",
                transactionId: "not-an-id",
                ownerType: "ACCOUNT",
                ownerId: "not-an-id",
                currency: "COIN",
                source: "BET_PLACE",
                sourceReferenceId: "not-an-id",
                amount: Number.MAX_SAFE_INTEGER + 1
            }
        }))

        expect(event).toBeNull()
    })

    it("rejects timestamps that the backend cannot emit as an Instant", () => {
        const event = parseRealtimeEvent(JSON.stringify({
            id: "019c1129-ef54-7000-8000-000000000331",
            type: "wallet.transaction.failed",
            occurredAt: "2026-08-15",
            operationId: null,
            payload: {
                requestId: "019c1129-ef54-7000-8000-000000000332",
                ownerType: "ACCOUNT",
                ownerId: "019c1129-ef54-7000-8000-000000000333",
                currency: "COIN",
                source: "BET_PLACE",
                sourceReferenceId: "019c1129-ef54-7000-8000-000000000334",
                amount: 500,
                reason: "INSUFFICIENT_BALANCE"
            }
        }))

        expect(event).toBeNull()
    })
})
