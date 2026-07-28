import {ApiError, NetworkError} from "@/shared/api/httpClient"
import {notifications} from "@/shared/notifications/notificationCenter"
import {AppError} from "./AppError"

function formatApiError(error: ApiError): string {
    const body = error.body
    if (typeof body === "string") return body
    const lines = [`HTTP ${error.status}`]
    if (body.code) lines.push(`Код: ${body.code}`)
    if (body.message) lines.push(`Сообщение: ${body.message}`)
    if (body.path) lines.push(`Путь: ${body.path}`)
    if (body.requestId) lines.push(`Request ID: ${body.requestId}`)
    if (body.details && Object.keys(body.details).length) lines.push(`Детали: ${JSON.stringify(body.details)}`)
    return lines.join("\n")
}

export function normalizeError(error: unknown): AppError {
    if (error instanceof AppError) return error
    if (error instanceof ApiError) return new AppError("api", formatApiError(error), error)
    if (error instanceof NetworkError) return new AppError("network", error.message, error)
    if (error instanceof Error) return new AppError("unexpected", error.message || "Произошла неизвестная ошибка", error)
    return new AppError("unexpected", String(error), error)
}

export function handleError(error: unknown): AppError {
    const normalized = normalizeError(error)
    console.error(normalized, normalized.cause)
    notifications.error(normalized.message)
    return normalized
}

export async function executeAction<T>(action: () => Promise<T>): Promise<T | undefined> {
    try {
        return await action()
    } catch (error) {
        handleError(error)
        return undefined
    }
}

export function installGlobalErrorHandlers() {
    window.addEventListener("error", event => handleError(event.error ?? event.message))
    window.addEventListener("unhandledrejection", event => {
        event.preventDefault()
        handleError(event.reason)
    })
}
