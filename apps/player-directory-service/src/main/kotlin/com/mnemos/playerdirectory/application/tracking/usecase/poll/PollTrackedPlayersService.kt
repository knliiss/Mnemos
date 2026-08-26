package com.mnemos.playerdirectory.application.tracking.usecase.poll

import com.mnemos.playerdirectory.application.player.exception.PlayerDirectoryUpstreamUnavailableException
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerBatchGateway
import com.mnemos.playerdirectory.application.player.support.PlayerObservationCoordinator
import com.mnemos.playerdirectory.application.tracking.support.PlayerTrackingCompletionCoordinator
import com.mnemos.playerdirectory.application.tracking.usecase.claim.ClaimDuePlayerTrackingUseCase
import org.springframework.stereotype.Service
import java.util.logging.Level
import java.util.logging.Logger

@Service
class PollTrackedPlayersService(
    private val claimDuePlayerTrackingUseCase: ClaimDuePlayerTrackingUseCase,
    private val cristalixPlayerBatchGateway: CristalixPlayerBatchGateway,
    private val playerObservationCoordinator: PlayerObservationCoordinator,
    private val trackingCompletionCoordinator: PlayerTrackingCompletionCoordinator,
    private val logger: Logger = Logger.getLogger(PollTrackedPlayersService::class.java.name),
) : PollTrackedPlayersUseCase {
    override fun execute() {
        val leases = claimDuePlayerTrackingUseCase.execute()

        if (leases.isEmpty()) {
            return
        }

        val batch =
            try {
                cristalixPlayerBatchGateway.findByIds(leases.map { lease -> lease.playerId })
            } catch (exception: PlayerDirectoryUpstreamUnavailableException) {
                leases.forEach(trackingCompletionCoordinator::completeFailure)
                logger.log(Level.WARNING, "Cristalix monitoring batch failed", exception)

                return
            }

        val observationsByPlayerId = batch.observations.associateBy { observation -> observation.playerId }

        leases.forEach { lease ->
            val observation = observationsByPlayerId[lease.playerId]

            if (observation == null) {
                trackingCompletionCoordinator.completeFailure(lease)

                return@forEach
            }

            try {
                playerObservationCoordinator.apply(observation)
                trackingCompletionCoordinator.completeSuccess(
                    lease = lease,
                    observation = observation,
                )
            } catch (exception: RuntimeException) {
                trackingCompletionCoordinator.completeFailure(lease)
                logger.log(
                    Level.WARNING,
                    "Failed to apply tracked player observation for ${lease.playerId.id}",
                    exception,
                )
            }
        }
    }
}
