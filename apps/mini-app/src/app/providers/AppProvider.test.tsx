// @vitest-environment jsdom

import { act, cleanup, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import type { CurrentContextResponse, UserResponse } from "@/shared/api/backendContracts"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import { AppProvider, useApp } from "./AppProvider"

const mocks = vi.hoisted(() => {
  const changeLanguage = vi.fn()

  return {
    changeLanguage,
    getCurrentContext: vi.fn(),
    getToken: vi.fn(),
    i18n: {
      changeLanguage,
    },
    initializeCurrentUser: vi.fn(),
    translate: (key: string) => key,
    updateCurrentLanguage: vi.fn(),
  }
})

vi.mock("react-i18next", async (importOriginal) => {
  const actual = await importOriginal<typeof import("react-i18next")>()

  return {
    ...actual,
    useTranslation: () => ({
      i18n: mocks.i18n,
      t: mocks.translate,
    }),
  }
})

vi.mock("@/features/auth/api/auth", () => ({
  getToken: mocks.getToken,
}))

vi.mock("@/shared/api/userApi", () => ({
  getCurrentContext: mocks.getCurrentContext,
  initializeCurrentUser: mocks.initializeCurrentUser,
  updateCurrentLanguage: mocks.updateCurrentLanguage,
}))

vi.mock("@/pages/loading/LoadingPage", () => ({
  LoadingPage: ({ label }: { label: string }) => <div data-testid="loading-page">{label}</div>,
}))

vi.mock("@/ui/system/BanScreen", () => ({
  BanScreen: ({ ban }: { ban: { reason: string; expiresAt: string | null } }) => (
    <div data-testid="ban-screen">
      <span data-testid="ban-reason">{ban.reason}</span>
      <span data-testid="ban-expiry">{ban.expiresAt ?? "permanent"}</span>
    </div>
  ),
}))

vi.mock("@/ui/system/ServiceUnavailableScreen", () => ({
  ServiceUnavailableScreen: () => <div data-testid="service-unavailable" />,
}))

const currentUser: UserResponse = {
  id: "user-1",
  username: "tester",
  localizationLanguage: "RU",
  activeAccountId: null,
  createdAt: "2026-08-29T00:00:00Z",
  updatedAt: "2026-08-29T00:00:00Z",
}

const currentContext: CurrentContextResponse = {
  user: currentUser,
  systemRoles: [],
  availableAccounts: [],
  activeAccount: null,
  effectivePermissions: [],
}

function ContextProbe() {
  const app = useApp()

  return <div data-testid="context-username">{app.context?.user.username}</div>
}

function createDeferred<T>() {
  let resolve!: (value: T) => void
  const promise = new Promise<T>((resolver) => {
    resolve = resolver
  })

  return { promise, resolve }
}

beforeEach(() => {
  mocks.changeLanguage.mockReset().mockResolvedValue(undefined)
  mocks.getCurrentContext.mockReset()
  mocks.getToken.mockReset().mockResolvedValue("access-token")
  mocks.initializeCurrentUser.mockReset().mockResolvedValue(currentUser)
  mocks.updateCurrentLanguage.mockReset().mockResolvedValue(currentUser)

  vi.spyOn(window, "requestAnimationFrame").mockImplementation((callback) => {
    callback(0)
    return 1
  })
})

afterEach(() => {
  cleanup()
  vi.restoreAllMocks()
})

describe("AppProvider bootstrap", () => {
  it("keeps product UI unmounted until authentication context is ready", async () => {
    const contextRequest = createDeferred<CurrentContextResponse>()
    mocks.getCurrentContext.mockReturnValue(contextRequest.promise)

    render(
      <AppProvider>
        <div data-testid="product-ui">Product UI</div>
      </AppProvider>,
    )

    await waitFor(() => {
      expect(mocks.getCurrentContext).toHaveBeenCalledOnce()
    })

    expect(screen.queryByTestId("product-ui")).toBeNull()
    expect(screen.getByTestId("loading-page").textContent).toBe("common.loading")

    await act(async () => {
      contextRequest.resolve(currentContext)
    })

    await waitFor(() => {
      expect(screen.queryByTestId("product-ui")?.textContent).toBe("Product UI")
    })

    expect(screen.queryByTestId("loading-page")).toBeNull()
    expect(mocks.getToken).toHaveBeenCalledWith({ forceRefresh: true })
    expect(mocks.changeLanguage).toHaveBeenCalledWith("ru")
  })

  it("refreshes global account context after an account lifecycle event", async () => {
    const refreshedContext: CurrentContextResponse = {
      ...currentContext,
      user: {
        ...currentUser,
        username: "refreshed-user",
      },
    }

    mocks.getCurrentContext
      .mockResolvedValueOnce(currentContext)
      .mockResolvedValueOnce(refreshedContext)

    render(
      <AppProvider>
        <ContextProbe />
      </AppProvider>,
    )

    await waitFor(() => {
      expect(screen.getByTestId("context-username").textContent).toBe("tester")
    })

    act(() => {
      realtimeEvents.publish({
        id: "019c1129-ef54-7000-8000-000000000411",
        type: "account.deleted",
        occurredAt: "2026-09-03T00:00:00Z",
        operationId: null,
        payload: {
          accountId: "019c1129-ef54-7000-8000-000000000412",
          status: "DELETED",
        },
      })
    })

    await waitFor(() => {
      expect(mocks.getCurrentContext).toHaveBeenCalledTimes(2)
      expect(screen.getByTestId("context-username").textContent).toBe("refreshed-user")
    })
  })

  it("shows ban screen immediately after realtime ban activation", async () => {
    mocks.getCurrentContext.mockResolvedValue(currentContext)

    render(
      <AppProvider>
        <ContextProbe />
      </AppProvider>,
    )

    await waitFor(() => {
      expect(screen.getByTestId("context-username").textContent).toBe("tester")
    })

    act(() => {
      realtimeEvents.publish({
        id: "019c1129-ef54-7000-8000-000000000421",
        type: "moderation.sanction.activated",
        occurredAt: "2026-09-03T01:00:00Z",
        operationId: null,
        payload: {
          sanctionId: "019c1129-ef54-7000-8000-000000000422",
          userId: "019c1129-ef54-7000-8000-000000000423",
          type: "BAN",
          status: "ACTIVE",
          reason: "Repeated abuse",
          expiresAt: "2026-09-04T01:00:00Z",
        },
      })
    })

    await waitFor(() => {
      expect(screen.getByTestId("ban-screen")).toBeTruthy()
      expect(screen.getByTestId("ban-reason").textContent).toBe("Repeated abuse")
      expect(screen.getByTestId("ban-expiry").textContent).toBe("2026-09-04T01:00:00Z")
    })

    expect(screen.queryByTestId("context-username")).toBeNull()
  })
})
