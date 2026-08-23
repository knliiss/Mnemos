package com.mnemos.personalization.application.asset.model

data class PersonalizationAssetImage(
    val bytes: ByteArray,
    val contentType: String,
    val widthPx: Int,
    val heightPx: Int,
)
