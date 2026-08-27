package com.mnemos.playerdirectory.adapter.out.http.cristalix

import java.time.Instant
import java.util.UUID

data class CristalixPlayerResponse(
    val playerId: UUID,
    val name: String,
    val version: Long,
    val updatedAt: Instant?,
    val social: Social? = null,
    val status: Status? = null,
    val customization: Customization? = null,
) {
    data class Social(
        val lastSeenOnline: Instant? = null,
    )

    data class Status(
        val online: Boolean? = null,
        val realm: String? = null,
        val realmType: String? = null,
        val privacyHidden: Boolean? = null,
    )

    data class Customization(
        val settings: Map<String, String> = emptyMap(),
    )
}
