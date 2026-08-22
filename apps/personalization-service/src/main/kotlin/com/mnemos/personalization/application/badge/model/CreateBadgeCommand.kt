package com.mnemos.personalization.application.badge.model

import com.mnemos.personalization.domain.personalization.model.HexColor

data class CreateBadgeCommand(
    val code: String,
    val name: String,
    val image: BadgeImageUpload,
    val color: HexColor,
)

data class BadgeImageUpload(
    val bytes: ByteArray,
    val contentType: String?,
)
