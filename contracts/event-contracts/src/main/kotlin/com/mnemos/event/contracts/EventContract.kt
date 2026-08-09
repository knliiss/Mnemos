package com.mnemos.event.contracts

object EventEventContract {
    const val TOPIC = "mnemos.event.events.v1"
    const val VERSION = 1
}

object EventEventType {
    const val ITEM_DROPPED = "event.item.dropped"
    const val RAID_STARTED = "event.raid.started"
    const val BOOSTER_ACTIVATED = "event.booster.activated"
    const val GLOBAL_STARTED = "event.global.started"
}

object EventAggregateType {
    const val ITEM_DROP_EVENT = "ITEM_DROP_EVENT"
    const val RAID_EVENT = "RAID_EVENT"
    const val BOOSTER_EVENT = "BOOSTER_EVENT"
    const val GLOBAL_EVENT = "GLOBAL_EVENT"
}