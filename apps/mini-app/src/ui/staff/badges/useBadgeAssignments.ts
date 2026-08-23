import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from "react"
import {
  grantAdminBadge,
  listAdminUserBadges,
  revokeAdminBadge,
  type BadgeResponse,
  type UserBadgeResponse,
} from "@/shared/api/badgeAdminApi"
import { searchIdentities, type IdentitySearchResponse } from "@/shared/api/moderationApi"
import { handleError } from "@/shared/errors/errorHandler"

export function useBadgeAssignments() {
  const [query, setQuery] = useState("")
  const [searchResult, setSearchResult] = useState<{
    query: string
    items: IdentitySearchResponse[]
  }>({ query: "", items: [] })
  const [searchErrorQuery, setSearchErrorQuery] = useState<string | null>(null)
  const [selectedUser, setSelectedUser] = useState<IdentitySearchResponse | null>(null)
  const [userBadgeResult, setUserBadgeResult] = useState<{
    userId: string
    items: UserBadgeResponse[]
  }>({ userId: "", items: [] })
  const [busy, setBusy] = useState(false)
  const userBadgeRequestRevision = useRef(0)
  const busyRef = useRef(false)
  const mountedRef = useRef(true)
  const selectedUserIdRef = useRef<string | null>(selectedUser?.userId ?? null)
  const normalizedQuery = query.trim()

  useLayoutEffect(() => {
    selectedUserIdRef.current = selectedUser?.userId ?? null
  }, [selectedUser?.userId])

  useEffect(() => {
    mountedRef.current = true

    return () => {
      userBadgeRequestRevision.current += 1
      busyRef.current = false
      mountedRef.current = false
    }
  }, [])

  useEffect(() => {
    if (normalizedQuery.length < 2) {
      return
    }

    let cancelled = false
    const timer = window.setTimeout(() => {
      setSearchErrorQuery(null)

      void searchIdentities(normalizedQuery)
        .then((items) => {
          if (!cancelled) {
            setSearchErrorQuery(null)
            setSearchResult({ query: normalizedQuery, items })
          }
        })
        .catch((error) => {
          if (!cancelled) {
            setSearchErrorQuery(normalizedQuery)
            handleError(error)
          }
        })
    }, 250)

    return () => {
      cancelled = true
      window.clearTimeout(timer)
    }
  }, [normalizedQuery])

  const refreshUserBadges = useCallback(async (userId: string) => {
    const revision = userBadgeRequestRevision.current + 1

    userBadgeRequestRevision.current = revision

    try {
      const items = await listAdminUserBadges(userId)

      if (revision === userBadgeRequestRevision.current) {
        setUserBadgeResult({ userId, items })
      }
    } catch (error) {
      if (revision === userBadgeRequestRevision.current) {
        setUserBadgeResult({ userId, items: [] })
        handleError(error)
      }
    }
  }, [])

  useEffect(() => {
    if (!selectedUser) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      void refreshUserBadges(selectedUser.userId)
    })

    return () => {
      userBadgeRequestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [refreshUserBadges, selectedUser])

  const userBadges = useMemo(
    () => (selectedUser?.userId === userBadgeResult.userId ? userBadgeResult.items : []),
    [selectedUser?.userId, userBadgeResult],
  )
  const assignedIds = useMemo(() => new Set(userBadges.map((item) => item.badge.id)), [userBadges])
  const visibleResults =
    normalizedQuery.length >= 2 && searchResult.query === normalizedQuery ? searchResult.items : []
  const searchFailed = searchErrorQuery === normalizedQuery

  const toggleAssignment = async (badge: BadgeResponse) => {
    if (!selectedUser || busyRef.current) {
      return
    }

    busyRef.current = true
    setBusy(true)
    const selectedUserId = selectedUser.userId

    try {
      if (assignedIds.has(badge.id)) {
        await revokeAdminBadge(badge.id, selectedUserId)
      } else {
        await grantAdminBadge(badge.id, selectedUserId)
      }

      if (mountedRef.current && selectedUserIdRef.current === selectedUserId) {
        await refreshUserBadges(selectedUserId)
      }
    } catch (error) {
      if (mountedRef.current) {
        handleError(error)
      }
    } finally {
      busyRef.current = false

      if (mountedRef.current) {
        setBusy(false)
      }
    }
  }

  return {
    query,
    setQuery,
    normalizedQuery,
    visibleResults,
    searchFailed,
    selectedUser,
    setSelectedUser,
    userBadges,
    assignedIds,
    busy,
    toggleAssignment,
  }
}
