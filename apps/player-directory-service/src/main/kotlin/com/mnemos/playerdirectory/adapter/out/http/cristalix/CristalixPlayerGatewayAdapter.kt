package com.mnemos.playerdirectory.adapter.out.http.cristalix

import com.mnemos.playerdirectory.adapter.out.http.cristalix.CristalixPlayerApiMapper.toObservation
import com.mnemos.playerdirectory.application.player.exception.PlayerDirectoryUpstreamUnavailableException
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerGateway
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerObservation
import feign.FeignException
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CristalixPlayerGatewayAdapter(
    private val client: CristalixPlayerClient,
    private val requestExecutor: CristalixRequestExecutor,
) : CristalixPlayerGateway {
    override fun findById(playerId: CristalixPlayerId): PlayerObservation? =
        executeLookup {
            client.findById(
                authorization = bearerToken(it.projectToken),
                projectKey = it.projectKey.trim(),
                playerId = playerId.id,
                fields = PLAYER_FIELDS,
            )
        }

    override fun findByNickname(nickname: PlayerNickname): PlayerObservation? =
        executeLookup {
            client.findByNickname(
                authorization = bearerToken(it.projectToken),
                projectKey = it.projectKey.trim(),
                nickname = nickname.value,
                fields = PLAYER_FIELDS,
            )
        }

    private fun executeLookup(
        request: (CristalixApiProperties.Credential) -> CristalixPlayerResponse,
    ): PlayerObservation? =
        try {
            requestExecutor
                .execute(
                    lane = CristalixCredentialLane.INTERACTIVE,
                    request = request,
                ).toObservation(Instant.now())
        } catch (_: FeignException.NotFound) {
            null
        } catch (exception: FeignException.Forbidden) {
            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix API rejected player-directory credentials",
                cause = exception,
            )
        } catch (exception: FeignException.Unauthorized) {
            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix API rejected player-directory credentials",
                cause = exception,
            )
        } catch (exception: FeignException.TooManyRequests) {
            throw PlayerDirectoryUpstreamUnavailableException(
                message = "Cristalix API rate limit was exceeded",
                cause = exception,
            )
        } catch (exception: FeignException) {
            throw PlayerDirectoryUpstreamUnavailableException(cause = exception)
        }

    private fun bearerToken(projectToken: String): String = "Bearer ${projectToken.trim()}"

    companion object {
        private const val PLAYER_FIELDS =
            "playerId,name,version,updatedAt,social.lastSeenOnline,status.online,status.realm," +
                "status.realmType,status.privacyHidden,customization.settings"
    }
}
