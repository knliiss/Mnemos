package com.mnemos.playerdirectory.application.activity.usecase.resolve

import com.mnemos.playerdirectory.application.activity.support.PendingPlayerActivityCompletionCoordinator
import com.mnemos.playerdirectory.application.activity.usecase.claim.ClaimPendingPlayerActivityUseCase
import com.mnemos.playerdirectory.application.player.exception.PlayerDirectoryUpstreamUnavailableException
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerNameBatchGateway
import com.mnemos.playerdirectory.application.player.support.PlayerObservationCoordinator
import com.mnemos.playerdirectory.application.tracking.usecase.activity.RecordPlayerDropActivityCommand
import com.mnemos.playerdirectory.application.tracking.usecase.activity.RecordPlayerDropActivityUseCase
import org.springframework.stereotype.Service
import java.util.logging.Level
import java.util.logging.Logger

@Service
class ResolvePendingPlayerActivityService(
    private val claimPendingPlayerActivityUseCase: ClaimPendingPlayerActivityUseCase,
    private val cristalixPlayerNameBatchGateway: CristalixPlayerNameBatchGateway,
    private val observationCoordinator: PlayerObservationCoordinator,
    private val recordPlayerDropActivityUseCase: RecordPlayerDropActivityUseCase,
    private val completionCoordinator: PendingPlayerActivityCompletionCoordinator,
    private val logger: Logger = Logger.getLogger(ResolvePendingPlayerActivityService::class.java.name),
) : ResolvePendingPlayerActivityUseCase {
    override fun execute() {
        val leases = claimPendingPlayerActivityUseCase.execute()

        if (leases.isEmpty()) {
            return
        }

        val batch =
            try {
                cristalixPlayerNameBatchGateway.findByNicknames(leases.map { lease -> lease.nickname })
            } catch (exception: PlayerDirectoryUpstreamUnavailableException) {
                leases.forEach(completionCoordinator::completeFailure)
                logger.log(Level.WARNING, "Cristalix pending player batch resolution failed", exception)

                return
            }

        leases.forEach { lease ->
            val observation = batch.observationsByNormalizedNickname[lease.nickname.normalized()]

            if (observation == null) {
                completionCoordinator.completeFailure(lease)
                return@forEach
            }

            try {
                val player = observationCoordinator.apply(observation)

                recordPlayerDropActivityUseCase.execute(
                    RecordPlayerDropActivityCommand(
                        playerId = player.id,
                        occurredAt = lease.lastDropAt,
                        dropCount = lease.pendingDropCount,
                    ),
                )
                completionCoordinator.completeSuccess(lease)
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
}
