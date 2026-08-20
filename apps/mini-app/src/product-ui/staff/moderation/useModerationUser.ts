import { useCallback, useEffect, useRef, useState } from "react"
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
  const [busy, setBusy] = useState(false)
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

  const refreshIdentity = useCallback(async () => {
    if (!userId) {
      setIdentity(null)
      return
    }

    const matches = await searchIdentities(userId, 5)
    const found = matches.find((item) => item.userId === userId) ?? matches[0] ?? null

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
    if (!userId) {
      setSanctions([])
      return
    }

    setSanctions(await listSanctions(userId))
  }, [userId])

  useEffect(() => {
    let cancelled = false

    const frame = window.requestAnimationFrame(() => {
      void Promise.all([refreshIdentity(), refreshSanctions()])
        .catch(handleError)
        .finally(() => {
          if (!cancelled) {
            setLoading(false)
          }
        })
    })

    return () => {
      cancelled = true
      window.cancelAnimationFrame(frame)
    }
  }, [refreshIdentity, refreshSanctions])

  const targetId = walletTarget === "USER" ? userId : accountTargetId
  const currency = walletTarget === "USER" ? ("PRIME" as const) : ("COIN" as const)
  const scopeKey = targetId ? `${walletTarget}:${targetId}` : ""
  const currentScopeRef = useRef(scopeKey)

  useEffect(() => {
    currentScopeRef.current = scopeKey
  }, [scopeKey])

  useEffect(() => {
    if (!canAdjustWallet || !targetId) {
      return
    }

    let cancelled = false
    const targetType = walletTarget
    const resolvedTargetId = targetId
    const resolvedScope = `${targetType}:${resolvedTargetId}`

    const frame = window.requestAnimationFrame(() => {
      void listWalletAdjustments(targetType, resolvedTargetId)
        .then((items) => {
          if (cancelled) {
            return
          }

          setAdjustments(items)
          setAdjustmentScope(resolvedScope)
        })
        .catch(handleError)
    })

    return () => {
      cancelled = true
      window.cancelAnimationFrame(frame)
    }
  }, [canAdjustWallet, targetId, walletTarget])

  const visibleAdjustments = adjustmentScope === scopeKey ? adjustments : []
  const numericDuration = Number(duration)
  const durationValid = !duration || (Number.isInteger(numericDuration) && numericDuration > 0)
  const numericAmount = Number(amount)
  const amountValid = Number.isInteger(numericAmount) && numericAmount > 0
  const activeBan = sanctions.find(
    (sanction) => sanction.type === "BAN" && sanction.status === "ACTIVE",
  )

  const submitSanction = async () => {
    if (!userId || !reason.trim() || !durationValid || busy) {
      return
    }

    setBusy(true)

    try {
      await createSanction({
        userId,
        type: sanctionType,
        reason: reason.trim(),
        internalNote: internalNote.trim() || null,
        durationHours: duration ? numericDuration : null,
      })

      setReason("")
      setInternalNote("")
      setDuration("")
      setSanctionFormOpen(false)

      await refreshSanctions()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const revoke = async (sanctionId: string) => {
    if (busy) {
      return
    }

    setBusy(true)

    try {
      await revokeSanction(sanctionId)
      await refreshSanctions()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const pollAdjustment = async (
    operationId: string,
    targetType: WalletOwnerType,
    resolvedTargetId: string,
  ) => {
    const resolvedScope = `${targetType}:${resolvedTargetId}`

    for (let attempt = 0; attempt < 15; attempt += 1) {
      await new Promise((resolve) => window.setTimeout(resolve, 800))

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
    if (!canAdjustWallet || !targetId || !amountValid || !walletReason.trim() || busy) {
      return
    }

    const targetType = walletTarget
    const resolvedTargetId = targetId
    const resolvedScope = `${targetType}:${resolvedTargetId}`

    setBusy(true)

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

      setWalletReason("")

      void pollAdjustment(created.operationId, targetType, resolvedTargetId).catch(handleError)
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  return {
    identity,
    sanctions,
    adjustments: visibleAdjustments,
    loading,
    busy,
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
