import { useCallback, useEffect, useRef, useState } from "react"
import { searchPublicProfiles, type PublicProfileResponse } from "@/shared/api/profileSocialApi"
import { handleError } from "@/shared/errors/errorHandler"

const SEARCH_DEBOUNCE_MS = 260
const MIN_SEARCH_QUERY_LENGTH = 2

export function useProfileSearch() {
  const [query, setQuery] = useState("")
  const [results, setResults] = useState<PublicProfileResponse[]>([])
  const [searching, setSearching] = useState(false)
  const [errorQuery, setErrorQuery] = useState<string | null>(null)
  const searchRevision = useRef(0)
  const normalizedQuery = query.trim()

  const runSearch = useCallback(async (value: string) => {
    const revision = searchRevision.current + 1

    searchRevision.current = revision
    setSearching(true)
    setErrorQuery(null)

    try {
      const page = await searchPublicProfiles(value)

      if (revision !== searchRevision.current) {
        return
      }

      setResults(page.content)
    } catch (error) {
      if (revision === searchRevision.current) {
        setResults([])
        setErrorQuery(value)
        handleError(error)
      }
    } finally {
      if (revision === searchRevision.current) {
        setSearching(false)
      }
    }
  }, [])

  useEffect(() => {
    if (normalizedQuery.length < MIN_SEARCH_QUERY_LENGTH) {
      return
    }

    const timeout = window.setTimeout(() => {
      void runSearch(normalizedQuery)
    }, SEARCH_DEBOUNCE_MS)

    return () => window.clearTimeout(timeout)
  }, [normalizedQuery, runSearch])

  const changeQuery = (value: string) => {
    const nextNormalizedQuery = value.trim()
    const normalizedQueryChanged = nextNormalizedQuery !== normalizedQuery

    setQuery(value)

    if (!normalizedQueryChanged) {
      return
    }

    searchRevision.current += 1
    setErrorQuery(null)
    setResults([])
    setSearching(nextNormalizedQuery.length >= MIN_SEARCH_QUERY_LENGTH)
  }

  const retry = () => {
    if (normalizedQuery.length >= MIN_SEARCH_QUERY_LENGTH) {
      void runSearch(normalizedQuery)
    }
  }

  return {
    query,
    normalizedQuery,
    results,
    searching,
    searchFailed: errorQuery === normalizedQuery,
    changeQuery,
    retry,
  }
}
