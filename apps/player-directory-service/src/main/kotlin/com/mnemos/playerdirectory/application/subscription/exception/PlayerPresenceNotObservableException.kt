package com.mnemos.playerdirectory.application.subscription.exception

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId

class PlayerPresenceNotObservableException(
    playerId: CristalixPlayerId,
) : RuntimeException("Online status for Cristalix player ${playerId.id} is hidden and cannot be subscribed to")
