import { beforeEach, describe, expect, it, vi } from "vitest"
import type { RemoteGroupState } from "@/shared/api/botAgentApi"
import { synchronizeBotGroups } from "./botAgentGroupSync"

const apiMock = vi.hoisted(() => ({
  get: vi.fn(),
  save: vi.fn(),
  worker: vi.fn(),
}))

vi.mock("@/shared/api/botAgentApi", () => ({
  getBotGroups: apiMock.get,
  saveBotGroups: apiMock.save,
  workerRequest: apiMock.worker,
}))

const empty = { groups: [], assignments: {} }
const desktopGroups = {
  groups: [{ id: "g-desktop", name: "Existing desktop group" }],
  assignments: { "00000000-0000-0000-0000-000000000001": "g-desktop" },
}

describe("C7x account group reconciliation", () => {
  beforeEach(() => {
    vi.clearAllMocks()
    apiMock.save.mockImplementation(async (_id: string, value: RemoteGroupState) => value)
  })

  it("imports old C7x desktop groups when server is empty", async () => {
    apiMock.get.mockResolvedValue({ revision: 0, state: empty })
    apiMock.worker.mockResolvedValueOnce({
      state: desktopGroups,
      revision: 4,
      initialized: true,
    })
    await synchronizeBotGroups("agent")
    expect(apiMock.save).toHaveBeenCalledWith("agent", {
      revision: 0,
      state: {
        ...desktopGroups,
        syncedWorkerRevision: 4,
        syncedDbRevision: 1,
      },
    })
    expect(apiMock.worker).toHaveBeenCalledTimes(1)
  })

  it("propagates a newer C7x desktop revision to Mnemos", async () => {
    apiMock.get.mockResolvedValue({
      revision: 3,
      state: { ...empty, syncedWorkerRevision: 2, syncedDbRevision: 3 },
    })
    apiMock.worker.mockResolvedValueOnce({
      revision: 4,
      state: desktopGroups,
      initialized: true,
    })
    await synchronizeBotGroups("agent")
    expect(apiMock.save).toHaveBeenCalledWith("agent", {
      revision: 3,
      state: {
        ...desktopGroups,
        syncedWorkerRevision: 4,
        syncedDbRevision: 4,
      },
    })
  })

  it("blocks conflicting independent edits instead of deleting groups", async () => {
    apiMock.get.mockResolvedValue({
      revision: 5,
      state: { ...empty, syncedWorkerRevision: 2, syncedDbRevision: 3 },
    })
    apiMock.worker.mockResolvedValueOnce({
      revision: 4,
      state: desktopGroups,
      initialized: true,
    })
    await expect(synchronizeBotGroups("agent")).rejects.toThrow("No data was overwritten")
    expect(apiMock.save).not.toHaveBeenCalled()
    expect(apiMock.worker).toHaveBeenCalledTimes(1)
  })

  it("pushes offline Mnemos edits to the desktop when the agent reconnects", async () => {
    apiMock.get.mockResolvedValue({
      revision: 5,
      state: { ...desktopGroups, syncedWorkerRevision: 2, syncedDbRevision: 3 },
    })
    apiMock.worker
      .mockResolvedValueOnce({ revision: 2, state: empty, initialized: true })
      .mockResolvedValueOnce({ revision: 3, state: desktopGroups, initialized: true })
    await synchronizeBotGroups("agent")
    expect(apiMock.worker).toHaveBeenNthCalledWith(2, "agent", "POST", "/api/groups", {
      revision: 2,
      state: desktopGroups,
    })
    expect(apiMock.save).toHaveBeenCalledWith("agent", {
      revision: 5,
      state: {
        ...desktopGroups,
        syncedWorkerRevision: 3,
        syncedDbRevision: 6,
      },
    })
  })
})
