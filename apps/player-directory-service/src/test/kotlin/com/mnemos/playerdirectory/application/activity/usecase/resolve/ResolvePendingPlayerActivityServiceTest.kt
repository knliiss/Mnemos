package com.mnemos.playerdirectory.application.activity.usecase.resolve

import com.mnemos.playerdirectory.application.activity.model.PendingPlayerActivityLease
import com.mnemos.playerdirectory.application.activity.support.PendingPlayerActivityCompletionCoordinator
import com.mnemos.playerdirectory.application.activity.usecase.claim.ClaimPendingPlayerActivityUseCase
import com.mnemos.playerdirectory.application.player.model.PlayerNicknameIdentityBatch
import com.mnemos.playerdirectory.application.player.model.PlayerObservationBatch
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerBatchGateway
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerNameBatchGateway
import com.mnemos.playerdirectory.application.player.port.out.PlayerNicknameOwnershipRepository
import com.mnemos.playerdirectory.application.player.support.PlayerObservationCoordinator
import com.mnemos.playerdirectory.application.tracking.usecase.activity.RecordPlayerDropActivityCommand
import com.mnemos.playerdirectory.application.tracking.usecase.activity.RecordPlayerDropActivityUseCase
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerObservation
import com.mnemos.playerdirectory.domain.player.model.PlayerPresence
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class ResolvePendingPlayerActivityServiceTest {
    @Test
    fun `batch resolution materializes player before recording drop activity`() {
        val nickname = PlayerNickname.of("CRAFTER12251225")
        val playerId = CristalixPlayerId(UUID.fromString("30b1b6cc-2c69-11e8-b5ea-1cb72caa35fd"))
        val occurredAt = Instant.parse("2026-08-27T18:10:00Z")
        val observedAt = Instant.parse("2026-08-27T18:11:00Z")
        val lease =
            PendingPlayerActivityLease(
                nickname = nickname,
                pendingDropCount = 2,
                lastDropAt = occurredAt,
                leaseUntil = Instant.parse("2026-08-27T18:12:00Z"),
            )
        val observation =
            PlayerObservation(
                playerId = playerId,
                nickname = nickname,
                externalVersion = 1,
                externalUpdatedAt = observedAt,
                presence =
                    PlayerPresence(
                        status = PlayerPresenceStatus.OFFLINE,
                        realm = null,
                        realmType = null,
                        privacyHidden = false,
                        lastSeenOnline = null,
                        observedAt = observedAt,
                    ),
            )
        val claimPendingPlayerActivityUseCase = mock<ClaimPendingPlayerActivityUseCase>()
        val nicknameOwnershipRepository = mock<PlayerNicknameOwnershipRepository>()
        val cristalixPlayerNameBatchGateway = mock<CristalixPlayerNameBatchGateway>()
        val cristalixPlayerBatchGateway = mock<CristalixPlayerBatchGateway>()
        val playerObservationCoordinator = mock<PlayerObservationCoordinator>()
        val recordPlayerDropActivityUseCase = mock<RecordPlayerDropActivityUseCase>()
        val completionCoordinator = mock<PendingPlayerActivityCompletionCoordinator>()

        whenever(claimPendingPlayerActivityUseCase.execute()).thenReturn(listOf(lease))
        whenever(nicknameOwnershipRepository.findByNickname(nickname)).thenReturn(null)
        whenever(cristalixPlayerNameBatchGateway.findByNicknames(listOf(nickname))).thenReturn(
            PlayerNicknameIdentityBatch(
                playerIdsByNormalizedNickname = mapOf(nickname.normalized() to playerId),
                notFoundNormalizedNicknames = emptySet(),
            ),
        )
        whenever(cristalixPlayerBatchGateway.findByIds(listOf(playerId))).thenReturn(
            PlayerObservationBatch(
                observations = listOf(observation),
                notFound = emptySet(),
            ),
        )

        val service =
            ResolvePendingPlayerActivityService(
                claimPendingPlayerActivityUseCase = claimPendingPlayerActivityUseCase,
                nicknameOwnershipRepository = nicknameOwnershipRepository,
                cristalixPlayerNameBatchGateway = cristalixPlayerNameBatchGateway,
                cristalixPlayerBatchGateway = cristalixPlayerBatchGateway,
                playerObservationCoordinator = playerObservationCoordinator,
                recordPlayerDropActivityUseCase = recordPlayerDropActivityUseCase,
                completionCoordinator = completionCoordinator,
            )

        service.execute()

        val order =
            inOrder(
                playerObservationCoordinator,
                recordPlayerDropActivityUseCase,
                completionCoordinator,
            )

        order.verify(playerObservationCoordinator).apply(observation)
        order.verify(recordPlayerDropActivityUseCase).execute(
            RecordPlayerDropActivityCommand(
                playerId = playerId,
                occurredAt = occurredAt,
                dropCount = 2,
            ),
        )
        order.verify(completionCoordinator).completeSuccess(lease)

        verify(cristalixPlayerBatchGateway).findByIds(listOf(playerId))
    }
}
