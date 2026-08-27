package com.mnemos.playerdirectory.application.tracking.policy

import java.time.Duration

class PlayerTrackingActivityPolicy(
    val lowActivityDropThreshold: Long,
    private val lowActivityRetention: Duration,
    private val highActivityRetention: Duration,
) {
    init {
        require(lowActivityDropThreshold >= 0) {
            "Low-activity drop threshold cannot be negative"
        }
        require(!lowActivityRetention.isNegative && !lowActivityRetention.isZero) {
            "Low-activity retention must be positive"
        }
        require(highActivityRetention >= lowActivityRetention) {
            "High-activity retention cannot be shorter than low-activity retention"
        }
    }

    fun retentionFor(observedDropCount: Long): Duration =
        if (isHighActivity(observedDropCount)) {
            highActivityRetention
        } else {
            lowActivityRetention
        }

    fun isHighActivity(observedDropCount: Long): Boolean = observedDropCount > lowActivityDropThreshold
}
