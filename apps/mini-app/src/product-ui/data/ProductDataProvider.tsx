import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useMemo,
} from "react"
import { useApp } from "@/app/providers/AppProvider"
import type { DailyRewardStateResponse } from "@/shared/api/backendContracts"
import type { EventFeedQuery } from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import type {
  Account,
  AccountRole,
  FeedEvent,
  LocalizationLanguage,
  Member,
  Subscription,
  SystemRole,
} from "../model"
import { toProductAccount } from "./productMappers"
import { useAccountData } from "./useAccountData"
import { useEventFeedData } from "./useEventFeedData"
import { useSubscriptionData } from "./useSubscriptionData"

type ProductUser = {
  id: string
  username: string
  language: LocalizationLanguage
  prime: number
  systemRoles: SystemRole[]
}

type ProductDataState = {
  user: ProductUser
  accounts: Account[]
  activeAccount: Account | null
  members: Member[]
  roles: AccountRole[]
  feed: FeedEvent[]
  eventCursor: string | null
  eventsHasMore: boolean
  subscriptions: Subscription[]
  dailyReward: DailyRewardStateResponse | null
  loadingAccount: boolean
  loadingEvents: boolean
  refreshAccount(): Promise<void>
  refreshEvents(query?: EventFeedQuery): Promise<void>
  loadMoreEvents(query?: EventFeedQuery): Promise<void>
  refreshSubscriptions(): Promise<void>
  selectAccount(account: Account): Promise<void>
  createAccount(nickname: string): Promise<void>
  joinAccount(inviteCode: string): Promise<void>
  setLanguage(language: LocalizationLanguage): Promise<void>
  claimReward(): Promise<{ coin: number; prime: number }>
  restoreReward(): Promise<void>
  removeSubscription(subscriptionId: string): Promise<void>
}

const ProductDataContext = createContext<ProductDataState | null>(null)

export function ProductDataProvider({ children }: { children: ReactNode }) {
  const app = useApp()
  const context = app.context
  const activeAccountId = context?.activeAccount?.id

  const {
    members,
    roles,
    dailyReward,
    accountCoin,
    prime,
    loadingAccount,
    refreshAccount,
    claimReward,
    restoreReward,
  } = useAccountData(activeAccountId)

  const {
    feed,
    eventCursor,
    eventsHasMore,
    loadingEvents,
    refreshEvents,
    loadMoreEvents,
  } = useEventFeedData()

  const { subscriptions, refreshSubscriptions, removeSubscription } = useSubscriptionData()

  const accounts = useMemo(
    () =>
      context?.availableAccounts.map((account) =>
        toProductAccount(account, account.id === activeAccountId ? accountCoin : 0),
      ) ?? [],
    [accountCoin, activeAccountId, context?.availableAccounts],
  )

  const activeAccount = useMemo(
    () => accounts.find((account) => account.id === activeAccountId) ?? null,
    [accounts, activeAccountId],
  )

  const user = useMemo<ProductUser>(
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
        if (account.active === false && account.owner) {
          await app.reactivate(account.id)
          return
        }

        await app.selectAccount(account.id)
      } catch (error) {
        handleError(error)
      }
    },
    [app],
  )

  const createAccount = useCallback(
    async (nickname: string) => {
      try {
        await app.registerAccount(nickname)
      } catch (error) {
        handleError(error)
        throw error
      }
    },
    [app],
  )

  const joinAccount = useCallback(
    async (inviteCode: string) => {
      try {
        await app.joinByCode(inviteCode)
      } catch (error) {
        handleError(error)
        throw error
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

  const value = useMemo<ProductDataState>(
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
      dailyReward,
      loadingAccount,
      loadingEvents,
      refreshAccount,
      refreshEvents,
      loadMoreEvents,
      refreshSubscriptions,
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
      dailyReward,
      loadingAccount,
      loadingEvents,
      refreshAccount,
      refreshEvents,
      loadMoreEvents,
      refreshSubscriptions,
      selectAccount,
      createAccount,
      joinAccount,
      setLanguage,
      claimReward,
      restoreReward,
      removeSubscription,
    ],
  )

  return <ProductDataContext.Provider value={value}>{children}</ProductDataContext.Provider>
}

export function useProductData() {
  const value = useContext(ProductDataContext)

  if (!value) {
    throw new Error("ProductDataProvider is missing")
  }

  return value
}
