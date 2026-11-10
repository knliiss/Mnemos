package com.mnemos.telegram.bot.application.widget.settings

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.settings.port.out.SettingsGateway
import com.mnemos.telegram.bot.application.widget.model.WidgetTextLimiter
import com.mnemos.telegram.bot.application.widget.settings.port.out.SettingsWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenSettingsWidgetService(
    private val properties: TelegramWidgetProperties,
    private val gateway: SettingsGateway,
    private val publisher: SettingsWidgetPublisher,
) : OpenSettingsWidgetUseCase, SettingsWidgetSnapshotQuery {
    override val enabled: Boolean get() = properties.enabled

    override fun execute(telegramId: Long, page: Int): TelegramDeliveryResult {
        if (!enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        return when (val snapshot = load(telegramId, page)) {
            is SettingsWidgetSnapshotResult.Available -> publisher.publish(telegramId, snapshot.view)
            SettingsWidgetSnapshotResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(telegramId: Long, page: Int): SettingsWidgetSnapshotResult {
        if (page !in 0..1_000) return SettingsWidgetSnapshotResult.Unavailable
        val snapshot = gateway.getSettings(telegramId) ?: return SettingsWidgetSnapshotResult.Unavailable
        val pages = maxOf(1, (snapshot.notificationPacks.size + PAGE_SIZE - 1) / PAGE_SIZE)
        val actualPage = page.coerceAtMost(pages - 1)
        return SettingsWidgetSnapshotResult.Available(
            SettingsWidgetView(
                language = snapshot.localizationLanguage,
                activePackId = snapshot.activeNotificationPackId,
                activePackTitle = snapshot.activeNotificationPackTitle?.let { WidgetTextLimiter.truncateUtf8(it, 42) },
                packs = snapshot.notificationPacks
                    .drop(actualPage * PAGE_SIZE)
                    .take(PAGE_SIZE)
                    .map { SettingsWidgetPack(it.personalizationId, WidgetTextLimiter.truncateUtf8(it.title, 42), it.active) },
                totalPacks = snapshot.notificationPacks.size,
                page = actualPage,
                totalPages = pages,
            ),
        )
    }

    private companion object {
        const val PAGE_SIZE = 4
    }
}
