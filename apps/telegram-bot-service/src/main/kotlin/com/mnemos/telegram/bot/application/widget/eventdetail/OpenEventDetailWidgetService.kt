package com.mnemos.telegram.bot.application.widget.eventdetail

import com.mnemos.telegram.bot.application.events.model.EventDetailResult
import com.mnemos.telegram.bot.application.events.model.EventHistoryItem
import com.mnemos.telegram.bot.application.events.port.out.EventDetailGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.eventdetail.port.out.EventDetailWidgetPublisher
import com.mnemos.telegram.bot.application.widget.model.WidgetTextLimiter
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenEventDetailWidgetService(
    private val properties: TelegramWidgetProperties,
    private val events: EventDetailGateway,
    private val publisher: EventDetailWidgetPublisher,
) : OpenEventDetailWidgetUseCase {
    override fun execute(telegramId: Long, eventId: UUID): TelegramDeliveryResult {
        if (!properties.enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        return when (val result = events.get(eventId)) {
            is EventDetailResult.Available -> publisher.publish(telegramId, result.item.toView())
            EventDetailResult.NotFound -> TelegramDeliveryResult.PermanentFailure("event-not-found")
            EventDetailResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    private fun EventHistoryItem.toView(): EventDetailView {
        val name: String
        val title: String
        val facts: List<EventDetailFact>
        when (this) {
            is EventHistoryItem.ItemDrop -> {
                name = "Дроп"
                title = itemName
                facts = listOf(
                    EventDetailFact("Гравець", droppedFor),
                    EventDetailFact("Рідкість", itemRarity),
                    EventDetailFact("Тип предмета", itemType),
                )
            }
            is EventHistoryItem.Booster -> {
                name = "Бустер"
                title = boosterType
                facts = listOf(EventDetailFact("Активував", activatedBy))
            }
            is EventHistoryItem.Global -> {
                name = "Глобальна подія"
                title = globalType
                facts = emptyList()
            }
            is EventHistoryItem.Raid -> {
                name = "Рейд"
                title = "Локації рейду"
                facts = listOf(EventDetailFact("Локації", locations.joinToString(", ")))
            }
        }
        return EventDetailView(
            type = name,
            title = WidgetTextLimiter.truncateUtf8(title, 96),
            occurredAt = occurredAt.toString().take(16).replace('T', ' ') + " UTC",
            facts = facts.map {
                EventDetailFact(it.title, WidgetTextLimiter.truncateUtf8(it.value, 160))
            },
        )
    }
}
