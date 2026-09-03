import { describe, expect, it } from "vitest"
import { parseRealtimeEvent } from "./realtimeEventParser"

const EVENT_ID = "019c1129-ef54-7000-8000-000000000401"
const ACCOUNT_ID = "019c1129-ef54-7000-8000-000000000402"
const MEMBER_ID = "019c1129-ef54-7000-8000-000000000403"
const USER_ID = "019c1129-ef54-7000-8000-000000000404"
const PREVIOUS_OWNER_MEMBER_ID = "019c1129-ef54-7000-8000-000000000405"
const PREVIOUS_OWNER_USER_ID = "019c1129-ef54-7000-8000-000000000406"
const NEW_OWNER_MEMBER_ID = "019c1129-ef54-7000-8000-000000000407"
const NEW_OWNER_USER_ID = "019c1129-ef54-7000-8000-000000000408"
const OPERATION_ID = "019c1129-ef54-7000-8000-000000000409"
const OCCURRED_AT = "2026-09-03T00:00:00Z"

type LifecycleCase = {
  type: string
  payload: Record<string, unknown>
}

const lifecycleCases: LifecycleCase[] = [
  {
    type: "account.activated",
    payload: {
      accountId: ACCOUNT_ID,
      status: "ACTIVE",
    },
  },
  {
    type: "account.deactivated",
    payload: {
      accountId: ACCOUNT_ID,
      status: "DEACTIVATED",
    },
  },
  {
    type: "account.deleted",
    payload: {
      accountId: ACCOUNT_ID,
      status: "DELETED",
    },
  },
  {
    type: "account.owner.transferred",
    payload: {
      accountId: ACCOUNT_ID,
      previousOwnerMemberId: PREVIOUS_OWNER_MEMBER_ID,
      previousOwnerUserId: PREVIOUS_OWNER_USER_ID,
      newOwnerMemberId: NEW_OWNER_MEMBER_ID,
      newOwnerUserId: NEW_OWNER_USER_ID,
    },
  },
  {
    type: "account.member.joined",
    payload: {
      accountId: ACCOUNT_ID,
      memberId: MEMBER_ID,
      userId: USER_ID,
      change: "JOINED",
    },
  },
  {
    type: "account.member.left",
    payload: {
      accountId: ACCOUNT_ID,
      memberId: MEMBER_ID,
      userId: USER_ID,
      change: "LEFT",
    },
  },
  {
    type: "account.member.removed",
    payload: {
      accountId: ACCOUNT_ID,
      memberId: MEMBER_ID,
      userId: USER_ID,
      change: "REMOVED",
    },
  },
]

describe("account lifecycle realtime parsing", () => {
  it.each(lifecycleCases)("accepts $type", ({ type, payload }) => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: EVENT_ID,
        type,
        occurredAt: OCCURRED_AT,
        operationId: null,
        payload,
      }),
    )

    expect(event?.type).toBe(type)
  })

  it("rejects lifecycle events carrying a domain operation id", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: EVENT_ID,
        type: "account.deleted",
        occurredAt: OCCURRED_AT,
        operationId: OPERATION_ID,
        payload: {
          accountId: ACCOUNT_ID,
          status: "DELETED",
        },
      }),
    )

    expect(event).toBeNull()
  })

  it("rejects a status that does not match the lifecycle event type", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: EVENT_ID,
        type: "account.deleted",
        occurredAt: OCCURRED_AT,
        operationId: null,
        payload: {
          accountId: ACCOUNT_ID,
          status: "DEACTIVATED",
        },
      }),
    )

    expect(event).toBeNull()
  })

  it("rejects a member change that does not match the lifecycle event type", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: EVENT_ID,
        type: "account.member.removed",
        occurredAt: OCCURRED_AT,
        operationId: null,
        payload: {
          accountId: ACCOUNT_ID,
          memberId: MEMBER_ID,
          userId: USER_ID,
          change: "LEFT",
        },
      }),
    )

    expect(event).toBeNull()
  })
})
