import { describe, expect, it } from "vitest"
import { parseRealtimeBroadcast, parseRealtimeEvent } from "./realtimeEventParser"

describe("parseRealtimeEvent", () => {
  it("accepts a typed bet completion event", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
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
          failureReason: null,
        },
      }),
    )

    expect(event?.type).toBe("bet.operation.completed")
  })

  it("accepts a personalization purchase completion event", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: "019c1129-ef54-7000-8000-000000000305",
        type: "personalization.purchase.completed",
        occurredAt: "2026-08-15T10:00:00Z",
        operationId: "019c1129-ef54-7000-8000-000000000306",
        payload: {
          personalizationId: "019c1129-ef54-7000-8000-000000000307",
          status: "CONFIRMED",
          walletTransactionId: "019c1129-ef54-7000-8000-000000000308",
          failureReason: null,
        },
      }),
    )

    expect(event?.type).toBe("personalization.purchase.completed")
  })

  it("rejects confirmed personalization purchase without wallet transaction", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: "019c1129-ef54-7000-8000-000000000309",
        type: "personalization.purchase.completed",
        occurredAt: "2026-08-15T10:00:00Z",
        operationId: "019c1129-ef54-7000-8000-000000000310",
        payload: {
          personalizationId: "019c1129-ef54-7000-8000-000000000311",
          status: "CONFIRMED",
          walletTransactionId: null,
          failureReason: null,
        },
      }),
    )

    expect(event).toBeNull()
  })

  it("rejects failed personalization purchase carrying wallet transaction", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: "019c1129-ef54-7000-8000-000000000312",
        type: "personalization.purchase.completed",
        occurredAt: "2026-08-15T10:00:00Z",
        operationId: "019c1129-ef54-7000-8000-000000000313",
        payload: {
          personalizationId: "019c1129-ef54-7000-8000-000000000314",
          status: "FAILED",
          walletTransactionId: "019c1129-ef54-7000-8000-000000000315",
          failureReason: "INSUFFICIENT_BALANCE",
        },
      }),
    )

    expect(event).toBeNull()
  })

  it("rejects a wallet event that incorrectly exposes a domain operation id", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
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
          reason: "INSUFFICIENT_BALANCE",
        },
      }),
    )

    expect(event).toBeNull()
  })

  it("rejects malformed identifiers and unsafe monetary amounts", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
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
          amount: Number.MAX_SAFE_INTEGER + 1,
        },
      }),
    )

    expect(event).toBeNull()
  })

  it("rejects timestamps that the backend cannot emit as an Instant", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
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
          reason: "INSUFFICIENT_BALANCE",
        },
      }),
    )

    expect(event).toBeNull()
  })
})

describe("parseRealtimeBroadcast", () => {
  it("accepts a drop spotlight broadcast with a notification pack snapshot", () => {
    const broadcast = parseRealtimeBroadcast(
      "drop-spotlight",
      JSON.stringify({
        id: "019c1129-ef54-7000-8000-000000000341",
        type: "drop-spotlight",
        sequence: 12,
        occurredAt: "2026-08-15T10:00:00Z",
        expiresAt: "2026-08-15T10:05:00Z",
        presentationMode: "COMPACT",
        payload: {
          spotlightId: "019c1129-ef54-7000-8000-000000000342",
          sequence: 12,
          sourceAccountId: "019c1129-ef54-7000-8000-000000000343",
          itemDropEventId: "019c1129-ef54-7000-8000-000000000344",
          droppedFor: "north",
          itemId: "019c1129-ef54-7000-8000-000000000345",
          itemName: "Moon Blade",
          itemRarity: "MYTHICAL",
          itemType: "SWORD",
          notificationPack: {
            mixedAggregateTitle: "Mnemos",
            templates: [
              {
                type: "ITEM_DROP",
                emoji: "✨",
                soloTitle: "{player} found {item}",
                soloBody: "{rarity} {itemType}",
                compact: "{player} · {item}",
                aggregateTitle: "Drops",
              },
            ],
          },
          publishedAt: "2026-08-15T10:00:00Z",
          expiresAt: "2026-08-15T10:05:00Z",
          minimumPresentationGapSeconds: 20,
        },
      }),
    )

    expect(broadcast?.type).toBe("drop-spotlight")
  })

  it("accepts a typed game event broadcast", () => {
    const broadcast = parseRealtimeBroadcast(
      "game-event",
      JSON.stringify({
        id: "019c1129-ef54-7000-8000-000000000351",
        type: "game-event",
        occurredAt: "2026-08-15T10:00:00Z",
        payload: {
          eventId: "019c1129-ef54-7000-8000-000000000352",
          eventType: "ITEM_DROP",
          occurredAt: "2026-08-15T10:00:00Z",
          itemDrop: {
            itemId: "019c1129-ef54-7000-8000-000000000353",
            itemName: "Moon Blade",
            itemType: "SWORD",
            itemRarity: "MYTHICAL",
            droppedFor: "north",
          },
          raid: null,
          booster: null,
          global: null,
        },
      }),
    )

    expect(broadcast?.type).toBe("game-event")
  })

  it("rejects a game event carrying more than one typed payload", () => {
    const broadcast = parseRealtimeBroadcast(
      "game-event",
      JSON.stringify({
        id: "019c1129-ef54-7000-8000-000000000361",
        type: "game-event",
        occurredAt: "2026-08-15T10:00:00Z",
        payload: {
          eventId: "019c1129-ef54-7000-8000-000000000362",
          eventType: "ITEM_DROP",
          occurredAt: "2026-08-15T10:00:00Z",
          itemDrop: {
            itemId: "019c1129-ef54-7000-8000-000000000363",
            itemName: "Moon Blade",
            itemType: "SWORD",
            itemRarity: "MYTHICAL",
            droppedFor: "north",
          },
          raid: null,
          booster: null,
          global: {
            type: "ECLIPSE",
          },
        },
      }),
    )

    expect(broadcast).toBeNull()
  })
})
