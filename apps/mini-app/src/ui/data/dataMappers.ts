import type {
  AccountMemberResponse,
  AccountRoleResponse,
  AccountSummaryResponse,
  EventFeedItemResponse,
} from "@/shared/api/backendContracts"
import type { SubscriptionResponse } from "@/shared/api/subscriptionContracts"
import type {
  Account,
  AccountRole,
  FeedEvent,
  Member,
  RolePermission,
  Subscription,
} from "../model"
import { allRolePermissions } from "../rolePermissions"

const SKIN_BASE_URL = "https://webdata.c7x.dev/textures/skin"

export function toAccount(account: AccountSummaryResponse, coin = 0): Account {
  return {
    id: account.id,
    currentMemberId: account.currentMemberId,
    cristalixUuid: account.cristalixUuid,
    nickname: account.cristalixNickname,
    role: account.owner ? "OWNER" : "MEMBER",
    skinUrl: `${SKIN_BASE_URL}/${account.cristalixUuid}`,
    memberCount: account.memberCount,
    maxMembers: account.maxMembers,
    owner: account.owner,
    active: account.status === "ACTIVE",
    coin,
  }
}

export function toMember(member: AccountMemberResponse, roleIds?: string[]): Member {
  return {
    id: member.id,
    name: member.name,
    owner: member.owner,
    joinedAt: member.joinedAt,
    highestRoleId: member.highestRole?.id ?? null,
    roleIds: roleIds ?? (member.highestRole ? [member.highestRole.id] : []),
  }
}

export function toRole(role: AccountRoleResponse): AccountRole {
  const permissions = Object.fromEntries(
    allRolePermissions.map((permission) => [permission, role.permissions[permission] === true]),
  ) as Record<RolePermission, boolean>

  return {
    id: role.id,
    name: role.name,
    color: role.color,
    hierarchyRank: role.hierarchyRank,
    permissions,
  }
}

export function toFeedEvent(event: EventFeedItemResponse): FeedEvent {
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
      localizationKey: event.itemKey ?? undefined,
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
      localizationKey: event.localizationKey,
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
      localizationKey: event.localizationKey,
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

export function toSubscription(subscription: SubscriptionResponse): Subscription {
  if (subscription.type === "ITEM_DROP") {
    return {
      id: subscription.id,
      createdAt: subscription.createdAt,
      type: "ITEM_DROP",
      droppedFor: subscription.details.droppedFor ?? undefined,
      itemName: subscription.details.itemName ?? undefined,
    }
  }

  if (subscription.type === "BOOSTER") {
    return {
      id: subscription.id,
      createdAt: subscription.createdAt,
      type: "BOOSTER",
      boosterType: subscription.details.boosterType,
    }
  }

  if (subscription.type === "DUNGEON") {
    return {
      id: subscription.id,
      createdAt: subscription.createdAt,
      type: "DUNGEON",
      dungeonType: subscription.details.dungeonType,
    }
  }

  if (subscription.type === "GLOBAL") {
    return {
      id: subscription.id,
      createdAt: subscription.createdAt,
      type: "GLOBAL",
      eventType: subscription.details.eventType,
    }
  }

  return {
    id: subscription.id,
    createdAt: subscription.createdAt,
    type: "RAID",
    locations: subscription.details.locations,
  }
}
