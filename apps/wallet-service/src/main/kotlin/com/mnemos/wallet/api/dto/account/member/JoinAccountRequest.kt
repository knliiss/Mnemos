package com.mnemos.accountservice.api.dto.account.member

import jakarta.validation.constraints.NotBlank

data class JoinAccountRequest(
    @field:NotBlank
    val inviteCode: String,
)
