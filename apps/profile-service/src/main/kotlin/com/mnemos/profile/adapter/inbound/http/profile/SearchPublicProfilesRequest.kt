package com.mnemos.profile.adapter.inbound.http.profile

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class SearchPublicProfilesRequest(
    @field:Size(min = 2, max = 64)
    val q: String,
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(100)
    val size: Int = 20,
)
