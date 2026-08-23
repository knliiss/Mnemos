import { describe, expect, it, vi } from "vitest"
import {
  copyInviteCodeToClipboard,
  deliverInviteCode,
  type InviteDeliveryPlatform,
} from "./inviteDelivery"

describe("inviteDelivery", () => {
  it("uses the native share sheet when it is available", async () => {
    const share = vi.fn().mockResolvedValue(undefined)
    const writeText = vi.fn().mockResolvedValue(undefined)
    const platform: InviteDeliveryPlatform = {
      share,
      clipboard: { writeText },
    }

    const result = await deliverInviteCode("ABCD-EFGH-IJKL-MNPQ", platform)

    expect(result).toBe("SHARED")
    expect(share).toHaveBeenCalledWith({
      title: "Mnemos",
      text: "ABCD-EFGH-IJKL-MNPQ",
    })
    expect(writeText).not.toHaveBeenCalled()
  })

  it("falls back to the clipboard when native sharing fails", async () => {
    const share = vi.fn().mockRejectedValue(new Error("share unavailable"))
    const writeText = vi.fn().mockResolvedValue(undefined)
    const platform: InviteDeliveryPlatform = {
      share,
      clipboard: { writeText },
    }

    const result = await deliverInviteCode("ABCD-EFGH-IJKL-MNPQ", platform)

    expect(result).toBe("COPIED")
    expect(writeText).toHaveBeenCalledWith("ABCD-EFGH-IJKL-MNPQ")
  })

  it("does not copy or report an error when the native share sheet is cancelled", async () => {
    const share = vi.fn().mockRejectedValue({ name: "AbortError" })
    const writeText = vi.fn().mockResolvedValue(undefined)
    const platform: InviteDeliveryPlatform = {
      share,
      clipboard: { writeText },
    }

    const result = await deliverInviteCode("ABCD-EFGH-IJKL-MNPQ", platform)

    expect(result).toBe("CANCELLED")
    expect(writeText).not.toHaveBeenCalled()
  })

  it("copies directly when native sharing is unavailable", async () => {
    const writeText = vi.fn().mockResolvedValue(undefined)
    const platform: InviteDeliveryPlatform = {
      clipboard: { writeText },
    }

    const result = await deliverInviteCode("ABCD-EFGH-IJKL-MNPQ", platform)

    expect(result).toBe("COPIED")
    expect(writeText).toHaveBeenCalledWith("ABCD-EFGH-IJKL-MNPQ")
  })

  it("fails explicitly when clipboard access is unavailable", async () => {
    await expect(copyInviteCodeToClipboard("ABCD-EFGH-IJKL-MNPQ", {})).rejects.toThrow(
      "Clipboard API is unavailable",
    )
  })
})
