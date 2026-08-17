package com.mnemos.social.domain.spotlight

import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import com.mnemos.social.domain.spotlight.model.ItemDropSnapshot
import com.mnemos.social.domain.spotlight.model.NotificationPackSelectionSnapshot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class DropSpotlightTest {
    @Test
    fun `moves from pending payment to queued and published`() {
        val createdAt = Instant.parse("2026-08-16T00:00:00Z")
        val spotlight = createSpotlight(createdAt)
        val transactionId = UUID.fromString("019c1ab0-2200-7000-8000-000000000001")
        val paidAt = Instant.parse("2026-08-16T00:00:01Z")
        val publishedAt = Instant.parse("2026-08-16T00:00:15Z")
        val expiresAt = Instant.parse("2026-08-16T01:00:15Z")
        val notificationPack = notificationPack()

        spotlight.paymentCompleted(
            transactionId = transactionId,
            now = paidAt,
        )
        spotlight.publish(
            notificationPack = notificationPack,
            now = publishedAt,
            expiresAt = expiresAt,
        )

        assertEquals(DropSpotlightStatus.PUBLISHED, spotlight.status)
        assertEquals(transactionId, spotlight.walletTransactionId)
        assertEquals(notificationPack, spotlight.notificationPack)
        assertEquals(publishedAt, spotlight.publishedAt)
        assertEquals(expiresAt, spotlight.expiresAt)
        assertEquals(publishedAt, spotlight.updatedAt)
    }

    @Test
    fun `starts without notification pack snapshot`() {
        val spotlight = createSpotlight(Instant.parse("2026-08-16T00:00:00Z"))

        assertNull(spotlight.notificationPack)
    }

    @Test
    fun `ignores duplicate wallet results after first terminal payment result`() {
        val createdAt = Instant.parse("2026-08-16T00:00:00Z")
        val spotlight = createSpotlight(createdAt)
        val firstTransactionId = UUID.fromString("019c1ab0-2200-7000-8000-000000000002")
        val secondTransactionId = UUID.fromString("019c1ab0-2200-7000-8000-000000000003")
        val firstResultAt = Instant.parse("2026-08-16T00:00:01Z")

        spotlight.paymentCompleted(
            transactionId = firstTransactionId,
            now = firstResultAt,
        )
        spotlight.paymentCompleted(
            transactionId = secondTransactionId,
            now = Instant.parse("2026-08-16T00:00:02Z"),
        )
        spotlight.paymentFailed(Instant.parse("2026-08-16T00:00:03Z"))

        assertEquals(DropSpotlightStatus.QUEUED, spotlight.status)
        assertEquals(firstTransactionId, spotlight.walletTransactionId)
        assertEquals(firstResultAt, spotlight.updatedAt)
    }

    @Test
    fun `does not accept payment completion after payment failure`() {
        val createdAt = Instant.parse("2026-08-16T00:00:00Z")
        val spotlight = createSpotlight(createdAt)
        val failedAt = Instant.parse("2026-08-16T00:00:01Z")

        spotlight.paymentFailed(failedAt)
        spotlight.paymentCompleted(
            transactionId = UUID.fromString("019c1ab0-2200-7000-8000-000000000004"),
            now = Instant.parse("2026-08-16T00:00:02Z"),
        )

        assertEquals(DropSpotlightStatus.PAYMENT_FAILED, spotlight.status)
        assertEquals(null, spotlight.walletTransactionId)
        assertEquals(failedAt, spotlight.updatedAt)
    }

    @Test
    fun `rejects publishing before payment completion`() {
        val spotlight = createSpotlight(Instant.parse("2026-08-16T00:00:00Z"))

        assertThrows(IllegalArgumentException::class.java) {
            spotlight.publish(
                notificationPack = notificationPack(),
                now = Instant.parse("2026-08-16T00:00:15Z"),
                expiresAt = Instant.parse("2026-08-16T01:00:15Z"),
            )
        }
    }

    private fun createSpotlight(now: Instant): DropSpotlight =
        DropSpotlight.create(
            sequence = 42,
            buyerUserId = UUID.fromString("019c1ab0-2200-7000-8000-000000000010"),
            sourceAccountId = UUID.fromString("019c1ab0-2200-7000-8000-000000000011"),
            itemDrop =
                ItemDropSnapshot(
                    id = UUID.fromString("019c1ab0-2200-7000-8000-000000000012"),
                    itemId = UUID.fromString("019c1ab0-2200-7000-8000-000000000013"),
                    itemName = "Elucidator",
                    itemRarity = "MYTHICAL",
                    itemType = "SWORD",
                    droppedFor = "Knalis",
                    occurredAt = now.minusSeconds(60),
                ),
            pricePrime = 25,
            scheduledAt = now.plusSeconds(15),
            now = now,
        )

    private fun notificationPack(): NotificationPackSelectionSnapshot =
        NotificationPackSelectionSnapshot(
            personalizationId = UUID.fromString("019c1ab0-2200-7000-8000-000000000014"),
            revision = 5,
            snapshotJson = "{\"mixedAggregateTitle\":\"Drops\",\"templates\":[]}",
        )
}
