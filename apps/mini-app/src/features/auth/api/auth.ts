import Keycloak from "keycloak-js"
import { AuthError } from "@/features/auth/api/authErrors"
import { getTelegramInitDataState } from "@/features/auth/api/telegramInitData"
import {
  clearTelegramKeycloakSession,
  loadTelegramKeycloakSession,
  persistTelegramKeycloakSession,
  type StoredTelegramKeycloakSession,
} from "@/features/auth/api/telegramKeycloakSessionStorage"
import { createTelegramLoginRedirectUrl } from "@/features/auth/api/telegramLoginTransport"
import { resolveTelegramSessionUserId } from "@/features/auth/api/telegramSessionBinding"

const keycloakUrl = (import.meta.env.VITE_KEYCLOAK_URL ?? "https://auth.knalis.rest").replace(
  /\/+$/,
  "",
)
const keycloakRealm = import.meta.env.VITE_KEYCLOAK_REALM ?? "mnemos"
const keycloakClientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? "mnemos-mini-app"

const AUTH_AVAILABILITY_TIMEOUT_MS = 4_000
const TOKEN_MINIMUM_VALIDITY_SECONDS = 30
const SESSION_REFRESH_MINIMUM_VALIDITY_SECONDS = 120
const SESSION_MAINTENANCE_INTERVAL_MS = 60_000
const OFFLINE_ACCESS_SCOPE = "offline_access"

type KeycloakClient = InstanceType<typeof Keycloak>
export type AuthRuntimeState = "ready" | "unavailable" | "session-expired"
export type GetTokenOptions = {
  forceRefresh?: boolean
}

type AuthRuntimeListener = (state: AuthRuntimeState) => void

let keycloak: KeycloakClient | null = null
let initializationPromise: Promise<void> | null = null
let sessionMaintenancePromise: Promise<void> | null = null
let sessionMaintenanceTimer: number | null = null
let lifecycleListenersInstalled = false

const authRuntimeListeners = new Set<AuthRuntimeListener>()

export function initializeAuth(): Promise<void> {
  if (keycloak?.authenticated && keycloak.token) {
    return Promise.resolve()
  }

  if (initializationPromise) {
    return initializationPromise
  }

  const attempt = initialize()
  initializationPromise = attempt

  void attempt.then(
    () => {
      if (initializationPromise === attempt) {
        initializationPromise = null
      }
    },
    () => {
      if (initializationPromise === attempt) {
        initializationPromise = null
      }
    },
  )

  return attempt
}

export async function retryAuth(): Promise<void> {
  const current = keycloak

  if (current?.refreshToken) {
    await ensureAuthServiceAvailable()

    try {
      await current.updateToken(-1)

      if (current.token) {
        await persistCurrentKeycloakSession(current)
        publishAuthRuntimeState("ready")
        startSessionMaintenance(current)
        return
      }
    } catch (error) {
      if (!(await isAuthServiceAvailable())) {
        throwAuthUnavailable(error)
      }
    }

    if (keycloak === current) {
      keycloak = null
    }
  }

  await initializeAuth()
}

export async function getToken(options: GetTokenOptions = {}): Promise<string> {
  await initializeAuth()

  const current = keycloak

  if (!current?.token) {
    throw new AuthError("TOKEN_MISSING")
  }

  if (!options.forceRefresh && !current.isTokenExpired(TOKEN_MINIMUM_VALIDITY_SECONDS)) {
    return current.token
  }

  return refreshAccessToken(current, options.forceRefresh === true)
}

export function subscribeAuthRuntimeState(listener: AuthRuntimeListener): () => void {
  authRuntimeListeners.add(listener)

  return () => {
    authRuntimeListeners.delete(listener)
  }
}

