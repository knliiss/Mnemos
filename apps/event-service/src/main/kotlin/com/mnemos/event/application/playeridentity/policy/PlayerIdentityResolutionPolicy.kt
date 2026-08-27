package com.mnemos.event.application.playeridentity.policy

import java.time.Duration

class PlayerIdentityResolutionPolicy(
    val maxAge: Duration,
) {
    init {
        require(!maxAge.isNegative && !maxAge.isZero) {
            "Player identity resolution max age must be positive"
        }
    }
}
