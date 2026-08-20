import { useCallback, useEffect, useLayoutEffect, useRef, useState } from "react"
import type { DailyRewardStateResponse } from "@/shared/api/backendContracts"
import {
  claimDailyReward,
  getAccountBalances,
  getAccountMembers,
  getAccountRoles,
  getCurrentUserBalances,
  getDailyRewardState,
  restoreDailyReward,
} from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { toProductMember, toProductRole } from "../../data/productMappers"
import type { AccountRole, Member } from "../../model"

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

export function useAccountData(accountId: string | undefined): AccountData {
  const [members, setMembers] = useState<Member[]>([])
  const [roles, setRoles] = useState<AccountRole[]>([])
  const [dailyReward, setDailyReward] = useState<DailyRewardStateResponse | null>(null)
  const [accountCoin, setAccountCoin] = useState(0)
  const [prime, setPrime] = useState(0)
  const [loadingAccount, setLoadingAccount] = useState(false)
  const [accountError, setAccountError] = useState(false)
  const [loadedScopeKey, setLoadedScopeKey] = useState<string | null>(null)
  const requestRevision = useRef(0)
  const activeAccountId = useRef(accountId)
  const scopeKey = accountId ?? "user"

  useLayoutEffect(() => {
    activeAccountId.current = accountId
  }, [accountId])

  const refreshAccount = useCallback(async () => {
    const requestedAccountId = accountId
    const requestedScopeKey = requestedAccountId ?? "user"
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoadingAccount(true)
    setAccountError(false)

    try {
      const userBalancesPromise = getCurrentUserBalances()

      if (!requestedAccountId) {
        const userBalances = await userBalancesPromise

        if (revision !== requestRevision.current || activeAccountId.current) {
          return
        }

        setPrime(userBalances.find((balance) => balance.currency === "PRIME")?.amount ?? 0)
        setMembers([])
        setRoles([])
        setDailyReward(null)
        setAccountCoin(0)
        setLoadedScopeKey(requestedScopeKey)
        return
      }

      const [memberResponses, roleResponses, balances, rewardState, userBalances] =
        await Promise.all([
          getAccountMembers(requestedAccountId),
          getAccountRoles(requestedAccountId),
          getAccountBalances(requestedAccountId),
          getDailyRewardState(),
          userBalancesPromise,
        ])

      if (
        revision !== requestRevision.current ||
        activeAccountId.current !== requestedAccountId
      ) {
        return
      }

      setMembers(memberResponses.map((member) => toProductMember(member)))
      setRoles(roleResponses.map(toProductRole))
      setAccountCoin(balances.find((balance) => balance.currency === "COIN")?.amount ?? 0)
      setPrime(userBalances.find((balance) => balance.currency === "PRIME")?.amount ?? 0)
      setDailyReward(rewardState)
      setLoadedScopeKey(requestedScopeKey)
    } catch (error) {
      if (
        revision === requestRevision.current &&
        activeAccountId.current === requestedAccountId
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
        activeAccountId.current === requestedAccountId
      ) {
        setLoadingAccount(false)
      }
    }
  }, [accountId])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refreshAccount()
    })

    return () => {
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [accountId, refreshAccount])

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
