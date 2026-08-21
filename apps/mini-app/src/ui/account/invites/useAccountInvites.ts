import {
  useCallback,
  useEffect,
  useLayoutEffect,
  useRef,
  useState,
  type Dispatch,
  type SetStateAction,
} from "react"
import {
  createAccountInvite,
  getAccountInvites,
  revokeAccountInvite,
  type AccountInvite,
} from "@/shared/api/accountInviteApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useMnemosData } from "../../data/MnemosDataProvider"

export type InviteExpiry = "NONE" | "1H" | "1D" | "7D"

type AccountInvitesData = {
  accountId: string | null
  canManageInvites: boolean
  expiry: InviteExpiry
  setExpiry: Dispatch<SetStateAction<InviteExpiry>>
  invites: AccountInvite[]
  loading: boolean
  loadError: boolean
  creating: boolean
  revokingId: string | null
  refresh(): Promise<void>
  createInvite(): Promise<void>
  revokeInvite(inviteId: string): Promise<void>
  copyInviteCode(code: string): Promise<void>
}

const expiryMinutes: Record<InviteExpiry, number | null> = {
  NONE: null,
  "1H": 60,
  "1D": 1_440,
  "7D": 10_080,
}

export function useAccountInvites(): AccountInvitesData {
  const data = useMnemosData()
  const accountId = data.activeAccount?.id ?? null
  const canManageInvites = data.hasPermission("MANAGE_MEMBERS")
  const [expiry, setExpiry] = useState<InviteExpiry>("1D")
  const [invites, setInvites] = useState<AccountInvite[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [loadedAccountId, setLoadedAccountId] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)
  const [revokingId, setRevokingId] = useState<string | null>(null)
  const requestRevision = useRef(0)
  const accountIdRef = useRef(accountId)
  const createLocked = useRef(false)
  const revokeLocked = useRef(false)
  const mountedRef = useRef(true)

  useLayoutEffect(() => {
    accountIdRef.current = accountId
  }, [accountId])

  const refresh = useCallback(async () => {
    const requestedAccountId = accountId
    const revision = requestRevision.current + 1

    requestRevision.current = revision

    if (!accountId) {
      if (accountIdRef.current === requestedAccountId) {
        setInvites([])
        setLoadedAccountId(null)
        setLoading(false)
      }

      return
    }

    setLoading(true)
    setLoadError(false)

    if (!canManageInvites) {
      if (revision === requestRevision.current && accountIdRef.current === requestedAccountId) {
        setInvites([])
        setLoadedAccountId(requestedAccountId)
        setLoading(false)
      }

      return
    }

    try {
      const nextInvites = await getAccountInvites(accountId)

      if (revision === requestRevision.current && accountIdRef.current === requestedAccountId) {
        setInvites(nextInvites)
        setLoadedAccountId(requestedAccountId)
      }
    } catch (error) {
      if (revision === requestRevision.current && accountIdRef.current === requestedAccountId) {
        setInvites([])
        setLoadedAccountId(requestedAccountId)
        setLoadError(true)
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current && accountIdRef.current === requestedAccountId) {
        setLoading(false)
      }
    }
  }, [accountId, canManageInvites])

  useEffect(() => {
    mountedRef.current = true
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => {
      requestRevision.current += 1
      mountedRef.current = false
      window.cancelAnimationFrame(frame)
    }
  }, [refresh])

  const createInvite = async () => {
    if (!accountId || !canManageInvites || createLocked.current) {
      return
    }

    const requestedAccountId = accountId

    createLocked.current = true
    setCreating(true)

    try {
      await createAccountInvite(accountId, expiryMinutes[expiry])

      if (accountIdRef.current === requestedAccountId) {
        await refresh()
      }
    } catch (error) {
      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        handleError(error)
      }
    } finally {
      createLocked.current = false

      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        setCreating(false)
      }
    }
  }

  const revokeInvite = async (inviteId: string) => {
    if (!accountId || !canManageInvites || revokeLocked.current) {
      return
    }

    const requestedAccountId = accountId

    revokeLocked.current = true
    setRevokingId(inviteId)

    try {
      await revokeAccountInvite(accountId, inviteId)

      if (accountIdRef.current === requestedAccountId) {
        await refresh()
      }
    } catch (error) {
      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        handleError(error)
      }
    } finally {
      revokeLocked.current = false

      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        setRevokingId(null)
      }
    }
  }

  const copyInviteCode = async (code: string) => {
    try {
      await navigator.clipboard.writeText(code)
    } catch (error) {
      handleError(error)
    }
  }

  const current = loadedAccountId === accountId

  return {
    accountId,
    canManageInvites,
    expiry,
    setExpiry,
    invites: current ? invites : [],
    loading: loading || !current,
    loadError,
    creating,
    revokingId,
    refresh,
    createInvite,
    revokeInvite,
    copyInviteCode,
  }
}
