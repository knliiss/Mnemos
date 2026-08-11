package com.mnemos.notification.application.global.matcher

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import com.mnemos.notification.domain.global.repository.GlobalSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GlobalSubscriptionMatcher(
    val repository: GlobalSubscriptionRepository,
) {
    @Transactional(readOnly = true)
    fun match(eventType: DomainGlobalEventType): Set<UserId> = repository.findMatchingUserIds(eventType)
}
