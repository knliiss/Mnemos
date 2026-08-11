package com.mnemos.notification.application.booster.matcher

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.booster.repository.BoosterSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BoosterSubscriptionMatcher(
    val repository: BoosterSubscriptionRepository,
) {
    @Transactional(readOnly = true)
    fun match(boosterType: DomainBoosterType): Set<UserId> = repository.findMatchingUserIds(boosterType)
}
