package com.mnemos.account.api.dto.identity

import com.mnemos.account.application.query.identity.IdentityDirectoryOrder
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class IdentityDirectoryRequest(
    @field:Size(max = 64)
    val query: String? = null,
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(50)
    val size: Int = 20,
    val order: IdentityDirectoryOrder = IdentityDirectoryOrder.NEWEST,
)
