package com.mnemos.playerdirectory.adapter.out.http.cristalix

import java.util.UUID

data class CristalixPlayerBatchResponse(
    val requested: Int,
    val found: Int,
    val notFound: List<UUID> = emptyList(),
    val items: List<CristalixPlayerResponse> = emptyList(),
)
