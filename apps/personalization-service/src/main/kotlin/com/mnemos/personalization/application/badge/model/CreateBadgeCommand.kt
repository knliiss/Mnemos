package com.mnemos.personalization.application.badge.model

import com.mnemos.personalization.domain.personalization.model.HexColor

data class CreateBadgeCommand(
    val code: String,
    val name: String,
    val icon: String,
    val color: HexColor,
)
