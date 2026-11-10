package com.mnemos.telegram.bot.adapter.out.redis.widget

import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.configuration.telegram.TelegramBotProperties
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class RedisWidgetTextInputSessionRepository(
    private val redis: StringRedisTemplate,
    private val telegram: TelegramBotProperties,
    private val widgets: TelegramWidgetProperties,
) : WidgetTextInputSessionRepository {
    override fun find(telegramId: Long, promptMessageId: Int): WidgetTextInputSession? {
        val value = redis.opsForValue().get(key(telegramId, promptMessageId)) ?: return null
        val parts = value.split('|')
        if (!((parts.size == 4 && parts[0] == "v1") ||
            (parts.size == 5 && parts[0] == "v2") ||
            (parts.size == 9 && parts[0] == "v3") ||
            (parts.size == 10 && parts[0] == "v4")
        )) return null
        val widgetId = parts[1].toIntOrNull()?.takeIf { it > 0 } ?: return null
        val accountId = if (parts[2] == "-") null else runCatching { UUID.fromString(parts[2]) }.getOrNull()
        if (parts[2] != "-" && accountId == null) return null
        val purpose = runCatching { WidgetTextInputPurpose.valueOf(parts[3]) }.getOrNull() ?: return null
        val targetRoleId = if (parts.size == 5 && parts[4] != "-") {
            runCatching { UUID.fromString(parts[4]) }.getOrNull() ?: return null
        } else null
        if (purpose == WidgetTextInputPurpose.ACCOUNT_ROLE_EDIT && targetRoleId == null) return null
        val commentPurpose = purpose == WidgetTextInputPurpose.PROFILE_COMMENT_CREATE ||
            purpose == WidgetTextInputPurpose.PROFILE_COMMENT_REPLY ||
            purpose == WidgetTextInputPurpose.PROFILE_COMMENT_EDIT
        if (commentPurpose && (parts.size != 9 && parts.size != 10)) return null
        if (purpose == WidgetTextInputPurpose.PROFILE_COMMENT_EDIT && parts.size != 10) return null
        fun optionalId(raw: String): UUID? =
            if (raw == "-") null else runCatching { UUID.fromString(raw) }.getOrNull()
        val profileId = parts.getOrNull(5)?.let(::optionalId)
        val parentId = parts.getOrNull(6)?.let(::optionalId)
        val actorId = parts.getOrNull(7)?.let(::optionalId)
        val mutationId = parts.getOrNull(8)?.let(::optionalId)
        val rootId = parts.getOrNull(9)?.let(::optionalId)
        if (commentPurpose && (profileId == null || actorId == null || mutationId == null ||
            (purpose == WidgetTextInputPurpose.PROFILE_COMMENT_REPLY && parentId == null) ||
            (purpose == WidgetTextInputPurpose.PROFILE_COMMENT_CREATE && parentId != null) ||
            (purpose == WidgetTextInputPurpose.PROFILE_COMMENT_EDIT && parentId == null)
        )) return null
        return WidgetTextInputSession(
            telegramId, promptMessageId, widgetId, accountId, purpose,
            targetRoleId, profileId, parentId, actorId, mutationId, rootId,
        )
    }

    override fun save(session: WidgetTextInputSession) {
        val commentPurpose = session.purpose == WidgetTextInputPurpose.PROFILE_COMMENT_CREATE ||
            session.purpose == WidgetTextInputPurpose.PROFILE_COMMENT_REPLY ||
            session.purpose == WidgetTextInputPurpose.PROFILE_COMMENT_EDIT
        val value = if (commentPurpose) {
            require(session.targetProfileId != null &&
                session.expectedActorAccountId != null && session.mutationId != null)
            if (session.purpose == WidgetTextInputPurpose.PROFILE_COMMENT_EDIT) {
                require(session.targetCommentId != null)
                listOf(
                    "v4", session.widgetMessageId, session.accountId ?: "-",
                    session.purpose.name, "-", session.targetProfileId, session.targetCommentId,
                    session.expectedActorAccountId, session.mutationId, session.threadRootId ?: "-",
                ).joinToString("|")
            } else {
                listOf(
                    "v3", session.widgetMessageId, session.accountId ?: "-",
                    session.purpose.name, "-", session.targetProfileId,
                    session.targetCommentId ?: "-", session.expectedActorAccountId, session.mutationId,
                ).joinToString("|")
            }
        } else {
            listOf(
                "v2", session.widgetMessageId, session.accountId ?: "-",
                session.purpose.name, session.targetRoleId ?: "-",
            ).joinToString("|")
        }
        val ttl = minOf(widgets.sessionTtl, Duration.ofMinutes(10))
        redis.opsForValue().set(key(session.telegramId, session.promptMessageId), value, ttl)
    }

    override fun claimMutation(telegramId: Long, promptMessageId: Int): Boolean =
        redis.opsForValue().setIfAbsent(
            key(telegramId, promptMessageId) + ":claimed",
            "1",
            minOf(widgets.sessionTtl, Duration.ofMinutes(10)),
        ) == true

    override fun delete(telegramId: Long, promptMessageId: Int) {
        redis.delete(key(telegramId, promptMessageId))
    }

    private fun key(telegramId: Long, promptMessageId: Int): String {
        val botId = telegram.token.substringBefore(':')
        return "telegram-bot:widget-input:" + botId + ":" + telegramId + ":" + promptMessageId
    }
}
