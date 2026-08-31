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
    fun match(locations: Set<Int>): Map<UserId, Set<Int>> {
        val matchedLocationsByUser = linkedMapOf<UserId, Set<Int>>()

        repository.findAllLocationsSubscriberUserIds().forEach { userId ->
            matchedLocationsByUser[userId] = locations
        }

        locations.sorted().forEach { location ->
            repository.findSubscriberUserIdsByLocations(setOf(location)).forEach { userId ->
                val currentLocations = matchedLocationsByUser[userId].orEmpty()
                matchedLocationsByUser[userId] = currentLocations + location
            }
        }

        return matchedLocationsByUser
    }
}
