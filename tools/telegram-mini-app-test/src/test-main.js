import Keycloak from "keycloak-js"
import "./style.css"

const KEYCLOAK_URL = "https://auth.knalis.rest"
const REALM = "mnemos"
const CLIENT_ID = "mnemos-mini-app"
const API_URL = "https://api.knalis.rest"

const ui = {
    status: document.getElementById("status"),

    userCard: document.getElementById("user-card"),
    usernameSection: document.getElementById("username-section"),
    buttons: document.getElementById("buttons"),

    userId: document.getElementById("user-id"),
    username: document.getElementById("username"),
    telegramId: document.getElementById("telegram-id"),
    keycloakId: document.getElementById("keycloak-id"),
    createdAt: document.getElementById("created-at"),

    usernameInput: document.getElementById("username-input"),

    updateButton: document.getElementById("update-username-button"),
    reloadButton: document.getElementById("reload-button"),
    logoutButton: document.getElementById("logout-button"),

    error: document.getElementById("error")
}

const keycloak = new Keycloak({
    url: KEYCLOAK_URL,
    realm: REALM,
    clientId: CLIENT_ID
})

initializeTelegram()
initializeApplication()

function initializeTelegram() {

    const telegram = window.Telegram?.WebApp

    if (!telegram) {
        return
    }

    telegram.ready()
    telegram.expand()

    telegram.setHeaderColor("#09090b")
    telegram.setBackgroundColor("#09090b")
}

async function initializeApplication() {

    try {

        hideError()

        setStatus("Авторизація...")

        const authenticated = await keycloak.init({
            checkLoginIframe: false,
            pkceMethod: "S256"
        })

        if (!authenticated) {

            const initData = window.Telegram?.WebApp?.initData

            if (!initData) {
                throw new Error("Telegram initData is missing")
            }

            const loginUrl = await keycloak.createLoginUrl({
                redirectUri: window.location.origin
            })

            const url = new URL(loginUrl)

            url.searchParams.set(
                "telegram_init_data",
                initData
            )

            window.location.replace(url)

            return
        }

        enableControls()

        await loadCurrentUser()

    } catch (error) {

        showError(error)

    }

}

async function api(path, options = {}) {

    await keycloak.updateToken(30)

    const response = await fetch(`${API_URL}${path}`, {

        ...options,

        headers: {
            Authorization: `Bearer ${keycloak.token}`,
            Accept: "application/json",
            ...options.headers
        }

    })

    if (!response.ok) {

        const body = await readResponseBody(response)

        throw new Error(
            `HTTP ${response.status}\n${body}`
        )

    }

    if (response.status === 204) {
        return null
    }

    return response.json()

}

async function loadCurrentUser() {
    try {
        hideError()

        setStatus("Завантаження користувача...")

        const user = await api(
            "/api/v1/users/me",
            {
                method: "PUT"
            }
        )

        await keycloak.updateToken(-1)

        renderUser(user)

        setStatus("Готово")
    } catch (error) {
        showError(error)
    }
}
async function updateUsername() {

    if (!ui.usernameInput.reportValidity()) {
        return
    }

    const username =
        ui.usernameInput.value.trim()

    if (!username.length) {
        return
    }

    ui.updateButton.disabled = true

    try {

        hideError()

        setStatus("Оновлення username...")

        const user = await api(
            "/api/v1/users/me/username",
            {
                method: "PATCH",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    username
                })
            }
        )

        renderUser(user)

        ui.usernameInput.value = ""

        setStatus("Username успішно оновлено")

    } catch (error) {

        showError(error)

    } finally {

        ui.updateButton.disabled = false

    }

}

function renderUser(user) {

    ui.userId.textContent = user.id
    ui.username.textContent = user.username
    ui.telegramId.textContent = user.telegramId
    ui.keycloakId.textContent = user.keycloakId

    ui.createdAt.textContent = new Date(
        user.createdAt
    ).toLocaleString("uk-UA")

    ui.userCard.classList.remove("hidden")
    ui.usernameSection.classList.remove("hidden")
    ui.buttons.classList.remove("hidden")

}

async function readResponseBody(response) {

    const text = await response.text()

    if (!text) {
        return response.statusText
    }

    try {

        return JSON.stringify(
            JSON.parse(text),
            null,
            2
        )

    } catch {

        return text

    }

}

function setStatus(message) {

    ui.status.textContent = message

}

function enableControls() {

    ui.reloadButton.disabled = false
    ui.logoutButton.disabled = false
    ui.updateButton.disabled = false

}

function disableControls() {

    ui.reloadButton.disabled = true
    ui.logoutButton.disabled = true
    ui.updateButton.disabled = true

}

function hideError() {

    ui.error.textContent = ""
    ui.error.classList.add("hidden")

}

function showError(error) {

    console.error(error)

    setStatus("Сталася помилка")

    ui.error.textContent =
        error instanceof Error
            ? error.message
            : String(error)

    ui.error.classList.remove("hidden")

}

ui.reloadButton.addEventListener(
    "click",
    loadCurrentUser
)

ui.updateButton.addEventListener(
    "click",
    updateUsername
)

ui.usernameInput.addEventListener(
    "keydown",
    event => {

        if (event.key === "Enter") {
            event.preventDefault()
            updateUsername()
        }

    }
)

ui.logoutButton.addEventListener(
    "click",
    async () => {

        try {

            disableControls()

            setStatus("Вихід...")

            await keycloak.logout({
                redirectUri: window.location.origin
            })

        } catch (error) {

            enableControls()
            showError(error)

        }

    }
)

window.addEventListener(
    "unhandledrejection",
    event => {

        console.error(event.reason)
        showError(event.reason)

    }
)

window.addEventListener(
    "error",
    event => {

        console.error(event.error)
        showError(event.error)

    }
)