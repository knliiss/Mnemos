package com.mnemos.event.domain.wipe.model

import java.time.Instant

data class Wipe(
    val id: WipeId,
    val number: Int,
    val name: String?,
    val startedAt: Instant,
    val endedAt: Instant?,
    val createdBy: String,
    val createdAt: Instant,
) {
    val active: Boolean
        get() = endedAt == null

    fun finish(at: Instant): Wipe {
        require(at.isAfter(startedAt)) {
            "Wipe end must be after its start"
        }

        return copy(endedAt = at)
    }

    fun contains(timestamp: Instant): Boolean =
        !timestamp.isBefore(startedAt) && (endedAt == null || timestamp.isBefore(endedAt))
}
