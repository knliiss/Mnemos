package com.mnemos.telegram.bot.application.widget.events

import com.mnemos.telegram.bot.application.events.EventHistorySearchQuery
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryItem
import com.mnemos.telegram.bot.application.events.model.EventHistoryQuery
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
import com.mnemos.telegram.bot.application.events.port.out.EventHistoryGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.events.port.out.EventWidgetPublisher
import com.mnemos.telegram.bot.application.widget.model.WidgetTextLimiter
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import java.util.UUID
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenEventWidgetService(
    private val properties: TelegramWidgetProperties,
    private val events: EventHistoryGateway,
    private val publisher: EventWidgetPublisher,
) : OpenEventWidgetUseCase,
    EventWidgetSnapshotQuery {
    override val enabled: Boolean
        get() = properties.enabled

    override fun execute(
        telegramId: Long,
        filter: EventHistoryFilter,
        after: UUID?,
        search: String?,
    ): TelegramDeliveryResult {
        if (!enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        return when (val snapshot = load(telegramId, filter, after, search)) {
            is EventWidgetSnapshotResult.Available -> publisher.publish(telegramId, snapshot.view)
            EventWidgetSnapshotResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(
        telegramId: Long,
        filter: EventHistoryFilter,
        after: UUID?,
        search: String?,
    ): EventWidgetSnapshotResult {
        if (search != null && EventHistorySearchQuery.normalize(search) != search) {
            return EventWidgetSnapshotResult.Unavailable
        }
        val result = events.getPage(EventHistoryQuery(filter = filter, after = after, search = search, telegramId = telegramId))
        if (result !is EventHistoryResult.Available) return EventWidgetSnapshotResult.Unavailable

        return EventWidgetSnapshotResult.Available(
            EventWidgetView(
                filter = filter,
                after = after,
                nextCursor = result.page.cursor,
                search = search,
                items =
                    result.page.items.map { item ->
                        val (kind, title) =
                            when (item) {
                                is EventHistoryItem.ItemDrop -> "Дроп" to (item.itemName + " · " + item.droppedFor)
                                is EventHistoryItem.Booster -> "Бустер" to (item.boosterType + " · " + item.activatedBy)
                                is EventHistoryItem.Global -> "Глобальна" to item.globalType
                                is EventHistoryItem.Raid -> "Рейд" to item.locations.joinToString(", ")
                            }
                        EventWidgetItem(kind, WidgetTextLimiter.truncateUtf8(title, 36), item.occurredAt.toString().substring(11, 16) + " UTC")
                    },
            ),
        )
    }
}
