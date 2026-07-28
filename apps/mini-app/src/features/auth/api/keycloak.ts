import Keycloak from "keycloak-js"
import {runtimeConfig} from "@/shared/config/runtimeConfig"

export const keycloak = new Keycloak({url: runtimeConfig.keycloakUrl, realm: runtimeConfig.realm, clientId: runtimeConfig.clientId})
