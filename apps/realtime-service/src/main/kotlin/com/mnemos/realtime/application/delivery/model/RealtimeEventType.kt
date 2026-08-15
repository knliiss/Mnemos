package com.mnemos.realtime.application.delivery.model

enum class RealtimeEventType(
    val eventName: String,
) {
    WALLET_TRANSACTION_COMPLETED("wallet.transaction.completed"),
    WALLET_TRANSACTION_FAILED("wallet.transaction.failed"),
    BET_OPERATION_COMPLETED("bet.operation.completed"),
    PERSONALIZATION_PURCHASE_COMPLETED("personalization.purchase.completed"),
    ;

    companion object {
        fun fromEventName(eventName: String): RealtimeEventType =
            entries.firstOrNull { type -> type.eventName == eventName }
                ?: throw IllegalArgumentException("Unsupported realtime event type: $eventName")
    }
}
