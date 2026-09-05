package com.mnemos.event.application.localization.model

import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.global.model.DomainGlobalEventType

object SaoEventLocalizationKeys {
    fun booster(type: DomainBoosterType): String =
        when (type) {
            DomainBoosterType.LUCK -> "sao.enum.boost.luck.name"
            DomainBoosterType.MONEY -> "sao.enum.boost.coin.name"
            DomainBoosterType.DAMAGE -> "sao.enum.boost.damage.name"
            DomainBoosterType.POWER -> "sao.enum.boost.power.name"
        }

    fun global(type: DomainGlobalEventType): String =
        when (type) {
            DomainGlobalEventType.DARKNESS -> "sao.event.darkness.alert"
            DomainGlobalEventType.MOON -> "sao.event.blood_moon.alert"
            DomainGlobalEventType.ECLIPSE -> "sao.event.solar_eclipse.alert"
            DomainGlobalEventType.EXPLOSION -> "sao.event.sunburst.alert"
            DomainGlobalEventType.COMET_CHAOS -> "sao.event.comet_chaos.alert"
        }

    fun location(locationId: Int): String = "sao.location.$locationId.name"

    const val RAID_OPEN = "sao.msg.raid.gates_opened"
    const val RAID_CLOSE = "sao.msg.raid.gates_closed"
    const val ITEM_DROP = "sao.msg.chat.item_drop"
    const val BOOSTER_ACTIVATED = "sao.msg.booster.global_activated"
}
