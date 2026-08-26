package com.mnemos.profile.adapter.inbound.http.profile

import java.time.Instant

data class PublicProfilePresenceResponse(
    val status: Status,
    val online: Boolean?,
    val realm: String?,
    val lastSeenOnline: Instant?,
    val observedAt: Instant?,
) {
    enum class Status {
        ONLINE,
        OFFLINE,
        UNKNOWN,
        HIDDEN,
    }
}
