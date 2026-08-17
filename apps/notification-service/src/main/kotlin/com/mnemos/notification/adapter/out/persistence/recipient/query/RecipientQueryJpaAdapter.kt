package com.mnemos.notification.adapter.out.persistence.recipient.query

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.out.persistence.moderation.NotificationSuspensionRepository
import com.mnemos.notification.adapter.out.persistence.recipient.RecipientJpaRepository
import com.mnemos.notification.adapter.out.persistence.recipient.query.RecipientQueryMapper.toView
import com.mnemos.notification.application.recipient.model.RecipientView
import com.mnemos.notification.application.recipient.port.out.RecipientReader
import org.springframework.stereotype.Repository

@Repository
class RecipientQueryJpaAdapter(
    private val recipientJpaRepository: RecipientJpaRepository,
    private val suspensionRepository: NotificationSuspensionRepository,
) : RecipientReader {
    override fun findByUserIds(userIds: Set<UserId>): Map<UserId, RecipientView> {
        if (userIds.isEmpty()) {
            return emptyMap()
        }

        return recipientJpaRepository
            .findViewsByUserIdIn(userIds.mapTo(HashSet()) { it.id })
            .filterNot { suspensionRepository.isSuspended(it.userId) }
            .map { it.toView() }
            .associateBy { it.userId }
    }

    override fun findByTelegramId(telegramId: TelegramId): RecipientView? =
        recipientJpaRepository.findActiveViewByTelegramId(telegramId.id)?.toView()
}
