import { useCallback, useEffect, useLayoutEffect, useRef, useState } from "react"
import {
  createSanction,
  createWalletAdjustment,
  getWalletAdjustment,
  listSanctions,
  listWalletAdjustments,
  revokeSanction,
  searchIdentities,
  type IdentitySearchResponse,
  type SanctionResponse,
  type WalletAdjustmentResponse,
} from "@/shared/api/moderationApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { SanctionType, TransactionType, WalletOwnerType } from "../../model"

type Options = {
  userId: string
  canAdjustWallet: boolean
}

export function useModerationUser({ userId, canAdjustWallet }: Options) {
  const [identity, setIdentity] = useState<IdentitySearchResponse | null>(null)
  const [sanctions, setSanctions] = useState<SanctionResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [loadedUserId, setLoadedUserId] = useState<string | null>(null)
  const [busyScope, setBusyScope] = useState<string | null>(null)
  const [sanctionFormOpen, setSanctionFormOpen] = useState(false)
  const [sanctionType, setSanctionType] = useState<SanctionType>("BAN")
  const [reason, setReason] = useState("")
  const [internalNote, setInternalNote] = useState("")
  const [duration, setDuration] = useState("")
  const [walletTarget, setWalletTarget] = useState<WalletOwnerType>("USER")
  const [accountTargetId, setAccountTargetId] = useState("")
  const [operation, setOperation] = useState<TransactionType>("CREDIT")
  const [amount, setAmount] = useState("100")
  const [walletReason, setWalletReason] = useState("")
  const [adjustments, setAdjustments] = useState<WalletAdjustmentResponse[]>([])
  const [adjustmentScope, setAdjustmentScope] = useState("")
  const identityRevision = useRef(0)
  const sanctionsRevision = useRef(0)
  const busyScopeRef = useRef<string | null>(null)
  const mountedRef = useRef(true)
  const userIdRef = useRef(userId)

  useLayoutEffect(() => {
    userIdRef.current = userId
  }, [userId])

  const setScopeBusy = useCallback((scope: string, value: boolean) => {
    if (!value && busyScopeRef.current !== scope) {
      return
    }

    const next = value ? scope : null

    busyScopeRef.current = next
    setBusyScope(next)
  }, [])

  const refreshIdentity = useCallback(async () => {
    const requestedUserId = userId
    const revision = identityRevision.current + 1

    identityRevision.current = revision

    if (!userId) {
      if (userIdRef.current === requestedUserId) {
        setIdentity(null)
      }
      return
    }

    const matches = await searchIdentities(userId, 5)
    const found = matches.find((item) => item.userId === userId) ?? matches[0] ?? null

    if (revision !== identityRevision.current || userIdRef.current !== requestedUserId) {
      return
    }

    setIdentity(found)

    if (!found) {
      setAccountTargetId("")
      return
    }

    const preferredAccount =
      found.accounts.find((account) => account.active)?.accountId ??
      found.accounts[0]?.accountId ??
      ""

    setAccountTargetId((current) =>
      current && found.accounts.some((account) => account.accountId === current)
        ? current
        : preferredAccount,
    )
  }, [userId])

  const refreshSanctions = useCallback(async () => {
    const requestedUserId = userId
    const revision = sanctionsRevision.current + 1

    sanctionsRevision.current = revision

    if (!userId) {
      if (userIdRef.current === requestedUserId) {
        setSanctions([])
      }
      return
    }

    const nextSanctions = await listSanctions(userId)

    if (revision === sanctionsRevision.current && userIdRef.current === requestedUserId) {
      setSanctions(nextSanctions)
    }
  }, [userId])

  useEffect(() => {
    let cancelled = false
    const requestedUserId = userId

    mountedRef.current = true

    const frame = window.requestAnimationFrame(() => {
      setLoading(true)

      void Promise.all([refreshIdentity(), refreshSanctions()])
        .catch((error) => {
          if (!cancelled && userIdRef.current === requestedUserId) {
            handleError(error)
          }
        })
        .finally(() => {
          if (!cancelled && userIdRef.current === requestedUserId) {
            setLoadedUserId(requestedUserId)
            setLoading(false)
          }
        })
    })

    return () => {
      cancelled = true
      identityRevision.current += 1
      sanctionsRevision.current += 1
      mountedRef.current = false
      window.cancelAnimationFrame(frame)
    }
  }, [refreshIdentity, refreshSanctions, userId])

  const targetId = walletTarget === "USER" ? userId : accountTargetId
  const currency = walletTarget === "USER" ? ("PRIME" as const) : ("COIN" as const)
  const scopeKey = targetId ? `${userId}:${walletTarget}:${targetId}` : ""
  const currentScopeRef = useRef(scopeKey)

  useLayoutEffect(() => {
    currentScopeRef.current = scopeKey
  }, [scopeKey])

  useEffect(() => {
    if (!canAdjustWallet || !targetId) {
      return
    }

    let cancelled = false
    const targetType = walletTarget
    const resolvedTargetId = targetId
    const resolvedScope = `${userId}:${targetType}:${resolvedTargetId}`

    const frame = window.requestAnimationFrame(() => {
      void listWalletAdjustments(targetType, resolvedTargetId)
        .then((items) => {
          if (cancelled) {
            return
          }

          setAdjustments(items)
          setAdjustmentScope(resolvedScope)
        })
        .catch((error) => {
          if (!cancelled && currentScopeRef.current === resolvedScope) {
            handleError(error)
          }
        })
    })

    return () => {
      cancelled = true
      window.cancelAnimationFrame(frame)
    }
  }, [canAdjustWallet, targetId, userId, walletTarget])

  const visibleAdjustments = adjustmentScope === scopeKey ? adjustments : []
  const numericDuration = Number(duration)
  const durationValid = !duration || (Number.isInteger(numericDuration) && numericDuration > 0)
  const numericAmount = Number(amount)
  const amountValid = Number.isInteger(numericAmount) && numericAmount > 0
  const currentSanctions = loadedUserId === userId ? sanctions : []
  const activeBan = currentSanctions.find(
    (sanction) => sanction.type === "BAN" && sanction.status === "ACTIVE",
  )

  const submitSanction = async () => {
    const actionScope = userId

    if (
      !userId ||
      !reason.trim() ||
      !durationValid ||
      busyScopeRef.current === actionScope
    ) {
      return
    }

    setScopeBusy(actionScope, true)

    try {
      await createSanction({
        userId,
        type: sanctionType,
        reason: reason.trim(),
        internalNote: internalNote.trim() || null,
        durationHours: duration ? numericDuration : null,
      })

      if (!mountedRef.current || userIdRef.current !== actionScope) {
        return
      }

      setReason("")
      setInternalNote("")
      setDuration("")
      setSanctionFormOpen(false)

      await refreshSanctions()
    } catch (error) {
      if (mountedRef.current && userIdRef.current === actionScope) {
        handleError(error)
      }
    } finally {
      if (mountedRef.current) {
        setScopeBusy(actionScope, false)
      }
    }
  }

  const revoke = async (sanctionId: string) => {
    const actionScope = userId

    if (!actionScope || busyScopeRef.current === actionScope) {
      return
    }

    setScopeBusy(actionScope, true)

    try {
      await revokeSanction(sanctionId)

      if (mountedRef.current && userIdRef.current === actionScope) {
        await refreshSanctions()
      }
    } catch (error) {
      if (mountedRef.current && userIdRef.current === actionScope) {
        handleError(error)
      }
    } finally {
      if (mountedRef.current) {
        setScopeBusy(actionScope, false)
      }
    }
  }

  const pollAdjustment = async (
    operationId: string,
    targetType: WalletOwnerType,
    resolvedTargetId: string,
  ) => {
    const resolvedScope = `${userIdRef.current}:${targetType}:${resolvedTargetId}`

    for (let attempt = 0; attempt < 15; attempt += 1) {
      if (!mountedRef.current || currentScopeRef.current !== resolvedScope) {
        return
      }

      await new Promise((resolve) => window.setTimeout(resolve, 800))

      if (!mountedRef.current || currentScopeRef.current !== resolvedScope) {
        return
      }

      const current = await getWalletAdjustment(operationId)

      if (current.status === "PENDING") {
        continue
      }

      const items = await listWalletAdjustments(targetType, resolvedTargetId)

      if (currentScopeRef.current === resolvedScope) {
        setAdjustments(items)
        setAdjustmentScope(resolvedScope)
      }

      return
    }
  }

  const submitAdjustment = async () => {
    const actionScope = userId

    if (
      !canAdjustWallet ||
      !targetId ||
      !amountValid ||
      !walletReason.trim() ||
      busyScopeRef.current === actionScope
    ) {
      return
    }

    const targetType = walletTarget
    const resolvedTargetId = targetId
    const resolvedScope = `${userId}:${targetType}:${resolvedTargetId}`

    setScopeBusy(actionScope, true)

    try {
      const created = await createWalletAdjustment({
        targetType,
        targetId: resolvedTargetId,
        currency,
        type: operation,
        amount: numericAmount,
        reason: walletReason.trim(),
      })

      if (currentScopeRef.current === resolvedScope) {
        setAdjustments((current) => [
          created,
          ...current.filter((item) => item.operationId !== created.operationId),
        ])
        setAdjustmentScope(resolvedScope)
      }

      if (currentScopeRef.current === resolvedScope) {
        setWalletReason("")
      }

      if (currentScopeRef.current === resolvedScope) {
        void pollAdjustment(created.operationId, targetType, resolvedTargetId).catch((error) => {
          if (mountedRef.current && currentScopeRef.current === resolvedScope) {
            handleError(error)
          }
        })
      }
    } catch (error) {
      if (mountedRef.current && userIdRef.current === actionScope) {
        handleError(error)
      }
    } finally {
      if (mountedRef.current) {
        setScopeBusy(actionScope, false)
      }
    }
  }

  return {
    identity: loadedUserId === userId ? identity : null,
    sanctions: currentSanctions,
    adjustments: visibleAdjustments,
    loading: loading || loadedUserId !== userId,
    busy: busyScope === userId,
    sanctionFormOpen,
    sanctionType,
    reason,
    internalNote,
    duration,
    durationValid,
    walletTarget,
    accountTargetId,
    operation,
    amount,
    amountValid,
    walletReason,
    targetId,
    currency,
    activeBan,
    setSanctionFormOpen,
    setSanctionType,
    setReason,
    setInternalNote,
    setDuration,
    setWalletTarget,
    setAccountTargetId,
    setOperation,
    setAmount,
    setWalletReason,
    submitSanction,
    revoke,
    submitAdjustment,
  }
}

export type ModerationUserController = ReturnType<typeof useModerationUser>
