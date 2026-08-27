package com.mnemos.playerdirectory.application.realm.policy

import java.time.Duration

class SaoRealmReadPolicy(
    val freshness: Duration,
    val cacheTtl: Duration,
) {
    init {
        require(!freshness.isNegative && !freshness.isZero) {
            "SAO realm freshness must be positive"
        }
        require(!cacheTtl.isNegative && !cacheTtl.isZero) {
            "SAO realm cache TTL must be positive"
        }
        require(cacheTtl <= freshness) {
            "SAO realm cache TTL cannot exceed presence freshness"
        }
    }
}
