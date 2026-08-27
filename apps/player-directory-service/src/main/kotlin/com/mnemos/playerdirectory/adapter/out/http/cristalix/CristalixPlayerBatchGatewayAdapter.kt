package com.mnemos.playerdirectory.adapter.out.http.cristalix

import com.mnemos.playerdirectory.adapter.out.http.cristalix.CristalixPlayerApiMapper.toObservationBatch
import com.mnemos.playerdirectory.application.player.exception.PlayerDirectoryUpstreamUnavailableException
import com.mnemos.playerdirectory.application.player.model.PlayerObservationBatch
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerBatchGateway
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import feign.FeignException
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class CristalixPlayerBatchGatewayAdapter(
    private val client: CristalixPlayerClient,
    private val requestExecutor: CristalixRequestExecutor,
    private val metrics: CristalixApiMetrics,
    private val clock: Clock,
) : CristalixPlayerBatchGateway {
    override fun findByIds(playerIds: List<CristalixPlayerId>): PlayerObservationBatch {
        require(playerIds.isNotEmpty()) { "Cristalix player batch cannot be empty" }
        require(playerIds.distinct().size == playerIds.size) { "Cristalix player batch cannot contain duplicate ids" }

        metrics.recordBatchSize(
            operation = "by_id",
            size = playerIds.size,
        )

        val observedAt = clock.instant()

        return try {
            requestExecutor
                .execute(CristalixCredentialLane.MONITORING) { credential ->
                    client.findByIds(
                        authorization = bearerToken(credential.projectToken),
                        projectKey = credential.projectKey.trim(),
                        request = CristalixPlayerBatchRequest(ids = playerIds.map(CristalixPlayerId::id)),
                    )
                }.toObservationBatch(observedAt)
        } catch (exception: FeignException.TooManyRequests) {
            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix monitoring API rate limit was exceeded",
                cause = exception,
            )
        } catch (exception: FeignException.Forbidden) {
            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix API rejected monitoring credentials",
                cause = exception,
            )
        } catch (exception: FeignException.Unauthorized) {
            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix API rejected monitoring credentials",
                cause = exception,
            )
        } catch (exception: FeignException) {
            throw PlayerDirectoryUpstreamUnavailableException(cause = exception)
        }
    }

    private fun bearerToken(projectToken: String): String = "Bearer ${projectToken.trim()}"
}
