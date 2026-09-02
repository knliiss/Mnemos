package com.mnemos.telegram.bot.application.pack.port.out

import com.mnemos.telegram.bot.application.pack.model.NotificationPackApplyResult
import com.mnemos.telegram.bot.application.pack.model.NotificationPackDetailResult
import com.mnemos.telegram.bot.application.pack.model.NotificationPackPurchaseResult
import com.mnemos.telegram.bot.application.pack.model.NotificationPackStoreResult
import com.mnemos.telegram.bot.application.pack.model.OwnedNotificationPacksResult
import java.util.UUID

interface NotificationPackGateway {
    fun store(
        telegramId: Long,
        page: Int,
        size: Int,
    ): NotificationPackStoreResult

    fun detail(
        telegramId: Long,
        personalizationId: UUID,
    ): NotificationPackDetailResult

    fun owned(telegramId: Long): OwnedNotificationPacksResult

    fun purchase(
        telegramId: Long,
        personalizationId: UUID,
        idempotencyKey: UUID,
    ): NotificationPackPurchaseResult

    fun apply(
        telegramId: Long,
        personalizationId: UUID,
    ): NotificationPackApplyResult
}
