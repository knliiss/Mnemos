package com.mnemos.accountservice.api.dto.account

import jakarta.validation.constraints.NotBlank

data class CreateAccountRequest(

    @field:NotBlank(message = "Cristalix ID cannot be blank")
    val cristalixNickname: String,
)