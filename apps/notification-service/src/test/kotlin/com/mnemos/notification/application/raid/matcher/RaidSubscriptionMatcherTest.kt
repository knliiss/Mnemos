package com.mnemos.notification.application.raid.matcher

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.raid.repository.RaidSubscriptionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class RaidSubscriptionMatcherTest {
    private val repository = mock<RaidSubscriptionRepository>()
    private val matcher = RaidSubscriptionMatcher(repository)

    @Test
    fun `matches only event locations covered by each raid subscription`() {
        val allLocationsUser = UserId(UUID.randomUUID())
        val locationTwoUser = UserId(UUID.randomUUID())
        val multipleLocationsUser = UserId(UUID.randomUUID())

        whenever(repository.findAllLocationsSubscriberUserIds())
            .thenReturn(setOf(allLocationsUser))
        whenever(repository.findSubscriberUserIdsByLocations(setOf(1)))
            .thenReturn(emptySet())
        whenever(repository.findSubscriberUserIdsByLocations(setOf(2)))
            .thenReturn(setOf(locationTwoUser, multipleLocationsUser))
        whenever(repository.findSubscriberUserIdsByLocations(setOf(5)))
            .thenReturn(setOf(multipleLocationsUser))

        val result = matcher.match(setOf(1, 2, 5))

        assertEquals(
            mapOf(
                allLocationsUser to setOf(1, 2, 5),
                locationTwoUser to setOf(2),
                multipleLocationsUser to setOf(2, 5),
            ),
            result,
        )
    }

    @Test
    fun `empty raid locations match only all locations subscribers`() {
        val allLocationsUser = UserId(UUID.randomUUID())

        whenever(repository.findAllLocationsSubscriberUserIds())
            .thenReturn(setOf(allLocationsUser))

        val result = matcher.match(emptySet())

        assertEquals(mapOf(allLocationsUser to emptySet<Int>()), result)
        verify(repository, never()).findSubscriberUserIdsByLocations(any())
    }
}
