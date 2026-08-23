import { Check, Copy, X } from "lucide-react"
import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import type { AppNotification } from "./notification.types"
import { notifications } from "./notificationCenter"

const AUTO_DISMISS_DELAY_MS = 3_500
const COPY_FEEDBACK_DELAY_MS = 1_500

export function NotificationHost() {
  const { t } = useTranslation()
  const [notification, setNotification] = useState<AppNotification | null>(null)
  const [copied, setCopied] = useState(false)

  useEffect(
    () =>
      notifications.subscribe((value) => {
        setNotification(value)
        setCopied(false)
      }),
    [],
  )

  useEffect(() => {
    if (!notification || notification.persistent) {
      return
    }

    const timeout = window.setTimeout(() => notifications.clear(), AUTO_DISMISS_DELAY_MS)

    return () => window.clearTimeout(timeout)
  }, [notification])

  if (!notification) {
    return null
  }

  const copyMessage = async () => {
    await navigator.clipboard.writeText(notification.message)
    setCopied(true)

    window.setTimeout(() => setCopied(false), COPY_FEEDBACK_DELAY_MS)
  }

  const CopyIcon = copied ? Check : Copy

  return (
    <div className={`notification notification--${notification.kind}`} role="status">
      <div className="notification__message">{notification.message}</div>

      <div className="notification__actions">
        {notification.kind === "error" ? (
          <button
            type="button"
            className="notification__button"
            onClick={() => void copyMessage()}
            aria-label={t("common.copy")}
          >
            <CopyIcon size={18} />
          </button>
        ) : null}

        <button
          type="button"
          className="notification__button"
          onClick={() => notifications.clear()}
          aria-label={t("common.close")}
        >
          <X size={18} />
        </button>
      </div>
    </div>
  )
}
