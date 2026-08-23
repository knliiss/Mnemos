package com.mnemos.personalization.adapter.inbound.http.badge

import java.util.UUID

data class BadgeResponse(
    val id: UUID,
    val code: String,
    val name: String,
    val imageUrl: String,
    val color: String,
)
