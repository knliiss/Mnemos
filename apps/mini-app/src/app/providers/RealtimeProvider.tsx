import { useEffect, type ReactNode } from "react"
import { useTranslation } from "react-i18next"
import { errorEvents } from "@/shared/errors/errorEventBus"
import { notifications } from "@/shared/notifications/notificationCenter"
import { realtimeClient } from "@/shared/realtime/realtimeClient"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import type { RealtimeEvent } from "@/shared/realtime/realtime.types"

export function RealtimeProvider({ children }: { children: ReactNode }) {
  const { t } = useTranslation()

  useEffect(() => {
    const stop = realtimeClient.start()
    const unsubscribe = realtimeEvents.subscribe((event) => {
      notifyAboutOperation(event, (key) => t(key))
    })

    return () => {
      unsubscribe()
      stop()
    }
  }, [t])

  return children
}

function notifyAboutOperation(event: RealtimeEvent, translate: (key: string) => string) {
  if (event.type === "wallet.transaction.failed") {
    if (event.payload.source !== "BET_PLACE") {
      errorEvents.publish({
        code: "WALLET_TRANSACTION_FAILED",
        message: translate("realtime.transactionFailed"),
        statusCode: 409,
        details: {
          reason: event.payload.reason,
          source: event.payload.source,
        },
        requestId: event.payload.requestId,
      })
    }

    return
  }

  if (event.type !== "bet.operation.completed") {
    return
  }

  const status = event.payload.status

  if (status === "CONFIRMED") {
    notifications.success(translate("realtime.betConfirmed"))
    return
  }

  if (status === "REJECTED") {
    notifications.warning(translate("realtime.betRejected"))
    return
  }

  errorEvents.publish({
    code: "BET_OPERATION_FAILED",
    message: event.payload.failureReason ?? translate("realtime.betFailed"),
    statusCode: 500,
    details: {
      resourceType: event.payload.resourceType,
      resourceId: event.payload.resourceId,
    },
  })
}
