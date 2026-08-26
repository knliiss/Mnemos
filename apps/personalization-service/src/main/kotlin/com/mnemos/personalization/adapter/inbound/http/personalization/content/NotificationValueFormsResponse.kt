package com.mnemos.personalization.adapter.inbound.http.personalization.content

data class NotificationValueFormsResponse(
    val default: String,
    val forms: Map<String, String>,
)
