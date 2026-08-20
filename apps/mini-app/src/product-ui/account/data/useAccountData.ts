import { useCallback, useEffect, useRef, useState } from "react"
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
  refreshAccount(): Promise<void>
  claimReward(): Promise<{ coin: number; prime: number }>
  restoreReward(): Promise<void>
}

export function useAccountData(accountId: string | undefined): AccountData {
  const [members, setMembers] = useState<Member[]>([])
  const [roles, setRoles] = useState<AccountRole[]>([])
  const [dailyReward, setDailyReward] = useState<DailyRewardStateResponse | null>(null)
  const [accountCoin, setAccountCoin] = useState(0)
  const [prime, setPrime] = useState(0)
  const [loadingAccount, setLoadingAccount] = useState(false)
  const requestRevision = useRef(0)

  const refreshAccount = useCallback(async () => {
    const revision = requestRevision.current + 1
    requestRevision.current = revision
    setLoadingAccount(true)

    try {
      const userBalancesPromise = getCurrentUserBalances()

      if (!accountId) {
        const userBalances = await userBalancesPromise

        if (revision !== requestRevision.current) {
          return
        }

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

      if (revision !== requestRevision.current) {
        return
      }

      setMembers(memberResponses.map((member) => toProductMember(member)))
      setRoles(roleResponses.map(toProductRole))
      setAccountCoin(balances.find((balance) => balance.currency === "COIN")?.amount ?? 0)
      setPrime(userBalances.find((balance) => balance.currency === "PRIME")?.amount ?? 0)
      setDailyReward(rewardState)
    } catch (error) {
      if (revision === requestRevision.current) {
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current) {
        setLoadingAccount(false)
      }
    }
  }, [accountId])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refreshAccount()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refreshAccount])

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

  return {
    members,
    roles,
    dailyReward,
    accountCoin,
    prime,
    loadingAccount,
    refreshAccount,
    claimReward,
    restoreReward,
  }
}
