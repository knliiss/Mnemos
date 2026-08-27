import { useCallback, useEffect, useRef, useState } from "react"
import {
  getSaoRealmSnapshot,
  type SaoRealmSnapshotResponse,
} from "@/shared/api/playerDirectoryApi"

const REFRESH_INTERVAL_MS = 15_000

export function useSaoRealmDirectory() {
  const [snapshot, setSnapshot] = useState<SaoRealmSnapshotResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState(false)
  const requestInFlight = useRef(false)

  const load = useCallback(async (background = false) => {
    if (requestInFlight.current) {
      return
    }

    requestInFlight.current = true
    setError(false)

    if (background) {
      setRefreshing(true)
    } else {
      setLoading(true)
    }

    try {
      setSnapshot(await getSaoRealmSnapshot())
    } catch {
      setError(true)
    } finally {
      requestInFlight.current = false
      setLoading(false)
      setRefreshing(false)
    }
  }, [])

  useEffect(() => {
    void load()

    const interval = window.setInterval(() => {
      if (document.visibilityState === "visible") {
        void load(true)
      }
    }, REFRESH_INTERVAL_MS)

    return () => window.clearInterval(interval)
  }, [load])

  return {
    snapshot,
    loading,
    refreshing,
    error,
    retry: () => void load(false),
    refresh: () => void load(true),
  }
}
