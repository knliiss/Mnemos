import type { CollectorCredentialResponse } from "@/shared/api/collectorAdminApi"

export type CollectorCredentialState = "ENABLED" | "DISABLED" | "REVOKED"
export type CollectorFilter = "ALL" | CollectorCredentialState
export type CollectorSort = "STATUS" | "RECENT" | "NAME"

export function collectorCredentialState(
  credential: CollectorCredentialResponse,
): CollectorCredentialState {
  if (credential.revokedAt) {
    return "REVOKED"
  }

  return credential.enabled ? "ENABLED" : "DISABLED"
}

export function filterAndSortCollectors(
  credentials: CollectorCredentialResponse[],
  filter: CollectorFilter,
  sort: CollectorSort,
  query: string,
): CollectorCredentialResponse[] {
  const normalizedQuery = query.trim().toLocaleLowerCase()
  const filtered = credentials.filter((credential) => {
    const stateMatches = filter === "ALL" || collectorCredentialState(credential) === filter
    const queryMatches =
      !normalizedQuery ||
      credential.name.toLocaleLowerCase().includes(normalizedQuery) ||
      credential.id.toLocaleLowerCase().includes(normalizedQuery)

    return stateMatches && queryMatches
  })

  return filtered.toSorted((left, right) => compareCollectors(left, right, sort))
}

function compareCollectors(
  left: CollectorCredentialResponse,
  right: CollectorCredentialResponse,
  sort: CollectorSort,
): number {
  if (sort === "NAME") {
    return left.name.localeCompare(right.name)
  }

  if (sort === "RECENT") {
    return Date.parse(right.createdAt) - Date.parse(left.createdAt)
  }

  const stateDifference = stateRank(left) - stateRank(right)

  if (stateDifference !== 0) {
    return stateDifference
  }

  return Date.parse(right.updatedAt) - Date.parse(left.updatedAt)
}

function stateRank(credential: CollectorCredentialResponse): number {
  const state = collectorCredentialState(credential)

  if (state === "ENABLED") {
    return 0
  }

  if (state === "DISABLED") {
    return 1
  }

  return 2
}
