package com.mnemos.telegram.bot.application.widget.profileactivity

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryItem
import com.mnemos.telegram.bot.application.events.model.EventHistoryQuery
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
import com.mnemos.telegram.bot.application.events.port.out.EventHistoryGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.profile.model.PublicProfileDetailResult
import com.mnemos.telegram.bot.application.profile.port.out.PublicProfileGateway
import com.mnemos.telegram.bot.application.widget.model.WidgetTextLimiter
import com.mnemos.telegram.bot.application.widget.profileactivity.port.out.ProfileActivityWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenProfileActivityWidgetService(
    private val properties: TelegramWidgetProperties,
    private val profiles: PublicProfileGateway,
    private val events: EventHistoryGateway,
    private val publisher: ProfileActivityWidgetPublisher,
) : OpenProfileActivityWidgetUseCase, ProfileActivitySnapshotQuery {
    override val enabled: Boolean get() = properties.enabled

    override fun execute(telegramId: Long, profileId: UUID): TelegramDeliveryResult {
        if (!enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        return when (val result = load(telegramId, profileId)) {
            is ProfileActivitySnapshotResult.Available -> publisher.publish(telegramId, result.view)
            ProfileActivitySnapshotResult.NotFound -> TelegramDeliveryResult.PermanentFailure("profile-not-found")
            ProfileActivitySnapshotResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(telegramId: Long, profileId: UUID, after: UUID?): ProfileActivitySnapshotResult {
        if (telegramId <= 0) return ProfileActivitySnapshotResult.Unavailable
        val profile = when (val found = profiles.get(profileId)) {
            is PublicProfileDetailResult.Available -> found.profile
            PublicProfileDetailResult.NotFound -> return ProfileActivitySnapshotResult.NotFound
            PublicProfileDetailResult.Unavailable -> return ProfileActivitySnapshotResult.Unavailable
        }

        // The event-service enforces the exact droppedFor match and the authenticated internal caller.
        val result = events.getPage(
            EventHistoryQuery(
                filter = EventHistoryFilter.ITEM_DROP,
                after = after,
                droppedFor = profile.nickname,
                telegramId = telegramId,
            ),
        ) as? EventHistoryResult.Available ?: return ProfileActivitySnapshotResult.Unavailable

        val drops = result.page.items.filterIsInstance<EventHistoryItem.ItemDrop>()
            .filter { it.droppedFor.equals(profile.nickname, ignoreCase = true) }
        val cursor = result.page.cursor?.takeIf {
            result.page.items.size >= PAGE_SIZE && it != after
        }
        return ProfileActivitySnapshotResult.Available(
            ProfileActivityView(
                profileId = profile.accountId,
                nickname = WidgetTextLimiter.truncateUtf8(profile.nickname, 40),
                after = after,
                nextCursor = cursor,
                items = drops.take(PAGE_SIZE).map { drop ->
                    ProfileActivityItem(
                        name = WidgetTextLimiter.truncateUtf8(drop.itemName, 36),
                        rarity = WidgetTextLimiter.truncateUtf8(drop.itemRarity, 20),
                        occurredAt = drop.occurredAt.toString().take(16).replace('T', ' '),
                    )
                },
            ),
        )
    }

    private companion object {
        const val PAGE_SIZE = 8
    }
}
