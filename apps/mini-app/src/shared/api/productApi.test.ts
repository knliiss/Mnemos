import { beforeEach, describe, expect, it, vi } from "vitest"
import { api } from "@/shared/api/httpClient"
import { getEventFeed } from "@/shared/api/productApi"

vi.mock("@/shared/api/httpClient", () => ({
  api: vi.fn(),
}))

const apiMock = vi.mocked(api)

describe("getEventFeed", () => {
  beforeEach(() => {
    apiMock.mockReset()
    apiMock.mockResolvedValue({
      items: [],
      cursor: null,
    })
  })

  it("serializes backend feed filters without client-side reinterpretation", async () => {
    await getEventFeed({
      after: "0198f6ce-e934-7000-8000-000000000001",
      types: ["ITEM_DROP"],
      search: "  knalis  ",
      droppedFor: "  knalis  ",
      itemRarity: "SECRET",
      sort: "OLDEST",
      limit: 60,
    })

    expect(apiMock).toHaveBeenCalledOnce()
    expect(apiMock).toHaveBeenCalledWith(
      "/api/v1/events/feed?after=0198f6ce-e934-7000-8000-000000000001&type=ITEM_DROP&search=knalis&droppedFor=knalis&itemRarity=SECRET&sort=OLDEST&limit=60",
    )
  })

  it("uses feed defaults when optional filters are omitted", async () => {
    await getEventFeed()

    expect(apiMock).toHaveBeenCalledWith("/api/v1/events/feed?sort=NEWEST&limit=50")
  })
})
