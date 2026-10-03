import { createContext, type ReactNode, useCallback, useContext, useEffect, useMemo } from "react"
import { useApp } from "@/app/providers/AppProvider"
import type { DailyRewardStateResponse } from "@/shared/api/backendContracts"
import type { EventFeedQuery } from "@/shared/api/eventApi"
import { handleError } from "@/shared/errors/errorHandler"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import { useAccountData } from "../account/data/useAccountData"
import { useEventFeedData } from "../events/feed/useEventFeedData"
import { useSubscriptionData } from "../events/subscriptions/useSubscriptionData"
import type {
  Account,
  AccountRole,
  FeedEvent,
  LocalizationLanguage,
  Member,
  RolePermission,
  Subscription,
  SystemRole,
} from "../model"
import { toAccount } from "./dataMappers"

type MnemosUser = {
  id: string
  username: string
  language: LocalizationLanguage
  prime: number
  systemRoles: SystemRole[]
}

type MnemosDataState = {
  user: MnemosUser
  accounts: Account[]
  activeAccount: Account | null
  members: Member[]
  roles: AccountRole[]
  feed: FeedEvent[]
  eventCursor: string | null
  eventsHasMore: boolean
  subscriptions: Subscription[]
  pingNotificationsEnabled: boolean
  loadingSubscriptions: boolean
  subscriptionsError: boolean
  dailyReward: DailyRewardStateResponse | null
  loadingAccount: boolean
  accountError: boolean
  loadingEvents: boolean
  eventsError: boolean
  hasPermission(permission: RolePermission): boolean
  refreshAccount(): Promise<void>
  refreshEvents(query?: EventFeedQuery): Promise<void>
  loadMoreEvents(query?: EventFeedQuery): Promise<void>
  refreshSubscriptions(): Promise<boolean>
  mutateSubscriptions(mutation: () => Promise<void>): Promise<boolean>
  selectAccount(account: Account): Promise<boolean>
  createAccount(nickname: string): Promise<boolean>
  joinAccount(inviteCode: string): Promise<boolean>
  setLanguage(language: LocalizationLanguage): Promise<void>
  claimReward(): Promise<{ coin: number; prime: number } | null>
  restoreReward(): Promise<void>
  removeSubscription(subscriptionId: string): Promise<boolean>
}

const MnemosDataContext = createContext<MnemosDataState | null>(null)

export function MnemosDataProvider({ children }: { children: ReactNode }) {
  const app = useApp()
  const context = app.context
  const activeAccountId = context?.activeAccount?.id
  const currentMemberId = context?.activeAccount?.currentMemberId
  const permissionSet = useMemo(
    () => new Set<RolePermission>(context?.effectivePermissions ?? []),
    [context?.effectivePermissions],
  )
  const hasPermission = useCallback(
    (permission: RolePermission) => permissionSet.has(permission),
    [permissionSet],
  )

  const {
    members,
    roles,
    dailyReward,
    accountCoin,
    prime,
    loadingAccount,
    accountError,
    refreshAccount,
    claimReward,
    restoreReward,
  } = useAccountData(activeAccountId, currentMemberId)

  useEffect(() => {
    return realtimeEvents.subscribe((event) => {
      if (
        event.type !== "account.activated" &&
        event.type !== "account.deactivated" &&
        event.type !== "account.deleted" &&
        event.type !== "account.owner.transferred" &&
        event.type !== "account.member.joined" &&
        event.type !== "account.member.left" &&
        event.type !== "account.member.removed"
      ) {
        return
      }

      if (!activeAccountId || event.payload.accountId !== activeAccountId) {
        return
      }

      void refreshAccount().catch(handleError)
    })
  }, [activeAccountId, refreshAccount])

  const {
    feed,
    eventCursor,
    eventsHasMore,
    loadingEvents,
    eventsError,
    refreshEvents,
    loadMoreEvents,
  } = useEventFeedData()

  const {
    subscriptions,
    pingNotificationsEnabled,
    loadingSubscriptions,
    subscriptionsError,
    refreshSubscriptions,
    mutateSubscriptions,
    removeSubscription,
  } = useSubscriptionData()

  const accounts = useMemo(
    () =>
      context?.availableAccounts.map((account) =>
        toAccount(account, account.id === activeAccountId ? accountCoin : 0),
      ) ?? [],
    [accountCoin, activeAccountId, context?.availableAccounts],
  )

  const activeAccount = useMemo(
    () => accounts.find((account) => account.id === activeAccountId) ?? null,
    [accounts, activeAccountId],
  )

  const user = useMemo<MnemosUser>(
    () => ({
      id: context?.user.id ?? "",
      username: context?.user.username ?? "",
      language: context?.user.localizationLanguage ?? "UK",
      prime,
      systemRoles: context?.systemRoles ?? [],
    }),
    [context?.systemRoles, context?.user, prime],
  )

  const selectAccount = useCallback(
    async (account: Account) => {
      try {
        await app.selectAccount(account.id)
        return true
      } catch (error) {
        handleError(error)
        return false
      }
    },
    [app],
  )

  const createAccount = useCallback(
    async (nickname: string) => {
      try {
        await app.registerAccount(nickname)
        return true
      } catch (error) {
        handleError(error)
        return false
      }
    },
    [app],
  )

  const joinAccount = useCallback(
    async (inviteCode: string) => {
      try {
        await app.joinByCode(inviteCode)
        return true
      } catch (error) {
        handleError(error)
        return false
      }
    },
    [app],
  )

  const setLanguage = useCallback(
    async (language: LocalizationLanguage) => {
      try {
        await app.setLanguage(language)
      } catch (error) {
        handleError(error)
      }
    },
    [app],
  )

  const value = useMemo<MnemosDataState>(
    () => ({
      user,
      accounts,
      activeAccount,
      members,
      roles,
      feed,
      eventCursor,
      eventsHasMore,
      subscriptions,
      pingNotificationsEnabled,
      loadingSubscriptions,
      subscriptionsError,
      dailyReward,
      loadingAccount,
      accountError,
      loadingEvents,
      eventsError,
      hasPermission,
      refreshAccount,
      refreshEvents,
      loadMoreEvents,
      refreshSubscriptions,
      mutateSubscriptions,
      selectAccount,
      createAccount,
      joinAccount,
      setLanguage,
      claimReward,
      restoreReward,
      removeSubscription,
    }),
    [
      user,
      accounts,
      activeAccount,
      members,
      roles,
      feed,
      eventCursor,
      eventsHasMore,
      subscriptions,
      pingNotificationsEnabled,
      loadingSubscriptions,
      subscriptionsError,
      dailyReward,
      loadingAccount,
      accountError,
      loadingEvents,
      eventsError,
      hasPermission,
      refreshAccount,
      refreshEvents,
      loadMoreEvents,
      refreshSubscriptions,
      mutateSubscriptions,
      selectAccount,
      createAccount,
      joinAccount,
      setLanguage,
      claimReward,
      restoreReward,
      removeSubscription,
    ],
  )

  return <MnemosDataContext.Provider value={value}>{children}</MnemosDataContext.Provider>
}

export function useMnemosData() {
  const value = useContext(MnemosDataContext)

  if (!value) {
    throw new Error("MnemosDataProvider is missing")
  }

  return value
}
