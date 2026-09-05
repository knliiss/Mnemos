package com.mnemos.event.application.localization.port.out

import com.mnemos.event.application.localization.model.SaoLocalizationPackDescriptor

interface SaoLocalizationSource {
    fun listPacks(): List<SaoLocalizationPackDescriptor>

    fun downloadProperties(descriptor: SaoLocalizationPackDescriptor): Map<String, String>
}
