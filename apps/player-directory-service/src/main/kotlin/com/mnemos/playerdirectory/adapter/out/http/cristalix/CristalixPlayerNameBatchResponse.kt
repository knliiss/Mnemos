package com.mnemos.playerdirectory.adapter.out.http.cristalix

data class CristalixPlayerNameBatchResponse(
    val requested: Int,
    val found: Int,
    val notFound: List<String> = emptyList(),
    val items: List<CristalixPlayerResponse> = emptyList(),
)
