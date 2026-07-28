import type {Account, Bet, Comment, ItemEvent, NewsItem, Notification, Proposal} from "@/shared/types/domain"

const delay = (ms = 160) => new Promise(resolve => setTimeout(resolve, ms))

const accounts: Account[] = [
    {
        id: "acc-mirabel",
        ownerUserId: "u2",
        cristalixUuid: "46d7a80e-74f5-11eb-acca-1cb72caa35fd",
        cristalixNickname: "Mirabel",
        role: {id: "member", name: "Участник", color: "#d9dde4", permissions: []},
        online: true,
        streak: 4,
        coins: 8900,
        primes: 120,
        likes: 64,
        comments: 8,
        members: 2,
        about: "Люблю участвовать в событиях ✨",
        personalization: {prefix: "[-M-]", suffix: "🐺", nicknameColor: "#d9dde4", badge: ""}
    },
    {
        id: "acc-vexor",
        ownerUserId: "u3",
        cristalixUuid: "46d7a80e-74f5-11eb-acca-1cb72caa35fd",
        cristalixNickname: "Vexor",
        role: {id: "member", name: "Участник", color: "#b8c7ba", permissions: []},
        online: true,
        streak: 11,
        coins: 17200,
        primes: 80,
        likes: 91,
        comments: 12,
        members: 5,
        about: "Участник сообщества",
        personalization: {prefix: "{V}", suffix: "", nicknameColor: "#b8c7ba", badge: ""}
    },
    {
        id: "acc-lightsoul",
        ownerUserId: "u4",
        cristalixUuid: "46d7a80e-74f5-11eb-acca-1cb72caa35fd",
        cristalixNickname: "LightSoul",
        role: {id: "owner", name: "Владелец", color: "#d5a957", permissions: ["ACCOUNT_EDIT"]},
        online: false,
        streak: 2,
        coins: 24100,
        primes: 430,
        likes: 152,
        comments: 31,
        members: 7,
        about: "Коллекционер",
        personalization: {prefix: "<ICE>", suffix: "❄", nicknameColor: "#d5a957", badge: "♔"}
    },
    {
        id: "acc-mnemos",
        ownerUserId: "u5",
        cristalixUuid: "46d7a80e-74f5-11eb-acca-1cb72caa35fd",
        cristalixNickname: "MnemosUser",
        role: {id: "owner", name: "Владелец", color: "#b58b91", permissions: ["ACCOUNT_EDIT"]},
        online: false,
        streak: 1,
        coins: 3300,
        primes: 25,
        likes: 33,
        comments: 4,
        members: 1,
        about: "Новый аккаунт",
        personalization: {prefix: "", suffix: "", nicknameColor: "#b58b91", badge: "♔"}
    }
]

const baseItems = ["Меч рассвета", "Крылья пустоты", "Сфера хаоса", "Лук звездопада", "Тотем бессмертия", "Посох тьмы", "Ледяной осколок", "Печать ветра", "Руна созвездия", "Клинок бездны"]

export async function getItems(offset = 0, limit = 8, mineOnly = false): Promise<ItemEvent[]> {
    await delay()
    return Array.from({length: limit}, (_, index) => {
        const n = offset + index
        return {
            id: `item-${n}`,
            name: baseItems[n % baseItems.length],
            type: "item",
            accountName: mineOnly ? "Knalis" : ["Knalis", "Mirabel", "Vexor", "LightSoul", "MnemosUser"][n % 5],
            happenedAt: new Date(Date.now() - n * 22 * 60_000).toISOString(),
            description: n % 2 ? "Выбил" : "Получил"
        }
    })
}

export async function searchAccounts(query = ""): Promise<Account[]> {
    await delay()
    return accounts.filter(account => account.cristalixNickname.toLowerCase().includes(query.toLowerCase()))
}

export async function getAccount(id: string): Promise<Account | undefined> {
    await delay()
    return accounts.find(account => account.id === id)
}

export async function getComments(): Promise<Comment[]> {
    await delay()
    return [
        {id: "1", author: "LiraNix", text: "Отличный аккаунт!", likes: 12, createdAt: "18 мар., 20:45"},
        {id: "2", author: "Vexor", text: "Поздравляю с победами!", likes: 7, createdAt: "18 мар., 19:12"},
        {id: "3", author: "MnemosUser", text: "Красиво оформлено 🔥", likes: 4, createdAt: "18 мар., 18:33"},
        {id: "4", author: "SiriusM", text: "Желаю удачи в следующих рейдах!", likes: 3, createdAt: "18 мар., 18:00"},
        {id: "5", author: "Mirabel", text: "Топовый аккаунт", likes: 6, createdAt: "18 мар., 17:41"}
    ]
}

export async function getBets(): Promise<Bet[]> {
    await delay()
    return [
        {
            id: "b1",
            title: "Великий метеорит",
            mode: "event",
            status: "ACTIVE",
            chance: 35,
            pool: 12480,
            participants: 41,
            endsAt: "2ч 15м"
        },
        {
            id: "b2",
            title: "Ледяной шторм",
            mode: "event",
            status: "ACTIVE",
            chance: 60,
            pool: 8210,
            participants: 29,
            endsAt: "5ч 10м"
        },
        {
            id: "b3",
            title: "Меч рассвета",
            mode: "race",
            status: "ACTIVE",
            pool: 24960,
            participants: 23,
            endsAt: "3ч 22м",
            myBet: 100,
            multiplier: 2.85
        },
        {
            id: "b4",
            title: "Тотем бессмертия",
            mode: "race",
            status: "PENDING",
            pool: 18450,
            participants: 17,
            endsAt: "5ч 41м"
        },
        {
            id: "b5",
            title: "Ночной замес",
            mode: "event",
            status: "WON",
            pool: 9450,
            participants: 32,
            endsAt: "Завершено"
        }
    ]
}

export async function getNotifications(): Promise<Notification[]> {
    await delay()
    return [{id: "n1", title: "Новая награда", body: "Ежедневная награда доступна", read: false}, {
        id: "n2",
        title: "Гонка началась",
        body: "Меч рассвета уже разыгрывается",
        read: true
    }]
}

export async function getNews(): Promise<NewsItem[]> {
    await delay()
    return [{
        id: "news1",
        title: "Весенний сезон",
        summary: "Новые события и награды уже доступны.",
        createdAt: "Сегодня"
    }, {
        id: "news2",
        title: "Обновление персонализации",
        summary: "Добавлены новые префиксы и цвета ника.",
        createdAt: "Вчера"
    }]
}

export async function getProposals(): Promise<Proposal[]> {
    await delay()
    return [{
        id: "p1",
        title: "Больше ежедневных заданий",
        description: "Добавить цепочки заданий на неделю.",
        author: "Mirabel",
        votes: 128
    }, {
        id: "p2",
        title: "Новая тема профиля",
        description: "Добавить ледяной набор оформления.",
        author: "Vexor",
        votes: 74
    }]
}
