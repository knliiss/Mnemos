export const runtimeConfig = {
    keycloakUrl: import.meta.env.VITE_KEYCLOAK_URL || "https://auth.knalis.rest",
    realm: import.meta.env.VITE_KEYCLOAK_REALM || "mnemos",
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || "mnemos-mini-app",
    apiUrl: import.meta.env.VITE_API_URL || "https://api.knalis.rest",
    skinUrl: import.meta.env.VITE_SKIN_URL || "https://webdata.c7x.dev/textures/skin/46d7a80e-74f5-11eb-acca-1cb72caa35fd"
}