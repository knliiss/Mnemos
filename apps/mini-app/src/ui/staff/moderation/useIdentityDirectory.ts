import { useEffect, useMemo, useState } from "react"
import {
  searchAccountDirectory,
  searchUserDirectory,
  type AccountIdentityDirectoryResponse,
  type IdentityDirectoryOrder,
  type IdentityDirectoryPage,
  type UserIdentityDirectoryResponse,
} from "@/shared/api/moderationApi"
import { handleError } from "@/shared/errors/errorHandler"

export type IdentityDirectoryMode = "USERS" | "ACCOUNTS"

export function useIdentityDirectory() {
  const [mode, setModeState] = useState<IdentityDirectoryMode>("USERS")
  const [query, setQueryState] = useState("")
  const [order, setOrderState] = useState<IdentityDirectoryOrder>("NEWEST")
  const [page, setPage] = useState(0)
  const [userPage, setUserPage] = useState<IdentityDirectoryPage<UserIdentityDirectoryResponse> | null>(
    null,
  )
  const [accountPage, setAccountPage] = useState<IdentityDirectoryPage<AccountIdentityDirectoryResponse> | null>(
    null,
  )
  const [loadedKey, setLoadedKey] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(false)
  const normalizedQuery = query.trim()
  const requestKey = `${mode}:${order}:${page}:${normalizedQuery}`

  useEffect(() => {
    let cancelled = false
    const timer = window.setTimeout(() => {
      setLoading(true)
      setError(false)

      const request = {
        query: normalizedQuery,
        page,
        order,
      }
      const promise =
        mode === "USERS" ? searchUserDirectory(request) : searchAccountDirectory(request)

      void promise
        .then((response) => {
          if (cancelled) {
            return
          }

          if (mode === "USERS") {
            setUserPage(response as IdentityDirectoryPage<UserIdentityDirectoryResponse>)
          } else {
            setAccountPage(response as IdentityDirectoryPage<AccountIdentityDirectoryResponse>)
          }

          setLoadedKey(requestKey)
        })
        .catch((requestError) => {
          if (cancelled) {
            return
          }

          setLoadedKey(requestKey)
          setError(true)
          handleError(requestError)
        })
        .finally(() => {
          if (!cancelled) {
            setLoading(false)
          }
        })
    }, normalizedQuery ? 220 : 0)

    return () => {
      cancelled = true
      window.clearTimeout(timer)
    }
  }, [mode, normalizedQuery, order, page, requestKey])

  const current = loadedKey === requestKey
  const visibleUserPage = mode === "USERS" && current ? userPage : null
  const visibleAccountPage = mode === "ACCOUNTS" && current ? accountPage : null
  const visiblePage = visibleUserPage ?? visibleAccountPage
  const totalPages = visiblePage?.totalPages ?? 0
  const totalElements = visiblePage?.totalElements ?? 0

  const canGoPrevious = page > 0
  const canGoNext = totalPages > 0 && page + 1 < totalPages

  const setMode = (nextMode: IdentityDirectoryMode) => {
    setModeState(nextMode)
    setPage(0)
  }

  const setQuery = (nextQuery: string) => {
    setQueryState(nextQuery)
    setPage(0)
  }

  const setOrder = (nextOrder: IdentityDirectoryOrder) => {
    setOrderState(nextOrder)
    setPage(0)
  }

  const pagination = useMemo(
    () => ({
      page,
      totalPages,
      totalElements,
      canGoPrevious,
      canGoNext,
    }),
    [canGoNext, canGoPrevious, page, totalElements, totalPages],
  )

  return {
    mode,
    query,
    normalizedQuery,
    order,
    userPage: visibleUserPage,
    accountPage: visibleAccountPage,
    loading: loading || !current,
    error: current && error,
    pagination,
    setMode,
    setQuery,
    setOrder,
    previousPage: () => setPage((currentPage) => Math.max(0, currentPage - 1)),
    nextPage: () => {
      if (canGoNext) {
        setPage((currentPage) => currentPage + 1)
      }
    },
  }
}

export type IdentityDirectoryController = ReturnType<typeof useIdentityDirectory>
