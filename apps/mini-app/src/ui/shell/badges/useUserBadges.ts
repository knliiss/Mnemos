import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import {
  activateMyBadge,
  deactivateMyBadge,
  listMyBadges,
  type UserBadge,
} from "@/shared/api/badgeApi"
import { handleError } from "@/shared/errors/errorHandler"

export type UserBadgesController = {
  badges: UserBadge[]
  activeBadge: UserBadge | null
  loading: boolean
  busyBadgeId: string | null
  refresh: () => Promise<void>
  activate: (badgeId: string) => Promise<void>
  deactivate: () => Promise<void>
}

export function useUserBadges(): UserBadgesController {
  const [badges, setBadges] = useState<UserBadge[]>([])
  const [loading, setLoading] = useState(true)
  const [busyBadgeId, setBusyBadgeId] = useState<string | null>(null)
  const mountedRef = useRef(true)
  const mutationInProgress = useRef(false)
  const requestRevision = useRef(0)

  const activeBadge = useMemo(() => badges.find((item) => item.active) ?? null, [badges])

  const refresh = useCallback(async () => {
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoading(true)

    try {
      const nextBadges = await listMyBadges()

      if (mountedRef.current && revision === requestRevision.current) {
        setBadges(nextBadges)
      }
    } catch (error) {
      if (mountedRef.current && revision === requestRevision.current) {
        setBadges([])
        handleError(error)
      }
    } finally {
      if (mountedRef.current && revision === requestRevision.current) {
        setLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    mountedRef.current = true

    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => {
      mountedRef.current = false
      mutationInProgress.current = false
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [refresh])

  const activate = async (badgeId: string) => {
    if (mutationInProgress.current) {
      return
    }

    mutationInProgress.current = true
    setBusyBadgeId(badgeId)

    try {
      const activated = await activateMyBadge(badgeId)

      if (!mountedRef.current) {
        return
      }

      setBadges((current) =>
        current.map((item) => ({
          ...item,
          active: item.badge.id === activated.badge.id,
        })),
      )
    } catch (error) {
      if (mountedRef.current) {
        handleError(error)
      }
    } finally {
      mutationInProgress.current = false

      if (mountedRef.current) {
        setBusyBadgeId(null)
      }
    }
  }

  const deactivate = async () => {
    if (mutationInProgress.current || !activeBadge) {
      return
    }

    mutationInProgress.current = true
    setBusyBadgeId(activeBadge.badge.id)

    try {
      await deactivateMyBadge()

      if (mountedRef.current) {
        setBadges((current) => current.map((item) => ({ ...item, active: false })))
      }
    } catch (error) {
      if (mountedRef.current) {
        handleError(error)
      }
    } finally {
      mutationInProgress.current = false

      if (mountedRef.current) {
        setBusyBadgeId(null)
      }
    }
  }

  return {
    badges,
    activeBadge,
    loading,
    busyBadgeId,
    refresh,
    activate,
    deactivate,
  }
}
