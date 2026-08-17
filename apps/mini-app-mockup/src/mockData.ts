import type {
  Account,
  AccountRole,
  BetRound,
  FeedEvent,
  ItemRace,
  Member,
  Personalization,
  ProfileComment,
  PublicProfile,
  RolePermission,
  Subscription,
  SystemRole,
  WalletTransaction,
} from "./model"

export const currentUser = {
  id: "0198f6ce-e934-7000-8000-000000000001",
  username: "knalis",
  language: "UK" as const,
  prime: 132,
  systemRoles: [
    "SUPER_ADMIN",
    "PERSONALIZATION_CREATOR",
    "PERSONALIZATION_MODERATOR",
  ] as SystemRole[],
}

export const skinUrl =
  "https://webdata.c7x.dev/textures/skin/46d7a80e-74f5-11eb-acca-1cb72caa35fd"

export const accounts: Account[] = [
  {
    id: "46d7a80e-74f5-11eb-acca-1cb72caa35fd",
    cristalixUuid: "46d7a80e-74f5-11eb-acca-1cb72caa35fd",
    nickname: "knalis",
    role: "Власник",
    skinUrl,
    memberCount: 4,
    maxMembers: 8,
    owner: true,
    coin: 12_480,
  },
  {
    id: "0f4e8d3c-2a9a-4e9d-9d18-7cb58ec00f18",
    cristalixUuid: "0f4e8d3c-2a9a-4e9d-9d18-7cb58ec00f18",
    nickname: "Knaliss",
    role: "Учасник",
    skinUrl,
    memberCount: 3,
    maxMembers: 6,
    owner: false,
    coin: 4_860,
  },
]

export const allPermissions: RolePermission[] = [
  "VIEW_ACCOUNT_SETTINGS",
  "EDIT_ACCOUNT_SETTINGS",
  "MANAGE_ACCOUNT_INVITES",
  "VIEW_MEMBERS",
  "INVITE_MEMBERS",
  "REMOVE_MEMBERS",
  "ASSIGN_MEMBER_ROLES",
  "KICK_MEMBERS",
  "VIEW_ROLES",
  "CREATE_ROLES",
  "EDIT_ROLE_DETAILS",
  "EDIT_ROLE_PERMISSIONS",
  "DELETE_ROLES",
  "MANAGE_ROLE_HIERARCHY",
  "MANAGE_ROLES",
  "VIEW_WALLET",
  "VIEW_TRANSACTION_HISTORY",
  "SPEND_WALLET_FUNDS",
  "CLAIM_DAILY_REWARD",
  "PUBLISH_COMMENTS",
  "PUBLISH_REACTIONS",
  "MODERATE_COMMENTS",
  "VIEW_BETS",
  "PLACE_BETS",
  "MANAGE_BETS",
  "VIEW_BET_HISTORY",
  "VIEW_PERSONALIZATIONS",
  "APPLY_PERSONALIZATIONS",
  "BUY_PERSONALIZATIONS",
  "MANAGE_PERSONALIZATIONS",
]

const permissionMap = (enabled: RolePermission[]): Record<RolePermission, boolean> =>
  Object.fromEntries(allPermissions.map((permission) => [permission, enabled.includes(permission)])) as Record<
    RolePermission,
    boolean
  >

export const initialRoles: AccountRole[] = [
  {
    id: "role-owner",
    name: "Власник",
    color: "#D8D4C9",
    hierarchyRank: 100,
    permissions: permissionMap(allPermissions),
    locked: true,
  },
  {
    id: "role-admin",
    name: "Адміністратор",
    color: "#C08B67",
    hierarchyRank: 80,
    permissions: permissionMap(allPermissions.filter((permission) => permission !== "MANAGE_ROLE_HIERARCHY")),
  },
  {
    id: "role-moderator",
    name: "Модератор",
    color: "#A985A8",
    hierarchyRank: 50,
    permissions: permissionMap([
      "VIEW_MEMBERS",
      "VIEW_ROLES",
      "MODERATE_COMMENTS",
      "VIEW_BETS",
      "VIEW_BET_HISTORY",
      "VIEW_PERSONALIZATIONS",
    ]),
  },
  {
    id: "role-member",
    name: "Учасник",
    color: "#8A877F",
    hierarchyRank: 10,
    permissions: permissionMap([
      "VIEW_MEMBERS",
      "VIEW_WALLET",
      "VIEW_TRANSACTION_HISTORY",
      "CLAIM_DAILY_REWARD",
      "PUBLISH_COMMENTS",
      "PUBLISH_REACTIONS",
      "VIEW_BETS",
      "PLACE_BETS",
      "VIEW_BET_HISTORY",
      "VIEW_PERSONALIZATIONS",
      "APPLY_PERSONALIZATIONS",
      "BUY_PERSONALIZATIONS",
    ]),
  },
]

