package com.mnemos.social.application.spotlight

import com.mnemos.social.application.actor.SocialActorProvider
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.spotlight.port.out.DropSpotlightQueue
import com.mnemos.social.application.spotlight.port.out.DropSpotlightReservation
import com.mnemos.social.application.spotlight.port.out.DropSpotlightWallet
import com.mnemos.social.application.spotlight.port.out.ItemDropLookup
import com.mnemos.social.configuration.spotlight.DropSpotlightProperties
import com.mnemos.social.domain.spotlight.model.DropSpotlight
import com.mnemos.social.domain.spotlight.model.ItemDropSnapshot
import com.mnemos.social.domain.spotlight.repository.DropSpotlightRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class CreateDropSpotlightServiceTest {
    @Test
    fun `creates spotlight and requests wallet debit`() {
        val fixture = Fixture()
        val scheduledAt = fixture.now.plusSeconds(15)

        whenever(fixture.queue.reserve(fixture.now))
            .thenReturn(
                DropSpotlightReservation(
                    sequence = 12,
                    scheduledAt = scheduledAt,
                ),
            )
        whenever(fixture.repository.existsNonFailedByItemDropEventId(fixture.itemDrop.id)).thenReturn(false)
        whenever(fixture.repository.findLatestByBuyerUserId(fixture.actor.userId)).thenReturn(null)
        whenever(fixture.repository.save(any())).thenAnswer { invocation -> invocation.getArgument<DropSpotlight>(0) }

        val result = fixture.service.execute(fixture.itemDrop.id)

        assertEquals(12, result.sequence)
        assertEquals(scheduledAt, result.scheduledAt)
        assertEquals(fixture.actor.userId, result.buyerUserId)
        assertEquals(fixture.actor.accountId, result.sourceAccountId)
        assertEquals(25, result.pricePrime)
        assertNull(result.notificationPack)

        verify(fixture.wallet).requestDebit(result)
    }

    @Test
    fun `rejects queue delay above configured maximum without wallet debit`() {
        val fixture = Fixture()

        whenever(fixture.queue.reserve(fixture.now))
            .thenReturn(
                DropSpotlightReservation(
                    sequence = 12,
                    scheduledAt = fixture.now.plus(fixture.properties.maxQueueDelay).plusSeconds(1),
                ),
            )
        whenever(fixture.repository.existsNonFailedByItemDropEventId(fixture.itemDrop.id)).thenReturn(false)
        whenever(fixture.repository.findLatestByBuyerUserId(fixture.actor.userId)).thenReturn(null)

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                fixture.service.execute(fixture.itemDrop.id)
            }

        assertEquals(HttpStatus.CONFLICT, exception.statusCode)

        verify(fixture.wallet, never()).requestDebit(any())
        verify(fixture.repository, never()).save(any())
    }

    @Test
    fun `checks duplicate spotlight after queue serialization`() {
        val fixture = Fixture()

        whenever(fixture.queue.reserve(fixture.now))
            .thenReturn(
                DropSpotlightReservation(
                    sequence = 12,
                    scheduledAt = fixture.now,
                ),
            )
        whenever(fixture.repository.existsNonFailedByItemDropEventId(fixture.itemDrop.id)).thenReturn(true)

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                fixture.service.execute(fixture.itemDrop.id)
            }

        assertEquals(HttpStatus.CONFLICT, exception.statusCode)

        verify(fixture.queue).reserve(fixture.now)
        verify(fixture.wallet, never()).requestDebit(any())
    }

    @Test
    fun `rejects drop owned by another account`() {
        val fixture =
            Fixture(
                itemDropOwner = "AnotherPlayer",
            )

        val exception =
            assertThrows(ResponseStatusException::class.java) {
                fixture.service.execute(fixture.itemDrop.id)
            }

        assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)

        verify(fixture.queue, never()).reserve(any())
        verify(fixture.wallet, never()).requestDebit(any())
    }

    private class Fixture(
        itemDropOwner: String = "Knalis",
    ) {
        val now: Instant = Instant.parse("2026-08-16T00:00:00Z")
        val actor =
            SocialActor(
                userId = UUID.fromString("019c1ab0-3300-7000-8000-000000000001"),
                accountId = UUID.fromString("019c1ab0-3300-7000-8000-000000000002"),
                cristalixNickname = "Knalis",
            )
        val itemDrop =
            ItemDropSnapshot(
                id = UUID.fromString("019c1ab0-3300-7000-8000-000000000003"),
                itemId = UUID.fromString("019c1ab0-3300-7000-8000-000000000004"),
                itemName = "Elucidator",
                itemRarity = "MYTHICAL",
                itemType = "SWORD",
                droppedFor = itemDropOwner,
                occurredAt = now.minus(Duration.ofMinutes(30)),
            )
        val properties =
            DropSpotlightProperties(
                pricePrime = 25,
                dropMaxAge = Duration.ofHours(2),
                lifetime = Duration.ofHours(1),
                buyerCooldown = Duration.ofMinutes(20),
                globalGap = Duration.ofSeconds(15),
                recipientGap = Duration.ofSeconds(30),
                maxQueueDelay = Duration.ofMinutes(5),
            )
        val actorProvider = mock<SocialActorProvider>()
        val itemDropLookup = mock<ItemDropLookup>()
        val repository = mock<DropSpotlightRepository>()
        val queue = mock<DropSpotlightQueue>()
        val wallet = mock<DropSpotlightWallet>()
        val service =
            CreateDropSpotlightService(
                actorProvider = actorProvider,
                itemDropLookup = itemDropLookup,
                repository = repository,
                queue = queue,
                wallet = wallet,
                properties = properties,
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )

        init {
            whenever(actorProvider.requireActor()).thenReturn(actor)
            whenever(itemDropLookup.findById(itemDrop.id)).thenReturn(itemDrop)
        }
    }
}
