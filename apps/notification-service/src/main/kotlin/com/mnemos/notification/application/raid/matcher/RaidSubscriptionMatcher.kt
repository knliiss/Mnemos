package com.mnemos.notification.application.raid.matcher

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.raid.repository.RaidSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaidSubscriptionMatcher(
    private val repository: RaidSubscriptionRepository,
) {
    @Transactional(readOnly = true)
    fun match(locations: Set<Int>): Set<UserId> {
        val allLocationsSubscribers = repository.findAllLocationsSubscriberUserIds()

        if (locations.isEmpty()) {
            return allLocationsSubscribers
        }

        return allLocationsSubscribers + repository.findSubscriberUserIdsByLocations(locations)
    }
}
