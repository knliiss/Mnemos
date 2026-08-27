package com.mnemos.playerdirectory.domain.player.model

enum class PlayerPresenceStatus {
    ONLINE,
    OFFLINE,
    UNKNOWN,
    HIDDEN,
    ;

    fun isVerifiableForSubscription(): Boolean = this == ONLINE || this == OFFLINE
}
