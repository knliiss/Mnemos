package com.mnemos.notification.application.telegram.start

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.notification.delivery.port.out.MessageDeliveryGateway
import com.mnemos.notification.application.notification.delivery.port.out.NotificationMessageRenderer
import com.mnemos.notification.application.recipient.port.out.RecipientReader
import com.mnemos.notification.domain.recipient.repository.RecipientRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(
    prefix = "mnemos.notification.telegram",
    name = ["enabled"],
    havingValue = "true",
)
class HandleTelegramStartService(
    private val renderer: NotificationMessageRenderer,
    private val gateway: MessageDeliveryGateway,
    private val recipientReader: RecipientReader,
    private val recipientRepository: RecipientRepository,
) {
    fun handle(context: TelegramStartContext): DeliveryResult {
        val telegramId = TelegramId(context.telegramId)

        recipientRepository.activateByTelegramId(telegramId)

        val resolvedContext =
            context.copy(
                userId = recipientReader.findByTelegramId(telegramId)?.userId,
            )
        val message = renderer.renderStart(resolvedContext)

        return gateway.deliver(
            telegramId = telegramId,
            message = message,
        )
    }
}
