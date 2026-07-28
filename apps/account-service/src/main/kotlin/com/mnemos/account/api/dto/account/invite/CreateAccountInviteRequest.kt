package com.mnemos.account.api.dto.account.invite

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class CreateAccountInviteRequest(
    @field:Min(1)
    @field:Max(10_080)
    val expiresInMinutes: Long? = 1_440,
)
