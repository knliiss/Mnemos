export type InviteDeliveryResult = "SHARED" | "COPIED" | "CANCELLED"

export type InviteDeliveryPlatform = {
  share?: (data: ShareData) => Promise<void>
  clipboard?: {
    writeText(text: string): Promise<void>
  }
}

export async function copyInviteCodeToClipboard(
  code: string,
  platform: InviteDeliveryPlatform = navigator,
): Promise<void> {
  if (!platform.clipboard) {
    throw new Error("Clipboard API is unavailable")
  }

  await platform.clipboard.writeText(code)
}

export async function deliverInviteCode(
  code: string,
  platform: InviteDeliveryPlatform = navigator,
): Promise<InviteDeliveryResult> {
  if (platform.share) {
    try {
      await platform.share({
        title: "Mnemos",
        text: code,
      })

      return "SHARED"
    } catch (error) {
      if (isAbortError(error)) {
        return "CANCELLED"
      }
    }
  }

  await copyInviteCodeToClipboard(code, platform)

  return "COPIED"
}

function isAbortError(error: unknown): boolean {
  return (
    typeof error === "object" && error !== null && "name" in error && error.name === "AbortError"
  )
}
