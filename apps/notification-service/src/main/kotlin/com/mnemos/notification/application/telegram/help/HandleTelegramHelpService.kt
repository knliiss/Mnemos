package com.mnemos.notification.application.telegram.help

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.notification.delivery.port.out.MessageDeliveryGateway
import com.mnemos.notification.application.notification.delivery.port.out.MessageEditGateway
import com.mnemos.notification.application.notification.delivery.port.out.NotificationMessageRenderer
import com.mnemos.notification.domain.recipient.repository.RecipientRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(
    prefix = "mnemos.notification.telegram",
    name = ["enabled"],
    havingValue = "true",
)
class HandleTelegramHelpService(
    private val renderer: NotificationMessageRenderer,
    private val deliveryGateway: MessageDeliveryGateway,
    private val editGateway: MessageEditGateway,
    private val recipientRepository: RecipientRepository,
) {
    fun send(
        telegramId: Long,
        section: TelegramHelpSection = TelegramHelpSection.OVERVIEW,
    ): DeliveryResult {
        val recipientTelegramId = TelegramId(telegramId)

        recipientRepository.activateByTelegramId(recipientTelegramId)

        return deliveryGateway.deliver(
            telegramId = recipientTelegramId,
            message = renderer.renderHelp(section),
        )
    }

    fun switchSection(
        telegramId: Long,
        messageId: Int,
        section: TelegramHelpSection,
    ): DeliveryResult {
        val recipientTelegramId = TelegramId(telegramId)

        recipientRepository.activateByTelegramId(recipientTelegramId)

        return editGateway.edit(
            telegramId = recipientTelegramId,
            messageId = messageId,
            message = renderer.renderHelp(section),
        )
    }
}
