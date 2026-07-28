import type {Account, AvailableAccount, User} from "@/shared/types/domain"
import {api} from "@/shared/api/httpClient"

const role = {
    id: "owner",
    name: "Владелец",
    color: "#d5a957",
    permissions: ["ACCOUNT_EDIT", "MEMBER_MANAGE", "COMMENT_MODERATE", "PERSONALIZATION_EDIT"]
}

export async function initializeUser(): Promise<User> {
    return api<User>("/api/v1/users/me", {method: "PUT"})
}

export async function updateLanguage(localizationLanguage: User["localizationLanguage"]): Promise<User> {
    return api<User>("/api/v1/users/me/localization", {
        method: "PATCH",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({localizationLanguage})
    })
}

export async function getAvailableAccounts(): Promise<AvailableAccount[]> {
    return api<AvailableAccount[]>("/api/v1/me/accounts")
}

export async function getActiveAccount(): Promise<Account | null> {
    const response = await api<Omit<Account, "role" | "online" | "streak" | "coins" | "primes" | "likes" | "comments" | "members" | "about" | "personalization"> | null>("/api/v1/me/accounts/active")
    return response ? enrichAccount(response) : null
}

export async function activateAccount(accountId: string): Promise<Account> {
    const response = await api<Omit<Account, "role" | "online" | "streak" | "coins" | "primes" | "likes" | "comments" | "members" | "about" | "personalization">>(`/api/v1/me/accounts/active/${accountId}`, {method: "PUT"})
    return enrichAccount(response)
}

export async function createAccount(cristalixNickname: string): Promise<Account> {
    const response = await api<Omit<Account, "role" | "online" | "streak" | "coins" | "primes" | "likes" | "comments" | "members" | "about" | "personalization">>("/api/v1/accounts", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({cristalixNickname})
    })
    return enrichAccount(response)
}

export async function leaveActiveAccount(): Promise<AvailableAccount[]> {
    return api<AvailableAccount[]>("/api/v1/me/accounts/active/leave", {method: "POST"})
}

function enrichAccount(account: Omit<Account, "role" | "online" | "streak" | "coins" | "primes" | "likes" | "comments" | "members" | "about" | "personalization">): Account {
    return {
        ...account,
        role,
        online: true,
        streak: 7,
        coins: 12480,
        primes: 240,
        likes: 128,
        comments: 27,
        members: 4,
        about: "Люблю участвовать в событиях ✨",
        personalization: {prefix: "[-M-]", suffix: "🐺", nicknameColor: role.color, badge: "♔"}
    }
}
