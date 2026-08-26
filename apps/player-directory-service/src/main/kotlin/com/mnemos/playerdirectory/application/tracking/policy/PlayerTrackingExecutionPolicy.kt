package com.mnemos.playerdirectory.application.tracking.policy

import java.time.Duration

class PlayerTrackingExecutionPolicy(
    val batchSize: Int,
    val leaseDuration: Duration,
) {
    init {
        require(batchSize in 1..200) { "Player tracking batch size must be between 1 and 200" }
        require(!leaseDuration.isNegative && !leaseDuration.isZero) {
            "Player tracking lease duration must be positive"
        }
    }
}
