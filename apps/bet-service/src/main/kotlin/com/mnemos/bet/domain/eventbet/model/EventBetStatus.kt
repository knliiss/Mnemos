package com.mnemos.bet.domain.eventbet.model

enum class EventBetStatus {
    ACTIVE,
    SETTLEMENT_PENDING,
    SETTLEMENT_FAILED,
    SETTLED,
    REFUND_PENDING,
    REFUND_FAILED,
    REFUNDED,
}
