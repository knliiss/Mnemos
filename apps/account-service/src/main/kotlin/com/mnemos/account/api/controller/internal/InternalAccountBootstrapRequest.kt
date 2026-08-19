package com.mnemos.account.api.controller.internal

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.util.UUID

data class InternalAccountBootstrapRequest(
    val afterAccountId: UUID? = null,
    @field:Min(1)
    @field:Max(500)
    val limit: Int = 200,
)
