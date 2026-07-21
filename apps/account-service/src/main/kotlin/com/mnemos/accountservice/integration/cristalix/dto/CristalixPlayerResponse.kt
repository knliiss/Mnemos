package com.mnemos.accountservice.integration.cristalix.dto

import java.util.UUID

data class CristalixPlayerResponse(
    val playerId: UUID,
    val name: String
)
