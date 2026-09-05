// @vitest-environment jsdom

import { act, cleanup, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import type { CurrentContextResponse, UserResponse } from "@/shared/api/backendContracts"
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
    updateCurrentLanguage: vi.fn(),
  }
})

vi.mock("react-i18next", async (importOriginal) => {
  const actual = await importOriginal<typeof import("react-i18next")>()

  return {
    ...actual,
    useTranslation: () => ({
      i18n: mocks.i18n,
      t: (key: string) => key,
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
  LoadingPage: () => <div data-testid="loading-page" />,
}))

vi.mock("@/ui/system/BanScreen", () => ({
  BanScreen: () => <div data-testid="ban-screen" />,
}))

vi.mock("@/ui/system/ServiceUnavailableScreen", () => ({
  ServiceUnavailableScreen: () => <div data-testid="service-unavailable" />,
}))

const user: UserResponse = {
  id: "user-1",
  username: "tester",
  localizationLanguage: "UK",
  activeAccountId: null,
  createdAt: "2026-09-05T00:00:00Z",
  updatedAt: "2026-09-05T00:00:00Z",
}

const context: CurrentContextResponse = {
  user,
  systemRoles: [],
  availableAccounts: [],
  activeAccount: null,
  effectivePermissions: [],
}

function LanguageProbe() {
  const app = useApp()

  return (
    <button type="button" onClick={() => void app.setLanguage("EN")}>
      {app.context?.user.localizationLanguage}
    </button>
  )
}

beforeEach(() => {
  mocks.changeLanguage.mockReset().mockResolvedValue(undefined)
  mocks.getCurrentContext.mockReset().mockResolvedValue(context)
  mocks.getToken.mockReset().mockResolvedValue("token")
  mocks.initializeCurrentUser.mockReset().mockResolvedValue(user)
  mocks.updateCurrentLanguage.mockReset().mockResolvedValue({
    ...user,
    localizationLanguage: "EN",
    updatedAt: "2026-09-05T00:01:00Z",
  })

  vi.spyOn(window, "requestAnimationFrame").mockImplementation((callback) => {
    callback(0)
    return 1
  })
})

afterEach(() => {
  cleanup()
  vi.restoreAllMocks()
})

describe("AppProvider language switching", () => {
  it("updates the current context in place without reloading the application context", async () => {
    render(
      <AppProvider>
        <LanguageProbe />
      </AppProvider>,
    )

    await waitFor(() => {
      expect(screen.getByRole("button").textContent).toBe("UK")
    })

    expect(mocks.getCurrentContext).toHaveBeenCalledOnce()

    await act(async () => {
      screen.getByRole("button").click()
    })

    await waitFor(() => {
      expect(screen.getByRole("button").textContent).toBe("EN")
    })

    expect(mocks.updateCurrentLanguage).toHaveBeenCalledWith("EN")
    expect(mocks.changeLanguage).toHaveBeenLastCalledWith("en")
    expect(mocks.getCurrentContext).toHaveBeenCalledOnce()
  })
})
