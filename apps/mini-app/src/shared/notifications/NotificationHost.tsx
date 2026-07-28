import {Check, Copy, X} from "lucide-react"
import {useEffect, useState} from "react"
import {notifications} from "./notificationCenter"
import type {AppNotification} from "./notification.types"

export function NotificationHost() {
    const [notification, setNotification] = useState<AppNotification | null>(null)
    const [copied, setCopied] = useState(false)

    useEffect(() => notifications.subscribe(value => {
        setNotification(value)
        setCopied(false)
    }), [])

    useEffect(() => {
        if (!notification || notification.persistent) return
        const timeout = window.setTimeout(() => notifications.clear(), 3500)
        return () => window.clearTimeout(timeout)
    }, [notification])

    if (!notification) return null

    async function copy() {
        if (!notification) return
        await navigator.clipboard.writeText(notification.message)
        setCopied(true)
        window.setTimeout(() => setCopied(false), 1500)
    }

    const CopyIcon = copied ? Check : Copy
    return <div className={`notification notification--${notification.kind}`} role="status">
        <div className="notification__message">{notification.message}</div>
        <div className="notification__actions">
            {notification.kind === "error" && <button type="button" className="notification__button" onClick={() => void copy()} aria-label="Скопировать"><CopyIcon size={18}/></button>}
            <button type="button" className="notification__button" onClick={() => notifications.clear()} aria-label="Закрыть"><X size={18}/></button>
        </div>
    </div>
}
