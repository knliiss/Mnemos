import {useEffect, type ReactNode} from "react"
import {useTranslation} from "react-i18next"
import {notifications} from "@/shared/notifications/notificationCenter"
import {realtimeClient} from "@/shared/realtime/realtimeClient"
import {realtimeEvents} from "@/shared/realtime/realtimeEventBus"
import type {RealtimeEvent} from "@/shared/realtime/realtime.types"

export function RealtimeProvider({children}: {children: ReactNode}) {
    const {t} = useTranslation()

    useEffect(() => {
        const stop = realtimeClient.start()
        const unsubscribe = realtimeEvents.subscribe(event => {
            notifyAboutOperation(event, key => t(key))
        })

        return () => {
            unsubscribe()
            stop()
        }
    }, [t])

    return children
}

function notifyAboutOperation(
    event: RealtimeEvent,
    translate: (key: string) => string
) {
    if (event.type === "wallet.transaction.failed") {
        if (event.payload.source !== "BET_PLACE") {
            notifications.error(translate("realtime.transactionFailed"))
        }

        return
    }

    if (event.type !== "bet.operation.completed") {
        return
    }

    const status = event.payload.status

    if (status === "CONFIRMED") {
        notifications.success(translate("realtime.betConfirmed"))
    } else if (status === "REJECTED") {
        notifications.warning(translate("realtime.betRejected"))
    } else {
        notifications.error(translate("realtime.betFailed"))
    }
}
