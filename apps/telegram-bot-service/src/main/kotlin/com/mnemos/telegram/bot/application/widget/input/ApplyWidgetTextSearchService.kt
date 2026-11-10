package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.events.EventWidgetSnapshotQuery
import com.mnemos.telegram.bot.application.widget.events.EventWidgetSnapshotResult
import com.mnemos.telegram.bot.application.widget.events.port.out.EventWidgetPublisher
import com.mnemos.telegram.bot.application.widget.online.OnlineWidgetSnapshotQuery
import com.mnemos.telegram.bot.application.widget.online.OnlineWidgetSnapshotResult
import com.mnemos.telegram.bot.application.widget.online.port.out.OnlineWidgetPublisher
import com.mnemos.telegram.bot.application.widget.profiles.ProfileWidgetSnapshotQuery
import com.mnemos.telegram.bot.application.widget.profiles.ProfileWidgetSnapshotResult
import com.mnemos.telegram.bot.application.widget.profiles.port.out.ProfileWidgetPublisher
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class ApplyWidgetTextSearchService(
    private val sessions: TelegramWidgetSessionRepository,
    private val eventSnapshots: EventWidgetSnapshotQuery,
    private val eventPublisher: EventWidgetPublisher,
    private val onlineSnapshots: OnlineWidgetSnapshotQuery,
    private val onlinePublisher: OnlineWidgetPublisher,
    private val profileSnapshots: ProfileWidgetSnapshotQuery,
    private val profilePublisher: ProfileWidgetPublisher,
) : ApplyWidgetTextSearchUseCase {
    override fun apply(session: TelegramWidgetSession, search: String): TelegramDeliveryResult {
        if (search.isBlank() || search.length > 64 || search.toByteArray(Charsets.UTF_8).size > 256) {
            return TelegramDeliveryResult.PermanentFailure("invalid-widget-search")
        }

        val result =
            when (session.screen) {
                TelegramWidgetScreen.EVENTS -> {
                    val filter = EventHistoryFilter.entries.getOrNull(session.page)
                        ?: return TelegramDeliveryResult.PermanentFailure("widget-expired")
                    val snapshot =
                        eventSnapshots.load(session.telegramId, filter, null, search) as? EventWidgetSnapshotResult.Available
                            ?: return TelegramDeliveryResult.Retryable
                    eventPublisher.update(session.telegramId, session.messageId, snapshot.view)
                }
                TelegramWidgetScreen.ONLINE -> {
                    val snapshot =
                        onlineSnapshots.load(session.telegramId, null, search) as? OnlineWidgetSnapshotResult.Available
                            ?: return TelegramDeliveryResult.Retryable
                    onlinePublisher.update(session.telegramId, session.messageId, snapshot.view)
                }
                TelegramWidgetScreen.PROFILES -> {
                    val snapshot =
                        profileSnapshots.load(session.telegramId, search, 0, null) as? ProfileWidgetSnapshotResult.Available
                            ?: return TelegramDeliveryResult.Retryable
                    profilePublisher.update(session.telegramId, session.messageId, snapshot.view)
                }
                else -> return TelegramDeliveryResult.PermanentFailure("unsupported-widget-search")
            }

        if (result == TelegramDeliveryResult.Delivered) {
                        sessions.save(session.copy(
                page = if (session.screen == TelegramWidgetScreen.ONLINE || session.screen == TelegramWidgetScreen.PROFILES) 0 else session.page,
                cursor = null,
                search = search,
                selectedProfileId = null,
            ))
        }
        return result
    }
}