export const initialMembers: Member[] = [
  { id: "member-1", name: "knalis", owner: true, joinedAt: "12 лип. 2026", highestRoleId: "role-owner", roleIds: ["role-owner"] },
  { id: "member-2", name: "north", owner: false, joinedAt: "18 лип. 2026", highestRoleId: "role-admin", roleIds: ["role-admin", "role-member"] },
  { id: "member-3", name: "vex", owner: false, joinedAt: "2 серп. 2026", highestRoleId: "role-moderator", roleIds: ["role-moderator", "role-member"] },
  { id: "member-4", name: "mare", owner: false, joinedAt: "9 серп. 2026", highestRoleId: "role-member", roleIds: ["role-member"] },
]

export const feed: FeedEvent[] = [
  { id: "ev-1", kind: "ITEM_DROP", title: "Eternal Blade", detail: "knalis", occurredAt: "2026-08-17T16:48:12Z", relative: "2 хв", meta: "SWORD", rarity: "MYTHICAL" },
  { id: "ev-2", kind: "BOOSTER", title: "LUCK", detail: "vex", occurredAt: "2026-08-17T16:42:41Z", relative: "8 хв", meta: "BOOSTER" },
  { id: "ev-3", kind: "GLOBAL", title: "ECLIPSE", detail: "GLOBAL", occurredAt: "2026-08-17T16:31:03Z", relative: "19 хв" },
  { id: "ev-4", kind: "RAID", title: "RAID", detail: "2 · 4 · 7", occurredAt: "2026-08-17T16:12:20Z", relative: "38 хв", meta: "3 locations" },
  { id: "ev-5", kind: "ITEM_DROP", title: "Moon Necklace", detail: "north", occurredAt: "2026-08-17T15:58:51Z", relative: "52 хв", meta: "JEWELRY", rarity: "SECRET" },
  { id: "ev-6", kind: "GLOBAL", title: "MOON", detail: "GLOBAL", occurredAt: "2026-08-17T15:31:03Z", relative: "1 год 19 хв" },
  { id: "ev-7", kind: "ITEM_DROP", title: "Black Flame", detail: "knalis", occurredAt: "2026-08-16T19:18:00Z", relative: "16 серп.", meta: "AURA", rarity: "SECRET" },
  { id: "ev-8", kind: "ITEM_DROP", title: "Observer", detail: "knalis", occurredAt: "2026-08-14T14:06:00Z", relative: "14 серп.", meta: "PET", rarity: "MYTHICAL" },
]

export const initialSubscriptions: Subscription[] = [
  { id: "sub-1", type: "ITEM_DROP", droppedFor: "knalis" },
  { id: "sub-2", type: "ITEM_DROP", itemName: "Eternal Blade" },
  { id: "sub-3", type: "BOOSTER", boosterType: "LUCK" },
  { id: "sub-4", type: "GLOBAL", eventType: "ECLIPSE" },
  { id: "sub-5", type: "RAID", locations: [2, 4, 7] },
  { id: "sub-6", type: "DUNGEON", dungeonType: "EXPERT" },
]

export const betRounds: BetRound[] = [
  { id: "round-moon", eventType: "MOON", scheduledAt: "18 серп. · 00:00", closesAt: "17 серп. · 23:55", resolvesAt: "18 серп. · 00:05", yesOdds: 1.84, noOdds: 1.94, status: "OPEN" },
  { id: "round-explosion", eventType: "EXPLOSION", scheduledAt: "18 серп. · 01:00", closesAt: "18 серп. · 00:55", resolvesAt: "18 серп. · 01:05", yesOdds: 1.05, noOdds: 16.2, status: "OPEN", mySide: "YES", myAmount: 750 },
  { id: "round-eclipse", eventType: "ECLIPSE", scheduledAt: "18 серп. · 03:00", closesAt: "18 серп. · 02:55", resolvesAt: "18 серп. · 03:05", yesOdds: 1.91, noOdds: 1.91, status: "LOCKED" },
]

export const itemRaces: ItemRace[] = [
  { id: "race-1", itemName: "Eternal Blade", itemType: "SWORD", entryAmount: 1200, closesAt: "18 серп. · 20:00", participants: 17, joined: true, myAmount: 2200 },
  { id: "race-2", itemName: "Moon Necklace", itemType: "JEWELRY", entryAmount: 1000, closesAt: "19 серп. · 21:00", participants: 11, joined: false },
]

export const personalizations: Personalization[] = [
  { id: "p-1", type: "NOTIFICATION_PACK", target: "USER", name: "Quiet Signal", creator: "north", currency: "PRIME", price: 45, status: "PUBLISHED", owned: true, active: true, preview: "signal" },
  { id: "p-2", type: "NICK_STYLE", target: "ACCOUNT", name: "Monochrome", creator: "mnemos", currency: "COIN", price: 1800, status: "PUBLISHED", owned: false, active: false, preview: "mono" },
  { id: "p-3", type: "PROFILE_THEME", target: "ACCOUNT", name: "Graphite", creator: "mare", currency: "PRIME", price: 30, status: "PUBLISHED", owned: true, active: true, preview: "graphite" },
  { id: "p-4", type: "AVATAR_FRAME", target: "ACCOUNT", name: "Contour", creator: "vex", currency: "COIN", price: 950, status: "PUBLISHED", owned: false, active: false, preview: "contour" },
  { id: "p-5", type: "PROFILE_EFFECT", target: "ACCOUNT", name: "Noise", creator: "north", currency: "PRIME", price: 20, status: "PUBLISHED", owned: false, active: false, preview: "noise" },
]