async function initialize(): Promise<void> {
  const telegram = window.Telegram?.WebApp

  if (!telegram) {
    throw new AuthError("TELEGRAM_REQUIRED")
  }

  configureTelegram(telegram)

  const initDataUserId = await resolveTelegramSessionUserId(telegram)
  const storedSession = await loadTelegramKeycloakSession(telegram)
  const telegramUserId = initDataUserId ?? storedSession?.telegramUserId ?? null
  const resumableSession =
    storedSession?.telegramUserId === telegramUserId ? storedSession : null

  if (storedSession && !resumableSession) {
    await clearTelegramKeycloakSession(telegram)
  }

  await ensureAuthServiceAvailable()

  const candidate = createKeycloakClient()
  keycloak = candidate

  let authenticated: boolean

  try {
    authenticated = await candidate.init({
      onLoad: "check-sso",
      silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
      checkLoginIframe: false,
      pkceMethod: "S256",
      flow: "standard",
      scope: OFFLINE_ACCESS_SCOPE,
      messageReceiveTimeout: AUTH_AVAILABILITY_TIMEOUT_MS,
      token: resumableSession?.token,
      refreshToken: resumableSession?.refreshToken,
    })
  } catch (error) {
    if (keycloak === candidate) {
      keycloak = null
    }

    if (!(await isAuthServiceAvailable())) {
      throwAuthUnavailable(error)
    }

    throw new AuthError("AUTH_FAILED", error)
  }

  if (authenticated) {
    if (!candidate.token) {
      if (keycloak === candidate) {
        keycloak = null
      }

      throw new AuthError("TOKEN_MISSING")
    }

    if (telegramUserId !== null && hasMatchingTelegramIdentity(candidate, telegramUserId)) {
      await persistCurrentKeycloakSession(candidate)
      publishAuthRuntimeState("ready")
      startSessionMaintenance(candidate)
      return
    }

    await clearTelegramKeycloakSession(telegram)
  }

  requireFreshTelegramAuthentication(telegram, telegramUserId, candidate)

  await ensureAuthServiceAvailable()
  await redirectToTelegramLogin(candidate, telegram.initData)

  return new Promise<void>(() => undefined)
}

function createKeycloakClient(): KeycloakClient {
  const candidate = new Keycloak({
    url: keycloakUrl,
    realm: keycloakRealm,
    clientId: keycloakClientId,
  })

  candidate.onTokenExpired = () => {
    if (keycloak === candidate) {
      void maintainSession()
    }
  }

  return candidate
}

function requireFreshTelegramAuthentication(
  telegram: TelegramWebApp,
  telegramUserId: number | null,
  current: KeycloakClient,
): void {
  if (getTelegramInitDataState(telegram.initData) === "fresh" && telegramUserId !== null) {
    return
  }

  if (keycloak === current) {
    keycloak = null
  }

  if (!isTelegramRuntime(telegram)) {
    throw new AuthError("TELEGRAM_REQUIRED")
  }

  publishAuthRuntimeState("session-expired")
  throw new AuthError("TELEGRAM_SESSION_EXPIRED")
}

function isTelegramRuntime(telegram: TelegramWebApp): boolean {
  return (
    typeof telegram.platform === "string" &&
    telegram.platform.length > 0 &&
    telegram.platform !== "unknown"
  )
}

async function redirectToTelegramLogin(current: KeycloakClient, initData: string): Promise<void> {
  const loginUrl = await current.createLoginUrl({
    redirectUri: window.location.origin,
    scope: OFFLINE_ACCESS_SCOPE,
    prompt: "login",
  })
  const redirectUrl = createTelegramLoginRedirectUrl(loginUrl, initData)

  window.location.replace(redirectUrl)
}

function hasMatchingTelegramIdentity(current: KeycloakClient, telegramUserId: number): boolean {
  const tokenTelegramUserId = getTokenTelegramUserId(current)

  return tokenTelegramUserId === telegramUserId
}

function getTokenTelegramUserId(current: KeycloakClient): number | null {
  const claim = current.tokenParsed?.telegram_id

  if (typeof claim === "number") {
    return Number.isSafeInteger(claim) && claim > 0 ? claim : null
  }

  if (typeof claim !== "string" || !/^\d+$/.test(claim)) {
    return null
  }

  const parsed = Number(claim)

  return Number.isSafeInteger(parsed) && parsed > 0 ? parsed : null
}

async function persistCurrentKeycloakSession(current: KeycloakClient): Promise<void> {
  const telegram = window.Telegram?.WebApp
  const telegramUserId = getTokenTelegramUserId(current)

  if (!telegram || telegramUserId === null || !current.token || !current.refreshToken) {
    return
  }

  const session: StoredTelegramKeycloakSession = {
    telegramUserId,
    token: current.token,
    refreshToken: current.refreshToken,
  }

  await persistTelegramKeycloakSession(telegram, session)
}

async function refreshAccessToken(current: KeycloakClient, forceRefresh: boolean): Promise<string> {
  try {
    const refreshed = await current.updateToken(forceRefresh ? -1 : TOKEN_MINIMUM_VALIDITY_SECONDS)

    if (refreshed) {
      await persistCurrentKeycloakSession(current)
    }
  } catch (error) {
    if (!forceRefresh && current.token && !current.isTokenExpired(0)) {
      return current.token
    }

    return recoverAuthentication(current, error)
  }

  if (!current.token) {
    return recoverAuthentication(current, new AuthError("TOKEN_MISSING"))
  }

  publishAuthRuntimeState("ready")
  return current.token
}

