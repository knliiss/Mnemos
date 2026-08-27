package com.mnemos.event.adapter.out.persistence.leaderboard

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

class BoosterLeaderboardJpaAdapterTest {
    private val repository = mock<BoosterLeaderboardJpaRepository>()
    private val playerIdentityRepository = mock<PlayerIdentityProjectionRepository>()
    private val adapter =
        BoosterLeaderboardJpaAdapter(
            repository = repository,
            playerIdentityRepository = playerIdentityRepository,
        )

    @Test
    fun `keeps historical leaderboard row when current nickname enrichment is missing`() {
        val playerId = UUID.fromString("019c1156-3274-7000-8000-000000000701")
        val lastActivatedAt = Instant.parse("2026-08-27T15:30:00Z")
        val projection = projection(playerId = playerId, nickname = "Historical", lastActivatedAt = lastActivatedAt)

        whenever(
            repository.read(
                from = any(),
                to = anyOrNull(),
                limit = any(),
            ),
        ).thenReturn(listOf(projection))
        whenever(playerIdentityRepository.findCurrentNickname(CristalixPlayerId(playerId))).thenReturn(null)

        val result =
            adapter.read(
                from = Instant.parse("2026-08-01T00:00:00Z"),
                to = null,
                limit = 10,
            )

        assertEquals(1, result.size)
        assertEquals(CristalixPlayerId(playerId), result.single().playerId)
        assertEquals("Historical", result.single().nickname)
        assertEquals(4, result.single().totalBoosters)
    }

    private fun projection(
        playerId: UUID,
        nickname: String,
        lastActivatedAt: Instant,
    ): BoosterLeaderboardProjection {
        val projection = mock<BoosterLeaderboardProjection>()

        whenever(projection.getPlayerId()).thenReturn(playerId)
        whenever(projection.getNickname()).thenReturn(nickname)
        whenever(projection.getTotalBoosters()).thenReturn(4)
        whenever(projection.getLastActivatedAt()).thenReturn(lastActivatedAt)

        return projection
    }
}
