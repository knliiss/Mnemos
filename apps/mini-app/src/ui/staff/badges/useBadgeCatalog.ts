import { useCallback, useEffect, useRef, useState } from "react"
import { createAdminBadge, listAdminBadges, type BadgeResponse } from "@/shared/api/badgeAdminApi"
import { handleError } from "@/shared/errors/errorHandler"

export type BadgeDraft = {
  code: string
  name: string
  image: File
  color: string
}

export function useBadgeCatalog() {
  const [badges, setBadges] = useState<BadgeResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [creating, setCreating] = useState(false)
  const requestRevision = useRef(0)
  const creatingRef = useRef(false)
  const mountedRef = useRef(true)

  const refresh = useCallback(async () => {
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoading(true)
    setLoadError(false)

    try {
      const nextBadges = await listAdminBadges()

      if (revision === requestRevision.current) {
        setBadges(nextBadges)
      }
    } catch (error) {
      if (revision === requestRevision.current) {
        setBadges([])
        setLoadError(true)
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current) {
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
      requestRevision.current += 1
      mountedRef.current = false
      creatingRef.current = false
      window.cancelAnimationFrame(frame)
    }
  }, [refresh])

  const create = async (draft: BadgeDraft) => {
    if (creatingRef.current) {
      return false
    }

    creatingRef.current = true
    setCreating(true)

    try {
      await createAdminBadge(draft)

      if (!mountedRef.current) {
        return false
      }

      await refresh()
      return mountedRef.current
    } catch (error) {
      if (mountedRef.current) {
        handleError(error)
      }

      return false
    } finally {
      creatingRef.current = false

      if (mountedRef.current) {
        setCreating(false)
      }
    }
  }

  return {
    badges,
    loading,
    loadError,
    creating,
    refresh,
    create,
  }
}
