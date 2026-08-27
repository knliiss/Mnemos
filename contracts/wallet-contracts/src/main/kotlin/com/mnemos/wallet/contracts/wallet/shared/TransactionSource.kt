package com.mnemos.wallet.contracts.wallet.shared

enum class TransactionSource {
    DAILY_REWARD,
    ITEM_DROP_REWARD,

    BET_PLACE,
    BET_WIN,
    BET_REFUND,

    PERSONALIZATION_PURCHASE,
    CREATOR_REVENUE,

    SPOTLIGHT_PURCHASE,

    PRIME_PURCHASE,
    COIN_PURCHASE,

    ADMIN_ADJUSTMENT,
}
