package com.mnemos.notification.application.notification.localization.port.out

import com.mnemos.notification.application.notification.localization.model.SaoLocalizationPack

fun interface SaoLocalizationGateway {
    fun findPack(locale: String): SaoLocalizationPack?
}
