package com.mnemos.telegram.bot.application.widget.notifications

import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import com.mnemos.telegram.bot.application.widget.model.WidgetTextLimiter
import com.mnemos.telegram.bot.application.widget.notifications.port.out.NotificationWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenNotificationWidgetService(
    private val properties: TelegramWidgetProperties,
    private val accounts: AccountGateway,
    private val subscriptions: NotificationSubscriptionGateway,
    private val publisher: NotificationWidgetPublisher,
) : OpenNotificationWidgetUseCase,
    NotificationWidgetSnapshotQuery {
    override val enabled: Boolean
        get() = properties.enabled

    override fun execute(
        telegramId: Long,
        page: Int,
    ): TelegramDeliveryResult {
        if (!enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")

        return when (val snapshot = load(telegramId, page)) {
            is NotificationWidgetSnapshotResult.Available -> publisher.publish(telegramId, snapshot.view)
            NotificationWidgetSnapshotResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(
        telegramId: Long,
        page: Int,
    ): NotificationWidgetSnapshotResult {
        if (page !in 0..1_000) return NotificationWidgetSnapshotResult.Unavailable

        val accountResult = accounts.getContext(telegramId)
        if (accountResult == TelegramAccountContextResult.Unavailable) {
            return NotificationWidgetSnapshotResult.Unavailable
        }
        val context = (accountResult as? TelegramAccountContextResult.Available)?.context
        val result = subscriptions.list(telegramId, page, PAGE_SIZE)
        if (result !is NotificationSubscriptionResult.Available) {
            return NotificationWidgetSnapshotResult.Unavailable
        }

        return NotificationWidgetSnapshotResult.Available(
            NotificationWidgetView(
                accountId = context?.activeAccountId,
                page = result.page.page,
                totalPages = result.page.totalPages,
                totalDrops = result.page.totalItems,
                pingEnabled = result.pingEnabled,
                itemDrops =
                    result.page.items.map { entry ->
                        val label =
                            listOfNotNull(entry.itemName, entry.droppedFor)
                                .filter(String::isNotBlank)
                                .joinToString(" · ")
                                .ifBlank { "Усі дропи" }
                                .let { WidgetTextLimiter.truncateUtf8(it, 48) }
                        NotificationWidgetDrop(id = entry.id, label = label)
                    },
                boosters = result.boosterSubscriptions.size,
                dungeons = result.dungeonSubscriptions.size,
                globalEvents = result.globalSubscriptions.size,
                raidEnabled = result.raidSubscription != null,
            ),
        )
    }

    private companion object {
        const val PAGE_SIZE = 4
    }
}
