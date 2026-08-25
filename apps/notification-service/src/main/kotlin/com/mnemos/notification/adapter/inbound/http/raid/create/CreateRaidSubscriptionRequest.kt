package com.mnemos.notification.adapter.inbound.http.raid.create

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class CreateRaidSubscriptionRequest(
    val locations:
        Set<
            @Min(1)
            @Max(28)
            Int,
        >,
)
