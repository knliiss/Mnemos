// @vitest-environment jsdom

import { act, renderHook, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { getCreatorPersonalization } from "@/shared/api/creatorPersonalizationApi"
import {
  activatePersonalization,
  deactivatePersonalization,
  getPersonalization,
  listOwnedPersonalizations,
  type PersonalizationResponse,
} from "@/shared/api/personalizationApi"
import { useUserNotificationPacks } from "./useUserNotificationPacks"

vi.mock("@/shared/api/creatorPersonalizationApi", () => ({
  getCreatorPersonalization: vi.fn(),
}))

vi.mock("@/shared/api/personalizationApi", () => ({
  activatePersonalization: vi.fn(),
  deactivatePersonalization: vi.fn(),
  getPersonalization: vi.fn(),
  listOwnedPersonalizations: vi.fn(),
}))

const publicPack: PersonalizationResponse = {
  id: "pack-public",
  creatorUserId: "creator",
  type: "NOTIFICATION_PACK",
  origin: "MARKETPLACE",
  visibility: "PUBLIC",
  name: "Quiet Pack",
  content: {
    colors: [{ color: "#CBFF2D", position: 0 }],
    angleDegrees: 0,
    glow: null,
  },
  price: null,
  status: "PUBLISHED",
  rejectionReason: null,
  createdAt: "2026-08-23T00:00:00Z",
  updatedAt: "2026-08-23T00:00:00Z",
}

const privatePack: PersonalizationResponse = {
  ...publicPack,
  id: "pack-private",
  name: "Creator Pack",
  status: "PRIVATE",
}

describe("useUserNotificationPacks", () => {
  beforeEach(() => {
    vi.mocked(listOwnedPersonalizations).mockReset()
    vi.mocked(getPersonalization).mockReset()
    vi.mocked(getCreatorPersonalization).mockReset()
    vi.mocked(activatePersonalization).mockReset()
    vi.mocked(deactivatePersonalization).mockReset()
    vi.stubGlobal("requestAnimationFrame", (callback: FrameRequestCallback) => {
      callback(0)
      return 1
    })
    vi.stubGlobal("cancelAnimationFrame", vi.fn())
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  it("does not load ownership until the User Hub is opened", async () => {
    vi.mocked(listOwnedPersonalizations).mockResolvedValue({
      items: [],
      page: 0,
      size: 100,
      totalItems: 0,
      totalPages: 0,
      hasNextPage: false,
    })

    const { rerender } = renderHook(({ enabled }) => useUserNotificationPacks(enabled), {
      initialProps: { enabled: false },
    })

    expect(listOwnedPersonalizations).not.toHaveBeenCalled()

    rerender({ enabled: true })

    await waitFor(() => {
      expect(listOwnedPersonalizations).toHaveBeenCalledWith("NOTIFICATION_PACK", 0, 100)
    })
  })

  it("hydrates private creator packs and keeps only one pack active after activation", async () => {
    vi.mocked(listOwnedPersonalizations).mockResolvedValue({
      items: [
        {
          personalizationId: publicPack.id,
          ownerType: "USER",
          ownerId: "user",
          type: "NOTIFICATION_PACK",
          acquiredAt: "2026-08-23T00:00:00Z",
          active: true,
        },
        {
          personalizationId: privatePack.id,
          ownerType: "USER",
          ownerId: "user",
          type: "NOTIFICATION_PACK",
          acquiredAt: "2026-08-23T00:00:00Z",
          active: false,
        },
      ],
      page: 0,
      size: 100,
      totalItems: 2,
      totalPages: 1,
      hasNextPage: false,
    })
    vi.mocked(getPersonalization)
      .mockResolvedValueOnce(publicPack)
      .mockRejectedValueOnce(new Error("not public"))
    vi.mocked(getCreatorPersonalization).mockResolvedValue(privatePack)
    vi.mocked(activatePersonalization).mockResolvedValue({
      personalizationId: privatePack.id,
      ownerType: "USER",
      ownerId: "user",
      type: "NOTIFICATION_PACK",
      acquiredAt: "2026-08-23T00:00:00Z",
      active: true,
    })

    const { result } = renderHook(() => useUserNotificationPacks(true))

    await waitFor(() => {
      expect(result.current.loading).toBe(false)
      expect(result.current.packs.map((pack) => pack.name)).toEqual(["Quiet Pack", "Creator Pack"])
    })

    await act(async () => {
      await result.current.activate(privatePack.id)
    })

    expect(result.current.activePack?.id).toBe(privatePack.id)
    expect(result.current.packs.filter((pack) => pack.active)).toHaveLength(1)
    expect(getCreatorPersonalization).toHaveBeenCalledWith(privatePack.id)
  })
})
