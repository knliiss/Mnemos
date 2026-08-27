package com.mnemos.playerdirectory.adapter.out.http.cristalix

import java.util.UUID

data class CristalixPlayerNameBatchResponse(
    val requested: Int,
    val found: Int,
    val notFound: List<String> = emptyList(),
    val items: List<CristalixPlayerNameBatchItemResponse> = emptyList(),
)

data class CristalixPlayerNameBatchItemResponse(
    val playerId: UUID,
    val name: String,
)
