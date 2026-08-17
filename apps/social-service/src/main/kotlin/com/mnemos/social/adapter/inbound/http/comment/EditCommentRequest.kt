package com.mnemos.social.adapter.inbound.http.comment

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class EditCommentRequest(
    @field:NotBlank
    @field:Size(max = 500)
    val content: String,
)
