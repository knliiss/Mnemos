import { useCallback, useEffect, useState } from "react"
import {
  createLeaderboardExclusion,
  deleteLeaderboardExclusion,
  listLeaderboardExclusions,
  type LeaderboardExclusionResponse,
  type LeaderboardExclusionScope,
  type LeaderboardExclusionSubjectType,
} from "@/shared/api/leaderboardExclusionApi"
import { handleError } from "@/shared/errors/errorHandler"
import { resolveLeaderboardExclusionTarget } from "./leaderboardExclusionTarget"

export type CreateLeaderboardExclusionForm = {
  subjectType: LeaderboardExclusionSubjectType
  target: string
  scope: LeaderboardExclusionScope
  reason: string
}

export function useLeaderboardExclusions() {
  const [items, setItems] = useState<LeaderboardExclusionResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [targetNotFound, setTargetNotFound] = useState(false)

  const refresh = useCallback(async () => {
    setLoading(true)

    try {
      setItems(await listLeaderboardExclusions())
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    let active = true

    void listLeaderboardExclusions()
      .then((exclusions) => {
        if (active) {
          setItems(exclusions)
        }
      })
      .catch((error) => {
        if (active) {
          handleError(error)
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [])

  const create = useCallback(
    async (form: CreateLeaderboardExclusionForm): Promise<boolean> => {
      if (busy) {
        return false
      }

      setBusy(true)
      setTargetNotFound(false)

      try {
        const target = await resolveLeaderboardExclusionTarget(form.subjectType, form.target)

        if (!target) {
          setTargetNotFound(true)
          return false
        }

        const created = await createLeaderboardExclusion({
          subjectType: form.subjectType,
          subjectId: target.id,
          scope: form.scope,
          reason: form.reason.trim() || null,
        })

        setItems((current) => [created, ...current.filter((item) => item.id !== created.id)])
        return true
      } catch (error) {
        handleError(error)
        return false
      } finally {
        setBusy(false)
      }
    },
    [busy],
  )

  const remove = useCallback(
    async (id: string) => {
      if (busy) {
        return
      }

      setBusy(true)

      try {
        await deleteLeaderboardExclusion(id)
        setItems((current) => current.filter((item) => item.id !== id))
      } catch (error) {
        handleError(error)
      } finally {
        setBusy(false)
      }
    },
    [busy],
  )

  return {
    items,
    loading,
    busy,
    targetNotFound,
    create,
    remove,
    refresh,
  }
}
