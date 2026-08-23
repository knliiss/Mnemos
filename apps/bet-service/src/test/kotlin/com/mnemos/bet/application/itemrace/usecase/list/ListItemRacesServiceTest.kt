package com.mnemos.bet.application.itemrace.usecase.list

import com.mnemos.bet.application.account.model.AccountSnapshot
import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.itemrace.model.ItemRaceParticipantView
import com.mnemos.bet.application.itemrace.model.ItemRaceView
import com.mnemos.bet.application.itemrace.port.out.ItemRaceReader
import com.mnemos.bet.domain.itemrace.model.DomainItemType
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantStatus
import com.mnemos.bet.domain.itemrace.model.ItemRaceStatus
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class ListItemRacesServiceTest {
    private val reader = mock<ItemRaceReader>()
    private val accountProvider = mock<AccountProvider>()
    private val service =
        ListItemRacesService(
            reader = reader,
            accountProvider = accountProvider,
        )

    @Test
    fun `current races include active account position`() {
        val accountId = AccountId(UUID.fromString("019c1e23-5000-7000-8000-000000000001"))
        val firstRace = createRace("019c1e23-5000-7000-8000-000000000002")
        val secondRace = createRace("019c1e23-5000-7000-8000-000000000003")
        val participant =
            createParticipant(
                id = "019c1e23-5000-7000-8000-000000000004",
                raceId = firstRace.id,
                accountId = accountId,
                status = ItemRaceParticipantStatus.ACTIVE,
            )

        whenever(accountProvider.requireActiveAccount()).thenReturn(
            AccountSnapshot(
                id = accountId,
                nickname = "knaliz",
            ),
        )
        whenever(reader.findCurrent()).thenReturn(listOf(firstRace, secondRace))
        whenever(reader.findOngoingParticipants(accountId)).thenReturn(listOf(participant))

        val result = service.execute()

        assertEquals(2, result.size)
        assertEquals(listOf(participant), result[0].participants)
        assertTrue(result[1].participants.isEmpty())
        verify(reader).findOngoingParticipants(accountId)
    }

    @Test
    fun `joined ongoing race remains visible when current query omits it`() {
        val accountId = AccountId(UUID.fromString("019c1e23-5000-7000-8000-000000000011"))
        val race =
            createRace(
                id = "019c1e23-5000-7000-8000-000000000012",
                status = ItemRaceStatus.REFUNDING,
            )
        val participant =
            createParticipant(
                id = "019c1e23-5000-7000-8000-000000000013",
                raceId = race.id,
                accountId = accountId,
                status = ItemRaceParticipantStatus.REFUND_PENDING,
            )

        whenever(accountProvider.requireActiveAccount()).thenReturn(
            AccountSnapshot(
                id = accountId,
                nickname = "knaliz",
            ),
        )
        whenever(reader.findCurrent()).thenReturn(emptyList())
        whenever(reader.findOngoingParticipants(accountId)).thenReturn(listOf(participant))
        whenever(reader.find(race.id)).thenReturn(race)

        val result = service.execute()

        assertEquals(1, result.size)
        assertEquals(race, result.single().race)
        assertEquals(listOf(participant), result.single().participants)
        verify(reader).find(race.id)
    }

    private fun createParticipant(
        id: String,
        raceId: ItemRaceId,
        accountId: AccountId,
        status: ItemRaceParticipantStatus,
    ): ItemRaceParticipantView =
        ItemRaceParticipantView(
            id = ItemRaceParticipantId(UUID.fromString(id)),
            raceId = raceId,
            accountId = accountId,
            accountNickname = "knaliz",
            confirmedAmount = 1_000,
            pendingAmount = 0,
            status = status,
        )

    private fun createRace(
        id: String,
        status: ItemRaceStatus = ItemRaceStatus.REGISTRATION,
    ): ItemRaceView =
        ItemRaceView(
            id = ItemRaceId(UUID.fromString(id)),
            itemName = "Eternal Blade",
            itemType = DomainItemType.SWORD,
            registrationClosesAt = Instant.parse("2026-08-23T12:00:00Z"),
            startsAt = Instant.parse("2026-08-23T12:05:00Z"),
            expiresAt = Instant.parse("2026-08-24T12:05:00Z"),
            entryAmount = 1_000,
            feePercent = 5,
            status = status,
            winnerAccountId = null,
        )
}
