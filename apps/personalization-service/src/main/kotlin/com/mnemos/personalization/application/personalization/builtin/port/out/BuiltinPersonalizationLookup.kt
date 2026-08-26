package com.mnemos.personalization.application.personalization.builtin.port.out

import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationCatalogKey

interface BuiltinPersonalizationLookup {
    fun findByCatalogKeyForUpdate(catalogKey: PersonalizationCatalogKey): Personalization?
}
