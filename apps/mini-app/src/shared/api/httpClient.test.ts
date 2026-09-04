import { describe, expect, it } from "vitest"
import { ApiError, getUserBanDetails } from "./httpClient"

describe("getUserBanDetails", () => {
  it("extracts structured ban details from gateway response", () => {
    const error = new ApiError(403, {
      code: "USER_BANNED",
      message: "User access is suspended",
      details: {
        sanctionId: "019c1129-ef54-7000-8000-000000000431",
        reason: "Repeated abuse",
        occurredAt: "2026-09-03T01:00:00Z",
        expiresAt: "2026-09-04T01:00:00Z",
      },
    })

    expect(getUserBanDetails(error)).toEqual({
      sanctionId: "019c1129-ef54-7000-8000-000000000431",
      reason: "Repeated abuse",
      occurredAt: "2026-09-03T01:00:00Z",
      expiresAt: "2026-09-04T01:00:00Z",
    })
  })

  it("treats a missing expiry as a permanent ban", () => {
    const error = new ApiError(403, {
      code: "USER_BANNED",
      message: "User access is suspended",
      details: {
        sanctionId: "019c1129-ef54-7000-8000-000000000432",
        reason: "Permanent suspension",
        occurredAt: "2026-09-03T01:00:00Z",
      },
    })

    expect(getUserBanDetails(error)).toEqual({
      sanctionId: "019c1129-ef54-7000-8000-000000000432",
      reason: "Permanent suspension",
      occurredAt: "2026-09-03T01:00:00Z",
      expiresAt: null,
    })
  })

  it("does not classify unrelated forbidden responses as bans", () => {
    const error = new ApiError(403, { code: "ACCESS_DENIED" })

    expect(getUserBanDetails(error)).toBeNull()
  })
})
