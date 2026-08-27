package com.mnemos.event.adapter.out.persistence.leaderboard

import com.mnemos.event.application.leaderboard.drop.DropLeaderboardDirection
import com.mnemos.event.application.leaderboard.drop.DropLeaderboardQuery
import com.mnemos.event.application.leaderboard.drop.DropLeaderboardSort
import com.mnemos.event.application.playeridentity.port.out.PlayerIdentityProjectionRepository
import com.mnemos.event.domain.player.model.CristalixPlayerId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class DropLeaderboardJpaAdapterTest {
    private val repository = mock<DropLeaderboardJpaRepository>()
    private val playerIdentityRepository = mock<PlayerIdentityProjectionRepository>()
    private val adapter =
        DropLeaderboardJpaAdapter(
            repository = repository,
            playerIdentityRepository = playerIdentityRepository,
        )

    @Test
    fun `keeps historical leaderboard row when current nickname enrichment is missing`() {
        val playerId = UUID.fromString("019c1156-3274-7000-8000-000000000601")
        val lastDropAt = Instant.parse("2026-08-27T15:30:00Z")
        val projection = projection(playerId = playerId, nickname = "Historical", lastDropAt = lastDropAt)

        whenever(
            repository.read(
                from = any(),
                to = anyOrNull(),
                rarity = anyOrNull(),
                itemType = anyOrNull(),
                itemName = anyOrNull(),
                playerId = anyOrNull(),
                sort = any(),
                direction = any(),
                limit = any(),
            ),
        ).thenReturn(listOf(projection))
        whenever(playerIdentityRepository.findCurrentNickname(CristalixPlayerId(playerId))).thenReturn(null)

        val result =
            adapter.read(
                from = Instant.parse("2026-08-01T00:00:00Z"),
                to = null,
                query = query(),
            )

        assertEquals(1, result.size)
        assertEquals(CristalixPlayerId(playerId), result.single().playerId)
        assertEquals("Historical", result.single().nickname)
        assertEquals(7, result.single().totalDrops)
    }

    @Test
    fun `uses current nickname when enrichment is available`() {
        val playerId = UUID.fromString("019c1156-3274-7000-8000-000000000602")
        val projection =
            projection(
                playerId = playerId,
                nickname = "Historical",
                lastDropAt = Instant.parse("2026-08-27T15:30:00Z"),
            )

        whenever(
            repository.read(
                from = any(),
                to = anyOrNull(),
                rarity = anyOrNull(),
                itemType = anyOrNull(),
                itemName = anyOrNull(),
                playerId = anyOrNull(),
                sort = any(),
                direction = any(),
                limit = any(),
            ),
        ).thenReturn(listOf(projection))
        whenever(playerIdentityRepository.findCurrentNickname(CristalixPlayerId(playerId))).thenReturn("Current")

        val result =
            adapter.read(
                from = Instant.parse("2026-08-01T00:00:00Z"),
                to = null,
                query = query(),
            )

        assertEquals("Current", result.single().nickname)
    }

    private fun query(): DropLeaderboardQuery =
        DropLeaderboardQuery(
            wipeId = null,
            rarity = null,
            itemType = null,
            itemName = null,
            nickname = null,
            sort = DropLeaderboardSort.TOTAL,
            direction = DropLeaderboardDirection.DESC,
            limit = 10,
        )

    private fun projection(
        playerId: UUID,
        nickname: String,
        lastDropAt: Instant,
    ): DropLeaderboardProjection {
        val projection = mock<DropLeaderboardProjection>()

        whenever(projection.getPlayerId()).thenReturn(playerId)
        whenever(projection.getNickname()).thenReturn(nickname)
        whenever(projection.getTotalDrops()).thenReturn(7)
        whenever(projection.getMythicalDrops()).thenReturn(2)
        whenever(projection.getSecretDrops()).thenReturn(1)
        whenever(projection.getLastDropAt()).thenReturn(lastDropAt)

        return projection
    }
}
