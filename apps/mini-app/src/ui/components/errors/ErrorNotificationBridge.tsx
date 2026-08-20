import { useEffect } from "react"
import { errorEvents } from "@/shared/errors/errorEventBus"
import { useErrorNotifications } from "./ErrorNotifications"

export function ErrorNotificationBridge() {
  const { showBackendError } = useErrorNotifications()

  useEffect(() => errorEvents.subscribe(showBackendError), [showBackendError])

  return null
}
