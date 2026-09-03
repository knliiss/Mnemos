package com.mnemos.realtime.application.delivery.model

enum class RealtimeEventType(
    val eventName: String,
) {
    WALLET_TRANSACTION_COMPLETED("wallet.transaction.completed"),
    WALLET_TRANSACTION_FAILED("wallet.transaction.failed"),
    BET_OPERATION_COMPLETED("bet.operation.completed"),
    PERSONALIZATION_PURCHASE_COMPLETED("personalization.purchase.completed"),
    ACCOUNT_ACTIVATED("account.activated"),
    ACCOUNT_DEACTIVATED("account.deactivated"),
    ACCOUNT_DELETED("account.deleted"),
    ACCOUNT_OWNER_TRANSFERRED("account.owner.transferred"),
    ACCOUNT_MEMBER_JOINED("account.member.joined"),
    ACCOUNT_MEMBER_LEFT("account.member.left"),
    ACCOUNT_MEMBER_REMOVED("account.member.removed"),
    ;

    companion object {
        fun fromEventName(eventName: String): RealtimeEventType =
            entries.firstOrNull { type -> type.eventName == eventName }
                ?: throw IllegalArgumentException("Unsupported realtime event type: $eventName")
    }
}
