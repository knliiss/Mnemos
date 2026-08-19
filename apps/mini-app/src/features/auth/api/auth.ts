import Keycloak from "keycloak-js"

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL ?? "https://auth.knalis.rest",
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? "mnemos",
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? "mnemos-mini-app",
})

let initializationPromise: Promise<void> | null = null

export function initializeAuth(): Promise<void> {
  if (!initializationPromise) {
    initializationPromise = initialize()
  }

  return initializationPromise
}

async function initialize(): Promise<void> {
  const telegram = window.Telegram?.WebApp

  if (!telegram?.initData) {
    throw new Error("TELEGRAM_REQUIRED")
  }

  telegram.ready()
  telegram.expand()
  telegram.setHeaderColor("#070a0d")
  telegram.setBackgroundColor("#070a0d")

  const authenticated = await keycloak.init({
    checkLoginIframe: false,
    pkceMethod: "S256",
    flow: "standard",
  })

  if (authenticated) {
    if (!keycloak.token) {
      throw new Error("TOKEN_MISSING")
    }

    return
  }

  const loginUrl = await keycloak.createLoginUrl({
    redirectUri: window.location.origin,
  })

  const url = new URL(loginUrl)
  url.searchParams.set("telegram_init_data", telegram.initData)

  window.location.replace(url)
}

export async function getToken(): Promise<string> {
  await initializeAuth()

  if (!keycloak.token) {
    throw new Error("TOKEN_MISSING")
  }

  if (!keycloak.isTokenExpired(30)) {
    return keycloak.token
  }

  if (!keycloak.refreshToken) {
    throw new Error("REFRESH_TOKEN_MISSING")
  }

  await keycloak.updateToken(30)

  if (!keycloak.token) {
    throw new Error("TOKEN_MISSING")
  }

  return keycloak.token
}
