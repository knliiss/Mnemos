package com.mnemos.personalization.contracts.event

data class NotificationPackValueFormsV1(
    val default: String,
    val forms: Map<String, String> = emptyMap(),
)
