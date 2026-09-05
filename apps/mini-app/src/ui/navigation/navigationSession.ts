import type { Route } from "../model"
import { initialStack, isRouteId } from "./routeRegistry"

const NAVIGATION_STACK_STORAGE_KEY = "mnemos.navigation.stack.v1"

export function restoreNavigationStack(): Route[] {
  const urlStack = initialStack()
  const searchParams = new URLSearchParams(window.location.search)

  if (searchParams.has("screen")) {
    return urlStack
  }

  try {
    const stored = window.sessionStorage.getItem(NAVIGATION_STACK_STORAGE_KEY)

    if (!stored) {
      return urlStack
    }

    const parsed: unknown = JSON.parse(stored)

    return isNavigationStack(parsed) ? parsed : urlStack
  } catch {
    return urlStack
  }
}

export function persistNavigationStack(stack: Route[]): void {
  try {
    window.sessionStorage.setItem(NAVIGATION_STACK_STORAGE_KEY, JSON.stringify(stack))
  } catch {
    // Navigation still works when browser storage is unavailable.
  }
}

function isNavigationStack(value: unknown): value is Route[] {
  return Array.isArray(value) && value.length > 0 && value.every(isStoredRoute)
}

function isStoredRoute(value: unknown): value is Route {
  if (!isRecord(value) || !isRouteId(value.id)) {
    return false
  }

  if (value.params === undefined) {
    return true
  }

  if (!isRecord(value.params)) {
    return false
  }

  return Object.values(value.params).every((param) => typeof param === "string")
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value)
}
