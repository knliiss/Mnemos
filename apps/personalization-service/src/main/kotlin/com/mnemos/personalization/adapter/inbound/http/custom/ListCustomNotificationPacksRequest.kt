package com.mnemos.personalization.adapter.inbound.http.custom

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class ListCustomNotificationPacksRequest(
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(100)
    val size: Int = 20,
)
