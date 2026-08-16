package com.mnemos.social.application.spotlight

import com.mnemos.social.application.spotlight.port.out.DropSpotlightEventPublisher
import com.mnemos.social.application.spotlight.port.out.NotificationPackSnapshotProvider
import com.mnemos.social.configuration.spotlight.DropSpotlightProperties
import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.ItemDropSnapshot
import com.mnemos.social.domain.spotlight.model.NotificationPackSelectionSnapshot
import com.mnemos.social.domain.spotlight.repository.DropSpotlightRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class PublishDueDropSpotlightsServiceTest {
    @Test
    fun `snapshots active notification pack when spotlight is published`() {
        val now = Instant.parse("2026-08-16T00:00:15Z")
        val repository = mock<DropSpotlightRepository>()
        val snapshotProvider = mock<NotificationPackSnapshotProvider>()
        val publisher = mock<DropSpotlightEventPublisher>()
        val properties = properties()
        val spotlight = queuedSpotlight()
        val notificationPack = notificationPack()
        val service =
            PublishDueDropSpotlightsService(
                repository = repository,
                notificationPackSnapshotProvider = snapshotProvider,
                publisher = publisher,
                properties = properties,
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )

        whenever(repository.findDue(now)).thenReturn(listOf(spotlight))
        whenever(snapshotProvider.findActive(spotlight.buyerUserId)).thenReturn(notificationPack)
        whenever(repository.save(any())).thenAnswer { invocation -> invocation.getArgument<DropSpotlight>(0) }

        service.execute()

        assertEquals(notificationPack, spotlight.notificationPack)
        assertEquals(now, spotlight.publishedAt)
        assertEquals(now.plus(properties.lifetime), spotlight.expiresAt)

        verify(snapshotProvider).findActive(spotlight.buyerUserId)
        verify(repository).save(spotlight)
        verify(publisher).publish(spotlight)
    }

    private fun queuedSpotlight(): DropSpotlight {
        val createdAt = Instant.parse("2026-08-16T00:00:00Z")
        val spotlight =
            DropSpotlight.create(
                sequence = 21,
                buyerUserId = UUID.fromString("019c1ab0-6600-7000-8000-000000000001"),
                sourceAccountId = UUID.fromString("019c1ab0-6600-7000-8000-000000000002"),
                itemDrop =
                    ItemDropSnapshot(
                        id = UUID.fromString("019c1ab0-6600-7000-8000-000000000003"),
                        itemId = UUID.fromString("019c1ab0-6600-7000-8000-000000000004"),
                        itemName = "Elucidator",
                        itemRarity = "MYTHICAL",
                        itemType = "SWORD",
                        droppedFor = "Knalis",
                        occurredAt = createdAt.minusSeconds(60),
                    ),
                pricePrime = 25,
                scheduledAt = createdAt.plusSeconds(15),
                now = createdAt,
            )

        spotlight.paymentCompleted(
            transactionId = UUID.fromString("019c1ab0-6600-7000-8000-000000000005"),
            now = createdAt.plusSeconds(1),
        )

        return spotlight
    }

    private fun notificationPack(): NotificationPackSelectionSnapshot =
        NotificationPackSelectionSnapshot(
            personalizationId = UUID.fromString("019c1ab0-6600-7000-8000-000000000006"),
            revision = 9,
            snapshotJson = "{\"mixedAggregateTitle\":\"Drops\",\"templates\":[]}",
        )

    private fun properties(): DropSpotlightProperties =
        DropSpotlightProperties(
            pricePrime = 25,
            dropMaxAge = Duration.ofHours(2),
            lifetime = Duration.ofHours(1),
            buyerCooldown = Duration.ofMinutes(20),
            globalGap = Duration.ofSeconds(15),
            recipientGap = Duration.ofSeconds(30),
            maxQueueDelay = Duration.ofMinutes(5),
        )
}
