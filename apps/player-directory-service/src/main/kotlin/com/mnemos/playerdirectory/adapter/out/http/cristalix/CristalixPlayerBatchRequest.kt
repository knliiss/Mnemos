package com.mnemos.playerdirectory.adapter.out.http.cristalix

import java.util.UUID

data class CristalixPlayerBatchRequest(
    val ids: List<UUID>,
)
