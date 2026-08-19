import type {
  AccountMemberResponse,
  AccountRoleResponse,
  AccountSummaryResponse,
  EventFeedItemResponse,
  SubscriptionResponse,
} from "@/shared/api/backendContracts"
import type {
  Account,
  AccountRole,
  FeedEvent,
  Member,
  RolePermission,
  Subscription,
} from "../model"

const SKIN_BASE_URL = "https://webdata.c7x.dev/textures/skin"

export function toProductAccount(account: AccountSummaryResponse, coin = 0): Account {
  return {
    id: account.id,
    cristalixUuid: account.cristalixUuid,
    nickname: account.cristalixNickname,
    role: account.owner ? "Власник" : "Учасник",
    skinUrl: `${SKIN_BASE_URL}/${account.cristalixUuid}`,
    memberCount: account.memberCount,
    maxMembers: account.maxMembers,
    owner: account.owner,
    active: account.status === "ACTIVE",
    coin,
  }
}

export function toProductMember(member: AccountMemberResponse, roleIds?: string[]): Member {
  return {
    id: member.id,
    name: member.name,
    owner: member.owner,
    joinedAt: member.joinedAt,
    highestRoleId: member.highestRole?.id ?? null,
    roleIds: roleIds ?? (member.highestRole ? [member.highestRole.id] : []),
  }
}

export function toProductRole(role: AccountRoleResponse): AccountRole {
  const permissions = Object.fromEntries(
    Object.entries(role.permissions).map(([permission, enabled]) => [permission, Boolean(enabled)]),
  ) as Record<RolePermission, boolean>

  return {
    id: role.id,
    name: role.name,
    color: role.color,
    hierarchyRank: role.hierarchyRank,
    permissions,
  }
}

export function toProductFeedEvent(event: EventFeedItemResponse): FeedEvent {
  if (event.type === "ITEM_DROP") {
    return {
      id: event.eventId,
      kind: "ITEM_DROP",
      title: event.itemName,
      detail: event.droppedFor,
      occurredAt: event.occurredAt,
      relative: event.occurredAt,
      meta: event.itemType,
      rarity: event.itemRarity,
    }
  }

  if (event.type === "BOOSTER") {
    return {
      id: event.eventId,
      kind: "BOOSTER",
      title: event.boosterType,
      detail: event.activatedBy,
      occurredAt: event.occurredAt,
      relative: event.occurredAt,
      meta: "BOOSTER",
    }
  }

  if (event.type === "GLOBAL") {
    return {
      id: event.eventId,
      kind: "GLOBAL",
      title: event.globalType,
      detail: "GLOBAL",
      occurredAt: event.occurredAt,
      relative: event.occurredAt,
    }
  }

  return {
    id: event.eventId,
    kind: "RAID",
    title: "RAID",
    detail: event.locations.join(" · "),
    occurredAt: event.occurredAt,
    relative: event.occurredAt,
    meta: `${event.locations.length} locations`,
  }
}

export function toProductSubscription(subscription: SubscriptionResponse): Subscription | null {
  if (subscription.type === "ITEM_DROP") {
    return {
      id: subscription.id,
      type: "ITEM_DROP",
      droppedFor: subscription.droppedFor ?? undefined,
      itemName: subscription.itemName ?? undefined,
    }
  }

  if (subscription.type === "BOOSTER" && subscription.boosterType) {
    return {
      id: subscription.id,
      type: "BOOSTER",
      boosterType: subscription.boosterType,
    }
  }

  if (subscription.type === "DUNGEON" && subscription.dungeonType) {
    return {
      id: subscription.id,
      type: "DUNGEON",
      dungeonType: subscription.dungeonType,
    }
  }

  if (subscription.type === "GLOBAL" && subscription.eventType) {
    return {
      id: subscription.id,
      type: "GLOBAL",
      eventType: subscription.eventType,
    }
  }

  if (subscription.type === "RAID") {
    return {
      id: subscription.id,
      type: "RAID",
      locations: subscription.locations ?? [],
    }
  }

  return null
}
