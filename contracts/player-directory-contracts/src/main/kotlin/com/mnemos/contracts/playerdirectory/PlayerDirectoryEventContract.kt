package com.mnemos.contracts.playerdirectory

object PlayerDirectoryEventContract {
    const val TOPIC = "mnemos.player-directory.events.v1"
    const val VERSION = 1
}

object PlayerDirectoryEventType {
    const val PRESENCE_CHANGED = "player.presence.changed"
    const val WENT_OFFLINE = "player.went-offline"
    const val NICKNAME_CHANGED = "player.nickname.changed"
}

object PlayerDirectoryEventAggregateType {
    const val PLAYER = "PLAYER"
}
