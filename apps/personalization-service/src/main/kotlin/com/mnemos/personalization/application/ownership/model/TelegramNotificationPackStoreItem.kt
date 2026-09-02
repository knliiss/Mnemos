package com.mnemos.personalization.application.ownership.model

import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.wallet.contracts.wallet.shared.Currency

data class TelegramNotificationPackStorePage(
    val items: List<TelegramNotificationPackStoreItem>,
    val page: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNextPage: Boolean,
)

data class TelegramNotificationPackStoreItem(
    val personalizationId: PersonalizationId,
    val name: String,
    val author: String,
    val priceAmount: Long,
    val currency: Currency,
    val owned: Boolean,
    val active: Boolean,
    val samples: List<String>,
)
