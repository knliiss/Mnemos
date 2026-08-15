package com.mnemos.personalization.application.security.port.out

import com.mnemos.personalization.application.security.model.PersonalizationActor

interface CurrentPersonalizationActorProvider {
    fun requireActor(): PersonalizationActor
}
