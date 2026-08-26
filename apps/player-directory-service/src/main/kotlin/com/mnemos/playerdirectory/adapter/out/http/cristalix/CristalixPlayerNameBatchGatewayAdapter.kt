package com.mnemos.playerdirectory.adapter.out.http.cristalix

import com.mnemos.playerdirectory.adapter.out.http.cristalix.CristalixPlayerApiMapper.toObservation
import com.mnemos.playerdirectory.application.player.exception.PlayerDirectoryUpstreamUnavailableException
import com.mnemos.playerdirectory.application.player.model.PlayerNicknameObservationBatch
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerNameBatchGateway
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import feign.FeignException
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CristalixPlayerNameBatchGatewayAdapter(
    private val client: CristalixPlayerClient,
    private val requestExecutor: CristalixRequestExecutor,
) : CristalixPlayerNameBatchGateway {
    override fun findByNicknames(nicknames: List<PlayerNickname>): PlayerNicknameObservationBatch {
        require(nicknames.isNotEmpty()) {
            "Cristalix player nickname batch cannot be empty"
        }
        require(nicknames.map(PlayerNickname::normalized).distinct().size == nicknames.size) {
            "Cristalix player nickname batch cannot contain duplicate names"
        }

        val observedAt = Instant.now()

        return try {
            val response =
                requestExecutor.execute(CristalixCredentialLane.MONITORING) { credential ->
                    client.findByNames(
                        authorization = bearerToken(credential.projectToken),
                        projectKey = credential.projectKey.trim(),
                        fields = PLAYER_FIELDS,
                        request = CristalixPlayerNameBatchRequest(names = nicknames.map(PlayerNickname::value)),
                    )
                }
            val observations = response.items.map { player -> player.toObservation(observedAt) }

            PlayerNicknameObservationBatch(
                observationsByNormalizedNickname =
                    observations.associateBy { observation -> observation.nickname.normalized() },
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

    companion object {
        private const val PLAYER_FIELDS =
            "playerId,name,version,updatedAt,social.lastSeenOnline,status.online,status.realm," +
                "status.realmType,status.privacyHidden,customization.settings"
    }
}