export const creatorItems: Personalization[] = [
  { id: "creator-1", type: "PROFILE_THEME", target: "ACCOUNT", name: "Slate", creator: "knalis", currency: "PRIME", price: 25, status: "DRAFT", owned: true, active: false, preview: "slate" },
  { id: "creator-2", type: "NOTIFICATION_PACK", target: "USER", name: "Dense", creator: "knalis", currency: "PRIME", price: 35, status: "IN_REVIEW", owned: true, active: false, preview: "dense" },
]

export const reviewQueue: Personalization[] = [
  { id: "review-1", type: "NICK_STYLE", target: "ACCOUNT", name: "Cold Wire", creator: "north", currency: "PRIME", price: 28, status: "IN_REVIEW", owned: false, active: false, preview: "wire" },
  { id: "review-2", type: "PROFILE_EFFECT", target: "ACCOUNT", name: "Pulse", creator: "mare", currency: "PRIME", price: 18, status: "IN_REVIEW", owned: false, active: false, preview: "pulse" },
]

export const accountTransactions: WalletTransaction[] = [
  { id: "t1", currency: "COIN", type: "CREDIT", source: "DAILY_REWARD", amount: 420, createdAt: "сьогодні · 10:14" },
  { id: "t2", currency: "COIN", type: "DEBIT", source: "BET_PLACE", amount: 750, createdAt: "вчора · 23:51" },
  { id: "t3", currency: "COIN", type: "CREDIT", source: "ITEM_DROP_REWARD", amount: 280, createdAt: "16 серп. · 19:18" },
]

export const userTransactions: WalletTransaction[] = [
  { id: "ut1", currency: "PRIME", type: "DEBIT", source: "PERSONALIZATION_PURCHASE", amount: 45, createdAt: "16 серп. · 18:32" },
  { id: "ut2", currency: "PRIME", type: "CREDIT", source: "CREATOR_REVENUE", amount: 12, createdAt: "15 серп. · 14:20" },
]

export const profiles: PublicProfile[] = [
  { accountId: accounts[0].id, cristalixUuid: accounts[0].cristalixUuid, nickname: "knalis", likes: 128, dislikes: 7, comments: 31, skinUrl },
  { accountId: "profile-north", cristalixUuid: "9bcb0018-baa2-4dde-b360-10dc98c60418", nickname: "north", likes: 91, dislikes: 11, comments: 24, skinUrl },
  { accountId: "profile-vex", cristalixUuid: "ad0c9864-7190-4e96-bc12-6a28f9ef0e7d", nickname: "vex", likes: 61, dislikes: 5, comments: 18, skinUrl },
]

export const initialComments: ProfileComment[] = [
  { id: "comment-1", authorAccountId: "profile-north", author: "north", content: "Останній дроп виглядає сильно.", likes: 14, replies: 2, createdAt: "18 хв", liked: false },
  { id: "comment-2", authorAccountId: "profile-vex", author: "vex", content: "Міфік за один вечір — це вже статистика.", likes: 8, replies: 0, createdAt: "1 год", liked: true },
]

export const permissionGroups: { label: string; permissions: RolePermission[] }[] = [
  { label: "Аккаунт", permissions: ["VIEW_ACCOUNT_SETTINGS", "EDIT_ACCOUNT_SETTINGS", "MANAGE_ACCOUNT_INVITES"] },
  { label: "Учасники", permissions: ["VIEW_MEMBERS", "INVITE_MEMBERS", "REMOVE_MEMBERS", "ASSIGN_MEMBER_ROLES", "KICK_MEMBERS"] },
  { label: "Ролі", permissions: ["VIEW_ROLES", "CREATE_ROLES", "EDIT_ROLE_DETAILS", "EDIT_ROLE_PERMISSIONS", "DELETE_ROLES", "MANAGE_ROLE_HIERARCHY", "MANAGE_ROLES"] },
  { label: "Гаманець", permissions: ["VIEW_WALLET", "VIEW_TRANSACTION_HISTORY", "SPEND_WALLET_FUNDS"] },
  { label: "Активність", permissions: ["CLAIM_DAILY_REWARD", "PUBLISH_COMMENTS", "PUBLISH_REACTIONS", "MODERATE_COMMENTS"] },
  { label: "Ставки", permissions: ["VIEW_BETS", "PLACE_BETS", "MANAGE_BETS", "VIEW_BET_HISTORY"] },
  { label: "Персоналізація", permissions: ["VIEW_PERSONALIZATIONS", "APPLY_PERSONALIZATIONS", "BUY_PERSONALIZATIONS", "MANAGE_PERSONALIZATIONS"] },
]
