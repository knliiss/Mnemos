package com.mnemos.playerdirectory.application.activity.usecase.resolve

import com.mnemos.playerdirectory.application.activity.model.PendingPlayerActivityLease
import com.mnemos.playerdirectory.application.activity.support.PendingPlayerActivityCompletionCoordinator
import com.mnemos.playerdirectory.application.activity.usecase.claim.ClaimPendingPlayerActivityUseCase
import com.mnemos.playerdirectory.application.player.exception.PlayerDirectoryUpstreamUnavailableException
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerNameBatchGateway
import com.mnemos.playerdirectory.application.player.port.out.PlayerNicknameOwnershipRepository
import com.mnemos.playerdirectory.application.tracking.usecase.activity.RecordPlayerDropActivityCommand
import com.mnemos.playerdirectory.application.tracking.usecase.activity.RecordPlayerDropActivityUseCase
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import org.springframework.stereotype.Service
import java.util.logging.Level
import java.util.logging.Logger

@Service
class ResolvePendingPlayerActivityService(
    private val claimPendingPlayerActivityUseCase: ClaimPendingPlayerActivityUseCase,
    private val nicknameOwnershipRepository: PlayerNicknameOwnershipRepository,
    private val cristalixPlayerNameBatchGateway: CristalixPlayerNameBatchGateway,
    private val recordPlayerDropActivityUseCase: RecordPlayerDropActivityUseCase,
    private val completionCoordinator: PendingPlayerActivityCompletionCoordinator,
    private val logger: Logger = Logger.getLogger(ResolvePendingPlayerActivityService::class.java.name),
) : ResolvePendingPlayerActivityUseCase {
    override fun execute() {
        val leases = claimPendingPlayerActivityUseCase.execute()

        if (leases.isEmpty()) {
            return
        }

        val unresolved =
            leases.filterNot { lease ->
                val ownership = nicknameOwnershipRepository.findByNickname(lease.nickname)

                if (ownership == null) {
                    false
                } else {
                    completeResolved(
                        lease = lease,
                        playerId = ownership.playerId,
                    )
                    true
                }
            }

        if (unresolved.isEmpty()) {
            return
        }

        val batch =
            try {
                cristalixPlayerNameBatchGateway.findByNicknames(unresolved.map { lease -> lease.nickname })
            } catch (exception: PlayerDirectoryUpstreamUnavailableException) {
                unresolved.forEach(completionCoordinator::completeFailure)
                logger.log(Level.WARNING, "Cristalix pending player batch resolution failed", exception)

                return
            }

        unresolved.forEach { lease ->
            val playerId = batch.playerIdsByNormalizedNickname[lease.nickname.normalized()]

            if (playerId == null) {
                completionCoordinator.completeFailure(lease)
                return@forEach
            }

            try {
                completeResolved(
                    lease = lease,
                    playerId = playerId,
                )
            } catch (exception: RuntimeException) {
                completionCoordinator.completeFailure(lease)
                logger.log(
                    Level.WARNING,
                    "Failed to resolve pending activity for ${lease.nickname.value}",
                    exception,
                )
            }
        }
    }

    private fun completeResolved(
        lease: PendingPlayerActivityLease,
        playerId: CristalixPlayerId,
    ) {
        if (lease.pendingDropCount > 0) {
            recordPlayerDropActivityUseCase.execute(
                RecordPlayerDropActivityCommand(
                    playerId = playerId,
                    occurredAt = requireNotNull(lease.lastDropAt),
                    dropCount = lease.pendingDropCount,
                ),
            )
        }

        completionCoordinator.completeSuccess(lease)
    }
}
