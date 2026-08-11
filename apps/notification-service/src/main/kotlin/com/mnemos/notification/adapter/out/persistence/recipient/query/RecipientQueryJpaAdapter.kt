package com.mnemos.notification.adapter.out.persistence.recipient.query

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.out.persistence.recipient.RecipientJpaRepository
import com.mnemos.notification.application.recipient.model.RecipientView
import com.mnemos.notification.application.recipient.port.out.RecipientReader
import org.springframework.stereotype.Repository

@Repository
class RecipientQueryJpaAdapter(
    private val recipientJpaRepository: RecipientJpaRepository,
) : RecipientReader {
    override fun findByUserIds(userIds: Set<UserId>): Map<UserId, RecipientView> {
        if (userIds.isEmpty()) {
            return emptyMap()
        }

        return recipientJpaRepository
            .findViewsByUserIdIn(
                userIds.mapTo(HashSet()) { it.id },
            ).map { it.toView() }
            .associateBy { it.userId }
    }

    override fun findByTelegramId(telegramId: TelegramId): RecipientView? =
        recipientJpaRepository.findViewByTelegramId(telegramId.id)?.toView()

    private fun RecipientViewProjection.toView(): RecipientView =
        RecipientView(
            userId = UserId(userId),
            telegramId = TelegramId(telegramId),
            priority = priority,
        )
}
