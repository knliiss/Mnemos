package com.mnemos.playerdirectory.adapter.out.http.cristalix

import com.mnemos.playerdirectory.application.player.exception.PlayerDirectoryUpstreamUnavailableException
import com.mnemos.playerdirectory.application.player.model.PlayerNicknameIdentityBatch
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerNameBatchGateway
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import feign.FeignException
import org.springframework.stereotype.Component

@Component
class CristalixPlayerNameBatchGatewayAdapter(
    private val client: CristalixPlayerClient,
    private val requestExecutor: CristalixRequestExecutor,
    private val metrics: CristalixApiMetrics,
) : CristalixPlayerNameBatchGateway {
    override fun findByNicknames(nicknames: List<PlayerNickname>): PlayerNicknameIdentityBatch {
        require(nicknames.isNotEmpty()) {
            "Cristalix player nickname batch cannot be empty"
        }
        require(nicknames.map(PlayerNickname::normalized).distinct().size == nicknames.size) {
            "Cristalix player nickname batch cannot contain duplicate names"
        }

        metrics.recordBatchSize(
            operation = "by_nickname",
            size = nicknames.size,
        )

        return try {
            val response =
                requestExecutor.execute(CristalixCredentialLane.MONITORING) { credential ->
                    client.findByNames(
                        authorization = bearerToken(credential.projectToken),
                        projectKey = credential.projectKey.trim(),
                        request = CristalixPlayerNameBatchRequest(names = nicknames.map(PlayerNickname::value)),
                    )
                }

            PlayerNicknameIdentityBatch(
                playerIdsByNormalizedNickname =
                    response.items.associate { player ->
                        PlayerNickname.of(player.name).normalized() to CristalixPlayerId(player.playerId)
                    },
                notFoundNormalizedNicknames =
                    response.notFound.mapTo(mutableSetOf()) { nickname ->
                        PlayerNickname.of(nickname).normalized()
                    },
            )
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
