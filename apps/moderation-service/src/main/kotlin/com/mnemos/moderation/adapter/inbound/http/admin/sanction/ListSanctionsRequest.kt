package com.mnemos.moderation.adapter.inbound.http.admin.sanction

import java.util.UUID

data class ListSanctionsRequest(
    val userId: UUID,
)
