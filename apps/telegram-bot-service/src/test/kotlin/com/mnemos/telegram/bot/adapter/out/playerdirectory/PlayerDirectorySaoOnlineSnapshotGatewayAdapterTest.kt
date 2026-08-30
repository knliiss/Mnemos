package com.mnemos.telegram.bot.adapter.out.playerdirectory

import com.mnemos.contracts.playerdirectory.internal.v1.InternalSaoRealmPlayerResponse
import com.mnemos.contracts.playerdirectory.internal.v1.InternalSaoRealmResponse
import com.mnemos.contracts.playerdirectory.internal.v1.InternalSaoRealmSnapshotResponse
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshotResult
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class PlayerDirectorySaoOnlineSnapshotGatewayAdapterTest {
    @Test
    fun `gateway maps available snapshot and orders realms`() {
        val generatedAt = Instant.parse("2026-08-30T12:24:00Z")
        val playerId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val response =
            InternalSaoRealmSnapshotResponse(
                generatedAt = generatedAt,
                trackedOnline = 1,
                realms =
                    listOf(
                        InternalSaoRealmResponse(
                            name = "SAO-2",
                            number = 2,
                            playerCount = 0,
                            players = emptyList(),
                        ),
                        InternalSaoRealmResponse(
                            name = "SAO-1",
                            number = 1,
                            playerCount = 1,
                            players =
                                listOf(
                                    InternalSaoRealmPlayerResponse(
                                        playerId = playerId,
                                        nickname = "knaliz",
                                        accountId = null,
                                        observedAt = generatedAt,
                                    ),
                                ),
                        ),
                    ),
            )
        val gateway = PlayerDirectorySaoOnlineSnapshotGatewayAdapter(FakePlayerDirectoryServiceClient(response))

        val result = gateway.getSnapshot() as SaoOnlineSnapshotResult.Available

        assertEquals(generatedAt, result.snapshot.generatedAt)
        assertEquals(1, result.snapshot.trackedOnline)
        assertEquals(listOf("SAO-1", "SAO-2"), result.snapshot.realms.map { realm -> realm.name })
        assertEquals(playerId, result.snapshot.realms.first().players.single().playerId)
        assertEquals("knaliz", result.snapshot.realms.first().players.single().nickname)
    }

    @Test
    fun `gateway returns unavailable when player directory call fails`() {
        val gateway = PlayerDirectorySaoOnlineSnapshotGatewayAdapter(FailingPlayerDirectoryServiceClient())

        val result = gateway.getSnapshot()

        assertSame(SaoOnlineSnapshotResult.Unavailable, result)
    }

    private class FakePlayerDirectoryServiceClient(
        private val response: InternalSaoRealmSnapshotResponse,
    ) : PlayerDirectoryServiceClient {
        override fun getSaoSnapshot(): InternalSaoRealmSnapshotResponse = response
    }

    private class FailingPlayerDirectoryServiceClient : PlayerDirectoryServiceClient {
        override fun getSaoSnapshot(): InternalSaoRealmSnapshotResponse =
            error("player-directory-service unavailable")
    }
}
