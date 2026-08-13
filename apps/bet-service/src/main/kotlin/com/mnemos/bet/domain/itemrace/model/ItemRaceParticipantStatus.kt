package com.mnemos.bet.domain.itemrace.model

enum class ItemRaceParticipantStatus {
    PENDING,
    ACTIVE,
    PAYOUT_PENDING,
    PAYOUT_FAILED,
    WON,
    LOST,
    REFUND_PENDING,
    REFUND_FAILED,
    REFUNDED,
}
