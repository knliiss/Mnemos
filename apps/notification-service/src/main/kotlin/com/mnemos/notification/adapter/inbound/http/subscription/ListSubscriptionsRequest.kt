package com.mnemos.notification.adapter.inbound.http.subscription

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class ListSubscriptionsRequest(
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(100)
    val size: Int = 20,
)
