package com.mnemos.accountservice.api.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateUsernameRequest(

    @field:NotBlank(message = "Username cannot be blank")
    @field:Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    val username: String
)
