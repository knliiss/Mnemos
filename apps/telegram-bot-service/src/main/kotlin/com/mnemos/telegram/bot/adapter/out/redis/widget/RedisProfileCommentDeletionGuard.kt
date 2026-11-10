package com.mnemos.telegram.bot.adapter.out.redis.widget

import com.mnemos.telegram.bot.application.widget.profilecomments.port.out.ProfileCommentDeletionGuard
import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class RedisProfileCommentDeletionGuard(
    private val redis: StringRedisTemplate,
    private val bot: TelegramBotProperties,
) : ProfileCommentDeletionGuard {
    override fun request(
        telegramId: Long,
        messageId: Int,
        profileId: UUID,
        commentId: UUID,
        actorAccountId: UUID,
    ) {
        redis.opsForValue().set(
            key(telegramId, messageId),
            "$profileId:$commentId:$actorAccountId",
            Duration.ofMinutes(3),
        )
    }

    override fun consume(
        telegramId: Long,
        messageId: Int,
        profileId: UUID,
        commentId: UUID,
        actorAccountId: UUID,
    ): Boolean =
        redis.opsForValue().getAndDelete(key(telegramId, messageId)) ==
            "$profileId:$commentId:$actorAccountId"

    override fun cancel(telegramId: Long, messageId: Int) {
        redis.delete(key(telegramId, messageId))
    }

    private fun key(telegramId: Long, messageId: Int): String =
        "telegram-bot:widget-comment-deletion:" + bot.token.substringBefore(':') +
            ":" + telegramId + ":" + messageId
}
