import type {
    AccountProfile,
    Comment,
    EventCard,
    ItemActivity,
    Notification,
    Page,
    Role,
    Transaction
} from "@/shared/types/domain"

const roles: Role[] = [{
    id: "owner",
    name: "Владелец",
    color: "#e7bd66",
    permissions: ["account.manage", "members.manage", "comments.moderate", "events.manage"]
}, {
    id: "admin",
    name: "Администратор",
    color: "#8bc7ff",
    permissions: ["comments.moderate", "events.manage"]
}, {id: "member", name: "Участник", color: "#d4d8df", permissions: ["account.view", "comments.write"]}]
const profiles: AccountProfile[] = [{
    id: "f522f836-3b67-48cb-8c6a-d4b8498af663",
    ownerUserId: "0bc94395-990b-48d2-bde2-19490ee62c89",
    cristalixUuid: "46d7a80e-74f5-11eb-acca-1cb72caa35fd",
    cristalixNickname: "Knalis",
    online: true,
    role: roles[0],
    likes: 128,
    members: 4,
    comments: 27,
    about: "Люблю участвовать в событиях ✦",
    balance: {coins: 12480, primes: 240},
    streak: 7,
    dailyRewardAvailable: true,
    personalization: {prefix: "[-M-]", emoji: "🐺", nicknameColor: "#f3f5f8", plaque: "ICE"}
}, {
    id: "fd6e6efe-21b5-41b3-92d3-2a1f2d2810fc",
    ownerUserId: "34e68766-3fb9-45cc-9437-c07964f26f20",
    cristalixUuid: "d293c379-57f1-4561-b752-c38c1a6481e3",
    cristalixNickname: "Mirabel",
    online: true,
    role: roles[2],
    likes: 64,
    members: 2,
    comments: 8,
    about: "Коллекционирую редкие предметы",
    balance: {coins: 8450, primes: 95},
    streak: 4,
    dailyRewardAvailable: false,
    personalization: {prefix: "{A}", emoji: "🦊", nicknameColor: "#f2c8a2"}
}, {
    id: "ba9500d2-5d75-4050-8d86-36dc58682dbf",
    ownerUserId: "9d269e4a-b3ef-422d-aa71-f507598b39a7",
    cristalixUuid: "abde0ca1-c8bc-4c09-8ab9-bc1d22343af0",
    cristalixNickname: "Vexor",
    online: true,
    role: roles[2],
    likes: 93,
    members: 3,
    comments: 15,
    about: "Ставки, гонки и торговля",
    balance: {coins: 9120, primes: 110},
    streak: 9,
    dailyRewardAvailable: true,
    personalization: {prefix: "<ICE>", emoji: "⚔️", nicknameColor: "#b7d7ff"}
}, {
    id: "768aac42-ea58-4bc4-9dd5-642b83ac192f",
    ownerUserId: "c6f6174d-24d3-462d-b0a1-0ae5b1f791ff",
    cristalixUuid: "6f0c96e3-4964-4622-844f-c783f9d73013",
    cristalixNickname: "LightSoul",
    online: false,
    role: roles[0],
    likes: 154,
    members: 5,
    comments: 31,
    about: "Создаю события для сообщества",
    balance: {coins: 18800, primes: 410},
    streak: 12,
    dailyRewardAvailable: false,
    personalization: {prefix: "{DRAGON}", emoji: "✨", nicknameColor: "#ffe0a1"}
}]
const itemNames = ["Меч рассвета", "Крылья пустоты", "Сфера хаоса", "Лук звездопада", "Тотем бессмертия", "Посох тьмы", "Кристалл бури", "Клинок памяти", "Печать феникса", "Осколок бездны", "Сердце кометы", "Руна времени"]
const kinds = ["weapon", "wing", "orb", "weapon", "totem", "artifact"] as const
const activities: ItemActivity[] = Array.from({length: 48}, (_, i) => ({
    id: `item-${i}`,
    title: itemNames[i % itemNames.length],
    kind: kinds[i % kinds.length],
    winner: profiles[i % profiles.length].cristalixNickname,
    accountId: profiles[i % profiles.length].id,
    createdAt: new Date(Date.now() - i * 1000 * 60 * 37).toISOString()
}))
const events: EventCard[] = [{
    id: "e1",
    title: "Великий метеорит",
    type: "EVENT",
    status: "ACTIVE",
    chance: 35,
    endsAt: new Date(Date.now() + 8550000).toISOString(),
    pool: 12480,
    participants: 42,
    myBet: 250,
    multiplier: 2.15
}, {
    id: "e2",
    title: "Ледяной шторм",
    type: "EVENT",
    status: "ACTIVE",
    chance: 60,
    endsAt: new Date(Date.now() + 18000000).toISOString(),
    pool: 8210,
    participants: 28
}, {
    id: "r1",
    title: "Меч рассвета",
    type: "RACE",
    status: "ACTIVE",
    endsAt: new Date(Date.now() + 13000000).toISOString(),
    pool: 24960,
    participants: 23,
    myBet: 100,
    multiplier: 2.85
}, {
    id: "r2",
    title: "Тотем бессмертия",
    type: "RACE",
    status: "PENDING",
    endsAt: new Date(Date.now() + 20000000).toISOString(),
    pool: 18490,
    participants: 17
}, {
    id: "e3",
    title: "Ночной замес",
    type: "EVENT",
    status: "WON",
    endsAt: new Date(Date.now() - 86400000).toISOString(),
    pool: 9450,
    participants: 36
}]
const comments: Comment[] = [{
    id: "c1",
    author: "LiraNix",
    text: "Отличный аккаунт!",
    likes: 12,
    createdAt: new Date(Date.now() - 7200000).toISOString(),
    replies: [{
        id: "c1r1",
        author: "Knalis",
        text: "Спасибо!",
        likes: 2,
        createdAt: new Date(Date.now() - 6800000).toISOString()
    }]
}, {
    id: "c2",
    author: "Vexor",
    text: "Поздравляю с победами!",
    likes: 7,
    createdAt: new Date(Date.now() - 10800000).toISOString()
}, {
    id: "c3",
    author: "MnemosUser",
    text: "Красиво оформлено 🔥",
    likes: 4,
    createdAt: new Date(Date.now() - 14400000).toISOString()
}, {
    id: "c4",
    author: "SiriusM",
    text: "Желаю удачи в следующих рейдах!",
    likes: 3,
    createdAt: new Date(Date.now() - 18000000).toISOString()
}]
const transactions: Transaction[] = Array.from({length: 24}, (_, i) => ({
    id: `tx-${i}`,
    label: i % 2 ? "Ставка на событие" : "Награда за победу",
    amount: i % 2 ? -(50 + i * 5) : 250 + i * 10,
    currency: i % 3 ? "COINS" : "PRIMES",
    createdAt: new Date(Date.now() - i * 86400000).toISOString()
}))
const notifications: Notification[] = [{
    id: "n1",
    title: "Новая награда",
    body: "Ежедневная награда доступна",
    read: false,
    createdAt: new Date().toISOString()
}, {
    id: "n2",
    title: "Гонка завершена",
    body: "Меч рассвета разыгран",
    read: false,
    createdAt: new Date(Date.now() - 3600000).toISOString()
}]
const wait = (ms = 180) => new Promise(r => setTimeout(r, ms))
export const contentRepository = {
    getRoles: async () => {
        await wait();
        return roles
    }, getProfile: async (id?: string) => {
        await wait();
        return profiles.find(p => p.id === id) || profiles[0]
    }, searchProfiles: async (q: string) => {
        await wait();
        return profiles.filter(p => p.cristalixNickname.toLowerCase().includes(q.toLowerCase()))
    }, getItems: async (page = 0, size = 10, accountId?: string): Promise<Page<ItemActivity>> => {
        await wait();
        const source = accountId ? activities.filter(x => x.accountId === accountId) : activities;
        const start = page * size;
        return {
            items: source.slice(start, start + size),
            page,
            size,
            total: source.length,
            hasMore: start + size < source.length
        }
    }, getEvents: async (type?: "EVENT" | "RACE") => {
        await wait();
        return type ? events.filter(e => e.type === type) : events
    }, getEvent: async (id: string) => {
        await wait();
        return events.find(e => e.id === id) || events[0]
    }, getComments: async () => {
        await wait();
        return comments
    }, getTransactions: async () => {
        await wait();
        return transactions
    }, getNotifications: async () => {
        await wait();
        return notifications
    }, joinByInvite: async (code: string) => {
        await wait();
        console.log("Join by invite", code)
    }, deleteAccount: async (id: string) => {
        await wait();
        console.log("Delete account", id)
    }
}