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
    hierarchyRank: 10,
    permissions: permissionMap(allPermissions),
    locked: true,
  },
  {
    id: "role-admin",
    name: "Адміністратор",
    color: "#C08B67",
    hierarchyRank: 20,
    permissions: permissionMap(allPermissions.filter((permission) => permission !== "MANAGE_ROLE_HIERARCHY")),
  },
  {
    id: "role-moderator",
    name: "Модератор",
    color: "#A985A8",
    hierarchyRank: 30,
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
    hierarchyRank: 40,
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
  { id: "member-3", name: "Asteria", owner: false, joinedAt: "22 лип. 2026", highestRoleId: "role-moderator", roleIds: ["role-moderator", "role-member"] },
  { id: "member-4", name: "Solaire", owner: false, joinedAt: "3 серп. 2026", highestRoleId: "role-member", roleIds: ["role-member"] },
]

export const feed: FeedEvent[] = [
  {
    id: "ev-1",
    kind: "ITEM_DROP",
    title: "Elucidator",
    detail: "knalis",
    occurredAt: "2026-08-17T19:16:00+03:00",
    rarity: "MYTHICAL",
    meta: "Міфічний предмет",
    itemType: "SWORD",
  },
  {
    id: "ev-2",
    kind: "BOOSTER",
    title: "Бустер урону",
    detail: "Активував north",
    occurredAt: "2026-08-17T19:12:00+03:00",
    meta: "25 хвилин",
    boosterType: "DAMAGE",
  },
  {
    id: "ev-3",
    kind: "GLOBAL",
    title: "Затемнення",
    detail: "Глобальна подія",
    occurredAt: "2026-08-17T19:00:00+03:00",
    meta: "Активна",
    globalType: "ECLIPSE",
  },
  {
    id: "ev-4",
    kind: "RAID",
    title: "Рейд",
    detail: "Floor 74",
    occurredAt: "2026-08-17T18:55:00+03:00",
    meta: "2 локації",
  },
  {
    id: "ev-5",
    kind: "ITEM_DROP",
    title: "Ring of Angel",
    detail: "knalis",
    occurredAt: "2026-08-17T18:42:00+03:00",
    rarity: "SECRET",
    meta: "Секретний предмет",
    itemType: "JEWELRY",
  },
  {
    id: "ev-6",
    kind: "GLOBAL",
    title: "Місяць",
    detail: "Глобальна подія",
    occurredAt: "2026-08-17T18:30:00+03:00",
    meta: "Завершена",
    globalType: "MOON",
  },
  {
    id: "ev-7",
    kind: "ITEM_DROP",
    title: "Crimson Aura",
    detail: "knalis",
    occurredAt: "2026-08-17T18:14:00+03:00",
    rarity: "MYTHICAL",
    meta: "Міфічний предмет",
    itemType: "AURA",
  },
  {
    id: "ev-8",
    kind: "ITEM_DROP",
    title: "Moon Rabbit",
    detail: "north",
    occurredAt: "2026-08-17T18:05:00+03:00",
    rarity: "SECRET",
    meta: "Секретний предмет",
    itemType: "PET",
  },
]

export const accountTransactions: WalletTransaction[] = [
  { id: "tx-a-1", type: "CREDIT", source: "DAILY_REWARD", amount: 420, createdAt: "сьогодні, 18:32" },
  { id: "tx-a-2", type: "DEBIT", source: "BET_PLACE", amount: 900, createdAt: "сьогодні, 17:10" },
  { id: "tx-a-3", type: "CREDIT", source: "ITEM_DROP_REWARD", amount: 1_800, createdAt: "сьогодні, 15:48" },
  { id: "tx-a-4", type: "DEBIT", source: "PERSONALIZATION_PURCHASE", amount: 1_200, createdAt: "вчора, 22:04" },
  { id: "tx-a-5", type: "CREDIT", source: "BET_WIN", amount: 2_350, createdAt: "вчора, 19:20" },
]

export const userTransactions: WalletTransaction[] = [
  { id: "tx-u-1", type: "CREDIT", source: "PRIME_PURCHASE", amount: 100, createdAt: "14 серп., 21:15" },
  { id: "tx-u-2", type: "DEBIT", source: "PERSONALIZATION_PURCHASE", amount: 24, createdAt: "13 серп., 18:07" },
  { id: "tx-u-3", type: "CREDIT", source: "DAILY_REWARD", amount: 3, createdAt: "12 серп., 00:03" },
  { id: "tx-u-4", type: "DEBIT", source: "PERSONALIZATION_PURCHASE", amount: 12, createdAt: "10 серп., 16:44" },
]

export const subscriptions: Subscription[] = [
  { id: "sub-1", type: "ITEM_DROP", label: "Elucidator", detail: "будь-який гравець", enabled: true },
  { id: "sub-2", type: "ITEM_DROP", label: "Дропи knalis", detail: "усі предмети", enabled: true },
  { id: "sub-3", type: "DUNGEON", label: "Expert Dungeon", detail: "за 5 хвилин", enabled: true },
  { id: "sub-4", type: "GLOBAL", label: "Затемнення", detail: "при появі", enabled: false },
]

