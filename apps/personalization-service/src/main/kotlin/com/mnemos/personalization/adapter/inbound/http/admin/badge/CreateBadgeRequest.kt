package com.mnemos.personalization.adapter.inbound.http.admin.badge

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateBadgeRequest(
    @field:Pattern(regexp = "^[A-Z][A-Z0-9_]{1,31}$")
    val code: String,
    @field:NotBlank
    @field:Size(max = 64)
    val name: String,
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val color: String,
)
