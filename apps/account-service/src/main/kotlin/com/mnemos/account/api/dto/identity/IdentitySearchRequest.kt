package com.mnemos.account.api.dto.identity

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class IdentitySearchRequest(
    @field:Size(min = 2, max = 64)
    val query: String,
    @field:Min(1)
    @field:Max(50)
    val limit: Int = 20,
)
