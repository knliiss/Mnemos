package com.mnemos.account.api.dto.account

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateAccountRequest(
    @field:NotBlank
    @field:Size(min = 4, max = 20, message = "Name must be between 4 and 20")
    val cristalixNickname: String,
)

data class UpdateAccountBaseRoleRequest(
    val roleId: UUID?,
)
