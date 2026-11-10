package com.mnemos.telegram.bot.application.widget.profiles

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.profile.model.PublicProfileDetailResult
import com.mnemos.telegram.bot.application.profile.model.PublicProfileSearchResult
import com.mnemos.telegram.bot.application.profile.model.PublicProfileSummary
import com.mnemos.telegram.bot.application.profile.port.out.PublicProfileGateway
import com.mnemos.telegram.bot.application.widget.model.WidgetTextLimiter
import com.mnemos.telegram.bot.application.widget.profiles.port.out.ProfileWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenProfileWidgetService(
    private val properties: TelegramWidgetProperties,
    private val profiles: PublicProfileGateway,
    private val publisher: ProfileWidgetPublisher,
) : OpenProfileWidgetUseCase, ProfileWidgetSnapshotQuery {
    override val enabled: Boolean get() = properties.enabled

    override fun execute(telegramId: Long): TelegramDeliveryResult {
        if (!enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        return when (val snapshot = load(telegramId, null, 0)) {
            is ProfileWidgetSnapshotResult.Available -> publisher.publish(telegramId, snapshot.view)
            else -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(
        telegramId: Long,
        query: String?,
        page: Int,
        selectedProfileId: UUID?,
    ): ProfileWidgetSnapshotResult {
        if (telegramId <= 0 || page !in 0..1_000 ||
            (query != null && ProfileWidgetSearchQuery.normalize(query) != query)
        ) return ProfileWidgetSnapshotResult.Unavailable

        if (selectedProfileId != null) {
            return when (val result = profiles.get(selectedProfileId)) {
                is PublicProfileDetailResult.Available ->
                    ProfileWidgetSnapshotResult.Available(
                        ProfileWidgetView(
                            query = query,
                            page = page,
                            totalPages = 1,
                            totalItems = 1,
                            profiles = emptyList(),
                            selected = result.profile.toEntry(),
                        ),
                    )
                PublicProfileDetailResult.NotFound -> ProfileWidgetSnapshotResult.NotFound
                PublicProfileDetailResult.Unavailable -> ProfileWidgetSnapshotResult.Unavailable
            }
        }

        if (query == null) {
            return ProfileWidgetSnapshotResult.Available(
                ProfileWidgetView(null, page = 0, totalPages = 1, totalItems = 0, profiles = emptyList()),
            )
        }

        return when (val result = profiles.search(query, page, PAGE_SIZE)) {
            is PublicProfileSearchResult.Available -> {
                val source = result.page
                ProfileWidgetSnapshotResult.Available(
                    ProfileWidgetView(
                        query = query,
                        page = source.page,
                        totalPages = maxOf(1, source.totalPages),
                        totalItems = source.totalItems,
                        profiles = source.items.take(PAGE_SIZE).map { it.toEntry() },
                    ),
                )
            }
            PublicProfileSearchResult.Unavailable -> ProfileWidgetSnapshotResult.Unavailable
        }
    }

    private fun PublicProfileSummary.toEntry(): ProfileWidgetEntry =
        ProfileWidgetEntry(
            accountId = accountId,
            nickname = WidgetTextLimiter.truncateUtf8(nickname, 40),
            likes = likes,
            dislikes = dislikes,
            comments = comments,
            presenceStatus = presenceStatus,
            realm = realm?.let { WidgetTextLimiter.truncateUtf8(it, 35) },
            lastSeenOnline = lastSeenOnline?.toString()?.take(16)?.plus(" UTC"),
            joinedAt = createdAt.toString().take(10),
        )

    private companion object {
        const val PAGE_SIZE = 4
    }
}