export const personalizationCatalog: Personalization[] = [
  {
    id: "p-1",
    target: "ACCOUNT",
    type: "PROFILE_FRAME",
    name: "Verdant Circuit",
    creator: "north",
    price: 180,
    currency: "COIN",
    owned: true,
    equipped: true,
    visibility: "PUBLIC",
  },
  {
    id: "p-2",
    target: "USER",
    type: "NOTIFICATION_PACK",
    name: "Obsidian Pulse",
    creator: "Asteria",
    price: 18,
    currency: "PRIME",
    owned: true,
    equipped: true,
    visibility: "PUBLIC",
  },
  {
    id: "p-3",
    target: "ACCOUNT",
    type: "PROFILE_BADGE",
    name: "Raid Vanguard",
    creator: "Mnemos",
    price: 240,
    currency: "COIN",
    owned: false,
    equipped: false,
    visibility: "PUBLIC",
  },
  {
    id: "p-4",
    target: "USER",
    type: "NOTIFICATION_PACK",
    name: "Celestial Whisper",
    creator: "north",
    price: 28,
    currency: "PRIME",
    owned: false,
    equipped: false,
    visibility: "PUBLIC",
  },
]

export const publicProfiles: PublicProfile[] = [
  {
    accountId: "profile-north",
    cristalixUuid: "46d7a80e-74f5-11eb-acca-1cb72caa35fd",
    nickname: "north",
    skinUrl,
    likes: 184,
    dislikes: 9,
    comments: 24,
    appearance: {
      frame: "Verdant Circuit",
      badge: "Raid Vanguard",
    },
  },
  {
    accountId: "profile-Asteria",
    cristalixUuid: "46d7a80e-74f5-11eb-acca-1cb72caa35fd",
    nickname: "Asteria",
    skinUrl,
    likes: 92,
    dislikes: 4,
    comments: 11,
    appearance: {
      frame: "Verdant Circuit",
      badge: "Raid Vanguard",
    },
  },
]

export const profileComments: ProfileComment[] = [
  {
    id: "comment-1",
    accountId: "profile-north",
    authorAccountId: "profile-Asteria",
    authorName: "Asteria",
    content: "Стабільний гравець, без проблем у рейдах.",
    likes: 14,
    replies: 2,
    createdAt: "сьогодні, 16:41",
  },
  {
    id: "comment-2",
    accountId: "profile-north",
    authorAccountId: "46d7a80e-74f5-11eb-acca-1cb72caa35fd",
    authorName: "knalis",
    content: "Підтверджую, по дропам усе чисто.",
    likes: 8,
    replies: 0,
    createdAt: "сьогодні, 17:02",
  },
]

export const betRounds: BetRound[] = [
  {
    id: "bet-moon-next",
    eventType: "MOON",
    startsAt: "2026-08-18T00:00:00+03:00",
    closesAt: "2026-08-17T23:55:00+03:00",
    settlesAt: "2026-08-18T02:05:00+03:00",
    yesPool: 6_840,
    noPool: 5_160,
    status: "OPEN",
  },
  {
    id: "bet-eclipse-next",
    eventType: "ECLIPSE",
    startsAt: "2026-08-18T03:00:00+03:00",
    closesAt: "2026-08-18T02:55:00+03:00",
    settlesAt: "2026-08-18T09:05:00+03:00",
    yesPool: 4_210,
    noPool: 4_050,
    status: "OPEN",
  },
]

export const itemRaces: ItemRace[] = [
  {
    id: "race-1",
    title: "Перший дроп Elucidator",
    itemName: "Elucidator",
    closesAt: "2026-08-18T22:00:00+03:00",
    pool: 18_450,
    status: "OPEN",
  },
]

export const permissionGroups = [
  {
    title: "Аккаунт",
    permissions: [
      "VIEW_ACCOUNT_SETTINGS",
      "EDIT_ACCOUNT_SETTINGS",
      "MANAGE_ACCOUNT_INVITES",
      "VIEW_MEMBERS",
      "INVITE_MEMBERS",
      "REMOVE_MEMBERS",
      "ASSIGN_MEMBER_ROLES",
      "KICK_MEMBERS",
    ] as RolePermission[],
  },
  {
    title: "Ролі",
    permissions: [
      "VIEW_ROLES",
      "CREATE_ROLES",
      "EDIT_ROLE_DETAILS",
      "EDIT_ROLE_PERMISSIONS",
      "DELETE_ROLES",
      "MANAGE_ROLE_HIERARCHY",
      "MANAGE_ROLES",
    ] as RolePermission[],
  },
  {
    title: "Гаманець",
    permissions: [
      "VIEW_WALLET",
      "VIEW_TRANSACTION_HISTORY",
      "SPEND_WALLET_FUNDS",
      "CLAIM_DAILY_REWARD",
    ] as RolePermission[],
  },
  {
    title: "Соціальне",
    permissions: ["PUBLISH_COMMENTS", "PUBLISH_REACTIONS", "MODERATE_COMMENTS"] as RolePermission[],
  },
  {
    title: "Ставки",
    permissions: ["VIEW_BETS", "PLACE_BETS", "MANAGE_BETS", "VIEW_BET_HISTORY"] as RolePermission[],
  },
  {
    title: "Персоналізація",
    permissions: [
      "VIEW_PERSONALIZATIONS",
      "APPLY_PERSONALIZATIONS",
      "BUY_PERSONALIZATIONS",
      "MANAGE_PERSONALIZATIONS",
    ] as RolePermission[],
  },
]
