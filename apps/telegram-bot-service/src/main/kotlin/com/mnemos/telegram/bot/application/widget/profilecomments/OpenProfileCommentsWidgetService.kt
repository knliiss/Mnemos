package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.profile.model.PublicProfileDetailResult
import com.mnemos.telegram.bot.application.profile.port.out.PublicProfileGateway
import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentsPageResult
import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentsGateway
import com.mnemos.telegram.bot.application.widget.model.WidgetTextLimiter
import com.mnemos.telegram.bot.application.widget.profilecomments.port.out.ProfileCommentsWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenProfileCommentsWidgetService(
    private val properties: TelegramWidgetProperties,
    private val profiles: PublicProfileGateway,
    private val comments: ProfileCommentsGateway,
    private val publisher: ProfileCommentsWidgetPublisher,
) : OpenProfileCommentsWidgetUseCase, ProfileCommentsWidgetSnapshotQuery {
    override fun execute(telegramId: Long, profileId: UUID): TelegramDeliveryResult {
        if (!properties.enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        return when (val result = load(telegramId, profileId, 0)) {
            is ProfileCommentsWidgetSnapshotResult.Available -> publisher.publish(telegramId, result.view)
            ProfileCommentsWidgetSnapshotResult.NotFound -> TelegramDeliveryResult.PermanentFailure("profile-not-found")
            ProfileCommentsWidgetSnapshotResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(
        telegramId: Long,
        profileId: UUID,
        page: Int,
        rootCommentId: UUID?,
    ): ProfileCommentsWidgetSnapshotResult {
        if (telegramId <= 0 || page !in 0..MAX_PAGE) return ProfileCommentsWidgetSnapshotResult.Unavailable
        val profile = when (val found = profiles.get(profileId)) {
            is PublicProfileDetailResult.Available -> found.profile
            PublicProfileDetailResult.NotFound -> return ProfileCommentsWidgetSnapshotResult.NotFound
            PublicProfileDetailResult.Unavailable -> return ProfileCommentsWidgetSnapshotResult.Unavailable
        }
        val result = if (rootCommentId == null) {
            comments.list(telegramId, profileId, page)
        } else {
            comments.replies(telegramId, profileId, rootCommentId, page)
        }
        val data = when (result) {
            is ProfileCommentsPageResult.Available -> result.page
            ProfileCommentsPageResult.NotFound -> return ProfileCommentsWidgetSnapshotResult.NotFound
            ProfileCommentsPageResult.Unavailable -> return ProfileCommentsWidgetSnapshotResult.Unavailable
        }

        val mapped = data.entries.take(PAGE_SIZE).map { entry ->
            val author = when (val found = profiles.get(entry.authorAccountId)) {
                is PublicProfileDetailResult.Available ->
                    WidgetTextLimiter.truncateUtf8(found.profile.nickname, 26)
                else -> "Гравець"
            }
            ProfileCommentWidgetItem(
                id = entry.id,
                author = author,
                content = if (entry.deleted) "Коментар видалено" else WidgetTextLimiter.truncateUtf8(entry.content ?: "", 160),
                deleted = entry.deleted,
                likes = entry.likes,
                replies = entry.replies,
                createdAt = entry.createdAt.toString().take(16).replace('T', ' '),
                canEdit = entry.canEdit,
                canDelete = entry.canDelete,
            )
        }
        return ProfileCommentsWidgetSnapshotResult.Available(
            ProfileCommentsWidgetView(
                profileId = profileId,
                nickname = WidgetTextLimiter.truncateUtf8(profile.nickname, 32),
                page = data.page,
                totalPages = maxOf(1, data.totalPages),
                totalItems = data.totalItems,
                rootCommentId = rootCommentId,
                comments = mapped,
            ),
        )
    }

    private companion object {
        const val MAX_PAGE = 1_000
        const val PAGE_SIZE = 3
    }
}
