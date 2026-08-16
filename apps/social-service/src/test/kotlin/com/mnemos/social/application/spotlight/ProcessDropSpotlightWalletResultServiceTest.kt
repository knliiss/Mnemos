package com.mnemos.social.application.spotlight

import com.mnemos.social.application.spotlight.model.SpotlightWalletResult
import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import com.mnemos.social.domain.spotlight.model.ItemDropSnapshot
import com.mnemos.social.domain.spotlight.repository.DropSpotlightRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class ProcessDropSpotlightWalletResultServiceTest {
    @Test
    fun `persists successful payment transition under row lock`() {
        val now = Instant.parse("2026-08-16T00:00:01Z")
        val repository = mock<DropSpotlightRepository>()
        val spotlight = createSpotlight()
        val transactionId = UUID.fromString("019c1ab0-7700-7000-8000-000000000001")
        val result = matchingResult(spotlight)
        val service = service(repository, now)

        whenever(repository.findByIdForUpdate(spotlight.id)).thenReturn(spotlight)
        whenever(repository.save(spotlight)).thenReturn(spotlight)

        service.completed(
            result = result,
            transactionId = transactionId,
        )

        assertEquals(DropSpotlightStatus.QUEUED, spotlight.status)
        assertEquals(transactionId, spotlight.walletTransactionId)

        verify(repository).findByIdForUpdate(spotlight.id)
        verify(repository).save(spotlight)
    }

    @Test
    fun `does not overwrite already published spotlight with late failed result`() {
        val paidAt = Instant.parse("2026-08-16T00:00:01Z")
        val publishedAt = Instant.parse("2026-08-16T00:00:15Z")
        val repository = mock<DropSpotlightRepository>()
        val spotlight = createSpotlight()
        val service = service(repository, Instant.parse("2026-08-16T00:00:20Z"))

        spotlight.paymentCompleted(
            transactionId = UUID.fromString("019c1ab0-7700-7000-8000-000000000002"),
            now = paidAt,
        )
        spotlight.publish(
            notificationPack = null,
            now = publishedAt,
            expiresAt = publishedAt.plusSeconds(3600),
        )
        whenever(repository.findByIdForUpdate(spotlight.id)).thenReturn(spotlight)

        service.failed(matchingResult(spotlight))

        assertEquals(DropSpotlightStatus.PUBLISHED, spotlight.status)

        verify(repository).findByIdForUpdate(spotlight.id)
        verify(repository, never()).save(spotlight)
    }

    @Test
    fun `rejects wallet result with mismatched request id`() {
        val repository = mock<DropSpotlightRepository>()
        val spotlight = createSpotlight()
        val service = service(repository, Instant.parse("2026-08-16T00:00:01Z"))
        val result =
            matchingResult(spotlight).copy(
                requestId = UUID.fromString("019c1ab0-7700-7000-8000-000000000099"),
            )

        whenever(repository.findByIdForUpdate(spotlight.id)).thenReturn(spotlight)

        assertThrows(IllegalStateException::class.java) {
            service.failed(result)
        }

        assertEquals(DropSpotlightStatus.PENDING_PAYMENT, spotlight.status)

        verify(repository, never()).save(spotlight)
    }

    @Test
    fun `rejects wallet result with mismatched buyer or amount`() {
        val repository = mock<DropSpotlightRepository>()
        val spotlight = createSpotlight()
        val service = service(repository, Instant.parse("2026-08-16T00:00:01Z"))
        val wrongOwner =
            matchingResult(spotlight).copy(
                ownerUserId = UUID.fromString("019c1ab0-7700-7000-8000-000000000098"),
            )
        val wrongAmount = matchingResult(spotlight).copy(amount = spotlight.pricePrime + 1)

        whenever(repository.findByIdForUpdate(spotlight.id)).thenReturn(spotlight)

        assertThrows(IllegalStateException::class.java) {
            service.failed(wrongOwner)
        }
        assertThrows(IllegalStateException::class.java) {
            service.failed(wrongAmount)
        }

        verify(repository, never()).save(spotlight)
    }

    private fun matchingResult(spotlight: DropSpotlight): SpotlightWalletResult =
        SpotlightWalletResult(
            requestId = spotlight.walletRequestId,
            spotlightId = spotlight.id.id,
            ownerUserId = spotlight.buyerUserId,
            amount = spotlight.pricePrime,
        )

    private fun service(
        repository: DropSpotlightRepository,
        now: Instant,
    ): ProcessDropSpotlightWalletResultService =
        ProcessDropSpotlightWalletResultService(
            repository = repository,
            clock = Clock.fixed(now, ZoneOffset.UTC),
        )

    private fun createSpotlight(): DropSpotlight {
        val createdAt = Instant.parse("2026-08-16T00:00:00Z")

        return DropSpotlight.create(
            sequence = 1,
            buyerUserId = UUID.fromString("019c1ab0-7700-7000-8000-000000000010"),
            sourceAccountId = UUID.fromString("019c1ab0-7700-7000-8000-000000000011"),
            itemDrop =
                ItemDropSnapshot(
                    id = UUID.fromString("019c1ab0-7700-7000-8000-000000000012"),
                    itemId = UUID.fromString("019c1ab0-7700-7000-8000-000000000013"),
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
    }
}
