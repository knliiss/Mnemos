package com.mnemos.bet.configuration

import java.math.BigDecimal

data class EventBetOdds(
    val yes: BigDecimal,
    val no: BigDecimal,
)
