package com.mnemos.personalization.contracts.event

import com.mnemos.personalization.contracts.shared.NotificationPackVariantField

data class NotificationPackVariantConditionV1(
    val field: NotificationPackVariantField,
    val value: String,
)
