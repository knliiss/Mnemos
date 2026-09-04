export type UserBanDetails = {
  sanctionId: string
  reason: string
  occurredAt: string
  expiresAt: string | null
}

type UserBanListener = (ban: UserBanDetails) => void

class UserBanEventBus {
  private readonly listeners = new Set<UserBanListener>()
  private currentBan: UserBanDetails | null = null

  publish(ban: UserBanDetails) {
    this.currentBan = ban
    this.listeners.forEach((listener) => listener(ban))
  }

  clear() {
    this.currentBan = null
  }

  subscribe(listener: UserBanListener): () => void {
    this.listeners.add(listener)

    if (this.currentBan) {
      listener(this.currentBan)
    }

    return () => {
      this.listeners.delete(listener)
    }
  }
}

export const userBanEvents = new UserBanEventBus()
