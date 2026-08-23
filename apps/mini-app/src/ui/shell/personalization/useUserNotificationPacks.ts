import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { getCreatorPersonalization } from "@/shared/api/creatorPersonalizationApi"
import {
  activatePersonalization,
  deactivatePersonalization,
  getPersonalization,
  listOwnedPersonalizations,
  type PersonalizationResponse,
} from "@/shared/api/personalizationApi"
import { handleError } from "@/shared/errors/errorHandler"

export type UserNotificationPack = {
  id: string
  name: string
  active: boolean
}

async function loadPersonalization(personalizationId: string): Promise<PersonalizationResponse> {
  try {
    return await getPersonalization(personalizationId)
  } catch {
    return getCreatorPersonalization(personalizationId)
  }
}

export function useUserNotificationPacks(enabled: boolean) {
  const [packs, setPacks] = useState<UserNotificationPack[]>([])
  const [loading, setLoading] = useState(false)
  const [busyPackId, setBusyPackId] = useState<string | null>(null)
  const mountedRef = useRef(true)
  const mutationInProgress = useRef(false)
  const requestRevision = useRef(0)

  const activePack = useMemo(() => packs.find((pack) => pack.active) ?? null, [packs])

  const refresh = useCallback(async () => {
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoading(true)

    try {
      const owned = await listOwnedPersonalizations("NOTIFICATION_PACK", 0, 100)
      const details = await Promise.all(
        owned.items.map((item) => loadPersonalization(item.personalizationId)),
      )
      const detailsById = new Map(details.map((item) => [item.id, item]))
      const nextPacks = owned.items.map((item) => ({
        id: item.personalizationId,
        name: detailsById.get(item.personalizationId)?.name ?? item.personalizationId,
        active: item.active,
      }))

      if (mountedRef.current && revision === requestRevision.current) {
        setPacks(nextPacks)
      }
    } catch (error) {
      if (mountedRef.current && revision === requestRevision.current) {
        setPacks([])
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

    return () => {
      mountedRef.current = false
      mutationInProgress.current = false
      requestRevision.current += 1
    }
  }, [])

  useEffect(() => {
    if (!enabled) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => {
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [enabled, refresh])

  const activate = async (personalizationId: string) => {
    if (mutationInProgress.current) {
      return
    }

    mutationInProgress.current = true
    setBusyPackId(personalizationId)

    try {
      const activated = await activatePersonalization(personalizationId)

      if (mountedRef.current) {
        setPacks((current) =>
          current.map((pack) => ({
            ...pack,
            active: pack.id === activated.personalizationId,
          })),
        )
      }
    } catch (error) {
      if (mountedRef.current) {
        handleError(error)
      }
    } finally {
      mutationInProgress.current = false

      if (mountedRef.current) {
        setBusyPackId(null)
      }
    }
  }

  const deactivate = async () => {
    if (mutationInProgress.current || !activePack) {
      return
    }

    mutationInProgress.current = true
    setBusyPackId(activePack.id)

    try {
      await deactivatePersonalization(activePack.id)

      if (mountedRef.current) {
        setPacks((current) => current.map((pack) => ({ ...pack, active: false })))
      }
    } catch (error) {
      if (mountedRef.current) {
        handleError(error)
      }
    } finally {
      mutationInProgress.current = false

      if (mountedRef.current) {
        setBusyPackId(null)
      }
    }
  }

  return {
    packs,
    activePack,
    loading,
    busyPackId,
    refresh,
    activate,
    deactivate,
  }
}