async function recoverAuthentication(
  failedClient: KeycloakClient,
  source: unknown,
): Promise<string> {
  if (!(await isAuthServiceAvailable())) {
    throwAuthUnavailable(source)
  }

  if (keycloak === failedClient) {
    keycloak = null
  }

  try {
    await initializeAuth()
  } catch (error) {
    if (error instanceof AuthError) {
      throw error
    }

    throw new AuthError("AUTH_FAILED", error)
  }

  if (!keycloak?.token) {
    throw new AuthError("TOKEN_MISSING")
  }

  publishAuthRuntimeState("ready")
  return keycloak.token
}

function startSessionMaintenance(current: KeycloakClient): void {
  if (!lifecycleListenersInstalled) {
    document.addEventListener("visibilitychange", handleVisibilityChange)
    window.addEventListener("focus", handleWindowFocus)
    lifecycleListenersInstalled = true
  }

  if (sessionMaintenanceTimer === null) {
    sessionMaintenanceTimer = window.setInterval(() => {
      void maintainSession()
    }, SESSION_MAINTENANCE_INTERVAL_MS)
  }

  if (keycloak === current) {
    void maintainSession()
  }
}

function handleVisibilityChange(): void {
  if (document.visibilityState === "visible") {
    void maintainSession()
  }
}

function handleWindowFocus(): void {
  void maintainSession()
}

function maintainSession(): Promise<void> {
  if (sessionMaintenancePromise) {
    return sessionMaintenancePromise
  }

  const attempt = refreshSessionInBackground()
  sessionMaintenancePromise = attempt

  void attempt.then(
    () => {
      if (sessionMaintenancePromise === attempt) {
        sessionMaintenancePromise = null
      }
    },
    () => {
      if (sessionMaintenancePromise === attempt) {
        sessionMaintenancePromise = null
      }
    },
  )

  return attempt
}

async function refreshSessionInBackground(): Promise<void> {
  const current = keycloak

  if (!current?.authenticated || !current.refreshToken || !current.token) {
    return
  }

  try {
    const refreshed = await current.updateToken(SESSION_REFRESH_MINIMUM_VALIDITY_SECONDS)

    if (refreshed) {
      await persistCurrentKeycloakSession(current)
    }

    publishAuthRuntimeState("ready")
  } catch (error) {
    if (current.token && !current.isTokenExpired(0)) {
      return
    }

    try {
      await recoverAuthentication(current, error)
    } catch (recoveryError) {
      if (recoveryError instanceof AuthError) {
        if (recoveryError.code === "AUTH_UNAVAILABLE") {
          publishAuthRuntimeState("unavailable")
        } else {
          publishAuthRuntimeState("session-expired")
        }

        return
      }

      publishAuthRuntimeState("session-expired")
    }
  }
}

function configureTelegram(telegram: TelegramWebApp): void {
  telegram.ready()
  telegram.expand()
  telegram.setHeaderColor("#070a0d")
  telegram.setBackgroundColor("#070a0d")
}

async function ensureAuthServiceAvailable(): Promise<void> {
  if (!(await isAuthServiceAvailable())) {
    throwAuthUnavailable()
  }
}

async function isAuthServiceAvailable(): Promise<boolean> {
  const controller = new AbortController()
  const timeout = window.setTimeout(() => {
    controller.abort()
  }, AUTH_AVAILABILITY_TIMEOUT_MS)

  try {
    const response = await fetch(
      `${keycloakUrl}/realms/${encodeURIComponent(keycloakRealm)}/.well-known/openid-configuration`,
      {
        method: "GET",
        headers: {
          Accept: "application/json",
        },
        credentials: "omit",
        cache: "no-store",
        signal: controller.signal,
      },
    )

    return response.ok
  } catch {
    return false
  } finally {
    window.clearTimeout(timeout)
  }
}

function throwAuthUnavailable(source?: unknown): never {
  publishAuthRuntimeState("unavailable")
  throw new AuthError("AUTH_UNAVAILABLE", source)
}

function publishAuthRuntimeState(state: AuthRuntimeState): void {
  for (const listener of authRuntimeListeners) {
    listener(state)
  }
}
