package com.mnemos.playerdirectory.application.player.usecase.refresh

import com.mnemos.playerdirectory.application.accountlink.port.out.PlayerAccountLinkRepository
import com.mnemos.playerdirectory.application.player.exception.PlayerNotFoundException
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerGateway
import com.mnemos.playerdirectory.application.player.port.out.PlayerIntegrationEventPublisher
import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.application.player.support.PlayerObservationCoordinator
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

data class LinkedPlayerNicknameRefreshResult(
    val playerId: UUID,
    val previousKnownNickname: String?,
    val currentNickname: String,
    val externalVersion: Long,
    val observedAt: Instant,
    val nicknameChangedInDirectory: Boolean,
)

@Service
class RefreshLinkedPlayerNicknameService(
    private val accountLinks: PlayerAccountLinkRepository,
    private val gateway: CristalixPlayerGateway,
    private val players: PlayerRepository,
    private val observations: PlayerObservationCoordinator,
    private val events: PlayerIntegrationEventPublisher,
) {
    /**
     * Explicit, admin-only check. Always queries Cristalix by immutable UUID;
     * does not rely on the cached name or the 6h scheduled polling interval.
     * The existing interactive credential quota also applies to this request.
     */
    @Transactional
    fun execute(playerId: CristalixPlayerId): LinkedPlayerNicknameRefreshResult {
        if (!accountLinks.existsByPlayerId(playerId)) {
            throw PlayerNotFoundException(playerId)
        }

        val before = players.findById(playerId)?.nickname?.value
        val observed = gateway.findById(playerId) ?: throw PlayerNotFoundException(playerId)
        check(observed.playerId == playerId) {
            "Cristalix returned a different player ID for requested UUID"
        }
        val current = observations.apply(observed)

        // Re-emit even when the directory had already learned the new name:
        // account-service may still hold an old snapshot or missed an event.
        events.publishInitial(current, observed.presence.observedAt)

        return LinkedPlayerNicknameRefreshResult(
            playerId = playerId.id,
            previousKnownNickname = before,
            currentNickname = current.nickname.value,
            externalVersion = current.externalVersion,
            observedAt = observed.presence.observedAt,
            nicknameChangedInDirectory = before != current.nickname.value,
        )
    }
}
