package com.mnemos.notification.application.ping

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.model.PingNotificationPayload
import com.mnemos.notification.application.notification.service.NotificationDispatcher
import com.mnemos.notification.application.ping.port.out.PingDeduplicationStore
import com.mnemos.notification.application.ping.port.out.PingRecipientResolver
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.UUID

@Service
class ProcessPingNotificationService(
    private val recipientResolver: PingRecipientResolver,
    private val preferenceService: PingNotificationPreferenceService,
    private val deduplicationStore: PingDeduplicationStore,
    private val dispatcher: NotificationDispatcher,
) {
    fun process(command: ProcessPingNotificationCommand) {
        if (!deduplicationStore.accept(command.sender, command.message)) {
            return
        }

        val targetsByUser = linkedMapOf<UserId, String>()

        command.mentions
            .distinctBy(String::lowercase)
            .forEach { mention ->
                recipientResolver.resolve(mention).forEach { userId ->
                    targetsByUser.putIfAbsent(userId, mention)
                }
            }

        val enabledUsers = preferenceService.filterEnabled(targetsByUser.keys)

        enabledUsers.forEach { userId ->
            val target = requireNotNull(targetsByUser[userId])
            dispatcher.dispatch(
                sourceId = deliverySourceId(command.messageId, userId),
                recipientIds = setOf(userId),
                payload =
                    PingNotificationPayload(
                        player = command.sender,
                        message = command.message,
                        text = command.text,
                        target = target,
                    ),
                occurredAt = command.observedAt,
            )
        }
    }

    private fun deliverySourceId(
        messageId: UUID,
        userId: UserId,
    ): UUID =
        UUID.nameUUIDFromBytes(
            (messageId.toString() + ":" + userId.id).toByteArray(StandardCharsets.UTF_8),
        )
}

data class ProcessPingNotificationCommand(
    val messageId: UUID,
    val observedAt: Instant,
    val sender: String,
    val message: String,
    val text: String,
    val mentions: List<String>,
)
