package com.mnemos.notification.application.itemdrop.matcher

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.itemdrop.repository.ItemDropSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ItemDropSubscriptionMatcher(
    val repository: ItemDropSubscriptionRepository,
) {
    @Transactional(readOnly = true)
    fun match(
        droppedFor: String,
        itemName: String,
    ): Set<UserId> =
        repository.findMatchingUserIds(
            droppedFor = droppedFor,
            itemName = itemName,
        )
}
