import type { RootRoute, Route, RouteId } from "../model"

export type FeatureArea = "events" | "account" | "people" | "personalization" | "creator" | "staff"

type RouteDefinition = {
  area: FeatureArea
  root?: true
}

export const routeRegistry: Record<RouteId, RouteDefinition> = {
  bets: { area: "events", root: true },
  events: { area: "events", root: true },
  "event-detail": { area: "events" },
  subscriptions: { area: "events" },

  account: { area: "account", root: true },
  member: { area: "account" },
  roles: { area: "account" },
  "role-editor": { area: "account" },
  "account-wallet": { area: "account" },
  "account-invites": { area: "account" },
  "account-settings": { area: "account" },
  "account-create": { area: "account" },
  "account-join": { area: "account" },

  search: { area: "people", root: true },
  "public-profile": { area: "people" },
  "user-wallet": { area: "people" },

  "personalization-store": { area: "personalization", root: true },
  "personalization-store-category": { area: "personalization" },
  "personalization-owned": { area: "personalization" },
  "personalization-detail": { area: "personalization" },

  "creator-studio": { area: "creator" },
  "creator-revenue": { area: "creator" },
  "personalization-editor": { area: "creator" },

  moderation: { area: "staff" },
  administration: { area: "staff" },
  "moderation-user": { area: "staff" },
  "personalization-review": { area: "staff" },
  "bet-admin": { area: "staff" },
  "badge-admin": { area: "staff" },
}

const areaRoots: Record<FeatureArea, RootRoute> = {
  events: "events",
  account: "account",
  people: "search",
  personalization: "personalization-store",
  creator: "account",
  staff: "account",
}

export function isRootRoute(id: RouteId): id is RootRoute {
  return routeRegistry[id].root === true
}

export function featureArea(id: RouteId): FeatureArea {
  return routeRegistry[id].area
}

export function initialStack(): Route[] {
  const params = new URLSearchParams(window.location.search)
  const requestedRaw = params.get("screen")

  if (!requestedRaw) {
    return [{ id: "account" }]
  }

  if (!(requestedRaw in routeRegistry)) {
    return [{
      id: "account",
      params: {
        notFound: "true",
        requested: requestedRaw,
      },
    }]
  }

  const requested = requestedRaw as RouteId
  const routeParams = Object.fromEntries(
    [...params.entries()]
      .filter(([key]) => key.startsWith("p."))
      .map(([key, value]) => [key.slice(2), value]),
  )

  const route: Route = {
    id: requested,
    params: Object.keys(routeParams).length ? routeParams : undefined,
  }

  if (isRootRoute(requested)) {
    return [route]
  }

  return [
    { id: areaRoots[routeRegistry[requested].area] },
    route,
  ]
}
