import { useCallback, useEffect, useLayoutEffect, useRef, useState } from "react"
import { getAccountMembers, getAccountRoles, getMemberRoles } from "@/shared/api/accountApi"
import type { DailyRewardStateResponse } from "@/shared/api/backendContracts"
import { claimDailyReward, getDailyRewardState, restoreDailyReward } from "@/shared/api/rewardApi"
import { getAccountBalances, getCurrentUserBalances } from "@/shared/api/walletApi"
import { handleError } from "@/shared/errors/errorHandler"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import { toMember, toRole } from "../../data/dataMappers"
import type { AccountRole, Member } from "../../model"

const BALANCE_RECONCILIATION_INTERVAL_MS = 30_000

type AccountData = {
  members: Member[]
  roles: AccountRole[]
  dailyReward: DailyRewardStateResponse | null
  accountCoin: number
  prime: number
  loadingAccount: boolean
  accountError: boolean
  refreshAccount(): Promise<void>
  claimReward(): Promise<{ coin: number; prime: number } | null>
  restoreReward(): Promise<void>
}

export function useAccountData(
  accountId: string | undefined,
  currentMemberId: string | undefined,
): AccountData {
  const [members, setMembers] = useState<Member[]>([])
  const [roles, setRoles] = useState<AccountRole[]>([])
  const [dailyReward, setDailyReward] = useState<DailyRewardStateResponse | null>(null)
  const [accountCoin, setAccountCoin] = useState(0)
  const [prime, setPrime] = useState(0)
  const [loadingAccount, setLoadingAccount] = useState(false)
  const [accountError, setAccountError] = useState(false)
  const [loadedScopeKey, setLoadedScopeKey] = useState<string | null>(null)
  const requestRevision = useRef(0)
  const accountBalanceRevision = useRef(0)
  const userBalanceRevision = useRef(0)
  const activeAccountId = useRef(accountId)
  const activeMemberId = useRef(currentMemberId)
  const scopeKey = accountId ? `${accountId}:${currentMemberId ?? "unknown"}` : "user"

  useLayoutEffect(() => {
    activeAccountId.current = accountId
    activeMemberId.current = currentMemberId
  }, [accountId, currentMemberId])

  const refreshUserBalance = useCallback(async () => {
    const revision = userBalanceRevision.current + 1

    userBalanceRevision.current = revision

    const balances = await getCurrentUserBalances()

    if (revision !== userBalanceRevision.current) {
      return
    }

    setPrime(balances.find((balance) => balance.currency === "PRIME")?.amount ?? 0)
  }, [])

  const refreshAccountBalance = useCallback(async (requestedAccountId: string) => {
    const revision = accountBalanceRevision.current + 1

    accountBalanceRevision.current = revision

    const balances = await getAccountBalances(requestedAccountId)

    if (
      revision !== accountBalanceRevision.current ||
      activeAccountId.current !== requestedAccountId
    ) {
      return
    }

    setAccountCoin(balances.find((balance) => balance.currency === "COIN")?.amount ?? 0)
  }, [])

  const reconcileBalances = useCallback(async () => {
    const currentAccountId = activeAccountId.current
    const requests: Promise<void>[] = [refreshUserBalance()]

    if (currentAccountId) {
      requests.push(refreshAccountBalance(currentAccountId))
    }

    await Promise.all(requests)
  }, [refreshAccountBalance, refreshUserBalance])

  const refreshAccount = useCallback(async () => {
    const requestedAccountId = accountId
    const requestedMemberId = currentMemberId
    const requestedScopeKey = requestedAccountId
      ? `${requestedAccountId}:${requestedMemberId ?? "unknown"}`
      : "user"
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoadingAccount(true)
    setAccountError(false)

    try {
      const userBalancePromise = refreshUserBalance()

      if (!requestedAccountId) {
        await userBalancePromise

        if (revision !== requestRevision.current || activeAccountId.current) {
          return
        }

        setMembers([])
        setRoles([])
        setDailyReward(null)
        setAccountCoin(0)
        setLoadedScopeKey(requestedScopeKey)
        return
      }

      const actorRolesPromise = requestedMemberId
        ? getMemberRoles(requestedAccountId, requestedMemberId)
        : Promise.resolve([])
      const [memberResponses, roleResponses, rewardState, actorRoleResponses] = await Promise.all([
        getAccountMembers(requestedAccountId),
        getAccountRoles(requestedAccountId),
        getDailyRewardState(),
        actorRolesPromise,
        refreshAccountBalance(requestedAccountId),
        userBalancePromise,
      ])

      if (
        revision !== requestRevision.current ||
        activeAccountId.current !== requestedAccountId ||
        activeMemberId.current !== requestedMemberId
      ) {
        return
      }

      const actorRoleIds = actorRoleResponses.map((role) => role.id)

      setMembers(
        memberResponses.map((member) =>
          toMember(member, member.id === requestedMemberId ? actorRoleIds : undefined),
        ),
      )
      setRoles(roleResponses.map(toRole))
      setDailyReward(rewardState)
      setLoadedScopeKey(requestedScopeKey)
    } catch (error) {
      if (
        revision === requestRevision.current &&
        activeAccountId.current === requestedAccountId &&
        activeMemberId.current === requestedMemberId
      ) {
        setMembers([])
        setRoles([])
        setDailyReward(null)
        setAccountCoin(0)
        setLoadedScopeKey(requestedScopeKey)
        setAccountError(true)
        handleError(error)
      }
    } finally {
      if (
        revision === requestRevision.current &&
        activeAccountId.current === requestedAccountId &&
        activeMemberId.current === requestedMemberId
      ) {
        setLoadingAccount(false)
      }
    }
  }, [accountId, currentMemberId, refreshAccountBalance, refreshUserBalance])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refreshAccount()
    })

    return () => {
      requestRevision.current += 1
      accountBalanceRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [accountId, currentMemberId, refreshAccount])

  useEffect(() => {
    const unsubscribe = realtimeEvents.subscribe((event) => {
      if (event.type !== "wallet.transaction.completed") {
        return
      }

      if (event.payload.ownerType === "USER") {
        void refreshUserBalance().catch(handleError)
        return
      }

      const currentAccountId = activeAccountId.current

      if (
        currentAccountId &&
        event.payload.ownerType === "ACCOUNT" &&
        event.payload.ownerId === currentAccountId
      ) {
        void refreshAccountBalance(currentAccountId).catch(handleError)
      }
    })

    return () => {
      unsubscribe()
    }
  }, [refreshAccountBalance, refreshUserBalance])

  useEffect(() => {
    const reconcile = () => {
      void reconcileBalances().catch(handleError)
    }
    const reconcileWhenVisible = () => {
      if (document.visibilityState === "visible") {
        reconcile()
      }
    }
    const intervalId = window.setInterval(
      reconcileWhenVisible,
      BALANCE_RECONCILIATION_INTERVAL_MS,
    )

    window.addEventListener("focus", reconcile)
    document.addEventListener("visibilitychange", reconcileWhenVisible)

    return () => {
      window.clearInterval(intervalId)
      window.removeEventListener("focus", reconcile)
      document.removeEventListener("visibilitychange", reconcileWhenVisible)
    }
  }, [reconcileBalances])

  const claimReward = useCallback(async () => {
    const requestedAccountId = accountId

    try {
      const result = await claimDailyReward()

      if (activeAccountId.current !== requestedAccountId) {
        return null
      }

      await refreshAccount()

      if (activeAccountId.current !== requestedAccountId) {
        return null
      }

      return {
        coin: result.coins,
        prime: result.primes,
      }
    } catch (error) {
      if (activeAccountId.current === requestedAccountId) {
        handleError(error)
      }
      throw error
    }
  }, [accountId, refreshAccount])

  const restoreReward = useCallback(async () => {
    const requestedAccountId = accountId

    try {
      await restoreDailyReward()

      if (activeAccountId.current !== requestedAccountId) {
        return
      }

      await refreshAccount()
    } catch (error) {
      if (activeAccountId.current === requestedAccountId) {
        handleError(error)
      }
      throw error
    }
  }, [accountId, refreshAccount])

  const scopeLoaded = loadedScopeKey === scopeKey

  return {
    members: scopeLoaded ? members : [],
    roles: scopeLoaded ? roles : [],
    dailyReward: scopeLoaded ? dailyReward : null,
    accountCoin: scopeLoaded ? accountCoin : 0,
    prime,
    loadingAccount: loadingAccount || !scopeLoaded,
    accountError: scopeLoaded && accountError,
    refreshAccount,
    claimReward,
    restoreReward,
  }
}
