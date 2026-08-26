package com.mnemos.playerdirectory.application.subscription.exception

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId

class PlayerPresenceVerificationUnavailableException(
    playerId: CristalixPlayerId,
) : RuntimeException("Online status for Cristalix player ${playerId.id} could not be verified")
