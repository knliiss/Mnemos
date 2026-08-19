import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react"
import { useApp } from "@/app/providers/AppProvider"
import type { DailyRewardStateResponse } from "@/shared/api/backendContracts"
import {
  claimDailyReward,
  deleteSubscription as deleteSubscriptionRequest,
  getAccountBalances,
  getAccountMembers,
  getAccountRoles,
  getCurrentUserBalances,
  getDailyRewardState,
  getEventFeed,
  getSubscriptions,
  restoreDailyReward,
  type EventFeedQuery,
} from "@/shared/api/productApi"
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
import {
  toProductAccount,
  toProductFeedEvent,
  toProductMember,
  toProductRole,
  toProductSubscription,
} from "./productMappers"

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
  const [members, setMembers] = useState<Member[]>([])
  const [roles, setRoles] = useState<AccountRole[]>([])
  const [feed, setFeed] = useState<FeedEvent[]>([])
  const [eventCursor, setEventCursor] = useState<string | null>(null)
  const [eventsHasMore, setEventsHasMore] = useState(true)
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([])
  const [dailyReward, setDailyReward] = useState<DailyRewardStateResponse | null>(null)
  const [accountCoin, setAccountCoin] = useState(0)
  const [prime, setPrime] = useState(0)
  const [loadingAccount, setLoadingAccount] = useState(false)
  const [loadingEvents, setLoadingEvents] = useState(false)

  const context = app.context

  const accounts = useMemo(
    () =>
      context?.availableAccounts.map((account) =>
        toProductAccount(account, account.id === context.activeAccount?.id ? accountCoin : 0),
      ) ?? [],
    [accountCoin, context],
  )

  const activeAccount = useMemo(
    () => accounts.find((account) => account.id === context?.activeAccount?.id) ?? null,
    [accounts, context?.activeAccount?.id],
  )

  const user = useMemo<ProductUser>(
    () => ({
      id: context?.user.id ?? "",
      username: context?.user.username ?? "",
      language: context?.user.localizationLanguage ?? "UK",
      prime,
      systemRoles: context?.systemRoles ?? [],
    }),
    [context, prime],
  )

  const refreshAccount = useCallback(async () => {
    const accountId = context?.activeAccount?.id

    setLoadingAccount(true)

    try {
      const userBalancesPromise = getCurrentUserBalances()

      if (!accountId) {
        const userBalances = await userBalancesPromise

        setPrime(userBalances.find((balance) => balance.currency === "PRIME")?.amount ?? 0)
        setMembers([])
        setRoles([])
        setDailyReward(null)
        setAccountCoin(0)
        return
      }

      const [memberResponses, roleResponses, balances, rewardState, userBalances] =
        await Promise.all([
          getAccountMembers(accountId),
          getAccountRoles(accountId),
          getAccountBalances(accountId),
          getDailyRewardState(),
          userBalancesPromise,
        ])

      setMembers(memberResponses.map((member) => toProductMember(member)))
      setRoles(roleResponses.map(toProductRole))
      setAccountCoin(balances.find((balance) => balance.currency === "COIN")?.amount ?? 0)
      setPrime(userBalances.find((balance) => balance.currency === "PRIME")?.amount ?? 0)
      setDailyReward(rewardState)
    } catch (error) {
      handleError(error)
    } finally {
      setLoadingAccount(false)
    }
  }, [context?.activeAccount?.id])

  const refreshEvents = useCallback(async (query: EventFeedQuery = {}) => {
    setLoadingEvents(true)

    try {
      const limit = query.limit ?? 50
      const response = await getEventFeed({ ...query, after: null })

      setFeed(response.items.map(toProductFeedEvent))
      setEventCursor(response.cursor)
      setEventsHasMore(response.items.length >= limit && response.cursor !== null)
    } catch (error) {
      handleError(error)
    } finally {
      setLoadingEvents(false)
    }
  }, [])

  const loadMoreEvents = useCallback(
    async (query: EventFeedQuery = {}) => {
      if (loadingEvents || !eventsHasMore || !eventCursor) return

      setLoadingEvents(true)

      try {
        const limit = query.limit ?? 50
        const response = await getEventFeed({
          ...query,
          after: eventCursor,
        })
        const nextItems = response.items.map(toProductFeedEvent)

        setFeed((current) => {
          const knownIds = new Set(current.map((item) => item.id))
          return [...current, ...nextItems.filter((item) => !knownIds.has(item.id))]
        })
        setEventCursor(response.cursor)
        setEventsHasMore(nextItems.length >= limit && response.cursor !== null)
      } catch (error) {
        handleError(error)
      } finally {
        setLoadingEvents(false)
      }
    },
    [eventCursor, eventsHasMore, loadingEvents],
  )

  const refreshSubscriptions = useCallback(async () => {
    try {
      const response = await getSubscriptions()
      setSubscriptions(
        response.items
          .map(toProductSubscription)
          .filter((subscription): subscription is Subscription => subscription !== null),
      )
    } catch (error) {
      handleError(error)
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refreshAccount()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refreshAccount])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refreshEvents()
      void refreshSubscriptions()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refreshEvents, refreshSubscriptions])

  const selectAccount = useCallback(
    async (account: Account) => {
      try {
        if (account.active === false && account.owner) {
          await app.reactivate(account.id)
        } else {
          await app.selectAccount(account.id)
        }
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

  const claimReward = useCallback(async () => {
    try {
      const result = await claimDailyReward()

      setDailyReward((current) =>
        current
          ? {
              ...current,
              currentStreak: result.streak,
              claimedToday: true,
              recoverable: false,
            }
          : current,
      )
      setAccountCoin((current) => current + result.coins)
      setPrime((current) => current + result.primes)

      return {
        coin: result.coins,
        prime: result.primes,
      }
    } catch (error) {
      handleError(error)
      throw error
    }
  }, [])

  const restoreReward = useCallback(async () => {
    try {
      const result = await restoreDailyReward()
      setDailyReward((current) =>
        current
          ? {
              ...current,
              currentStreak: result.currentStreak,
              recoverable: false,
            }
          : current,
      )
    } catch (error) {
      handleError(error)
      throw error
    }
  }, [])

  const removeSubscription = useCallback(async (subscriptionId: string) => {
    try {
      await deleteSubscriptionRequest(subscriptionId)
      setSubscriptions((current) =>
        current.filter((subscription) => subscription.id !== subscriptionId),
      )
    } catch (error) {
      handleError(error)
      throw error
    }
  }, [])

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
