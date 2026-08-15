package com.mnemos.personalization.domain.ownership.model

import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import java.time.Instant

class OwnedPersonalization(
    val owner: PersonalizationOwner,
    val personalizationId: PersonalizationId,
    val type: PersonalizationType,
    val acquiredAt: Instant,
    active: Boolean,
) {
    var active: Boolean = active
        private set

    init {
        require(owner.target == type.target) { "Personalization owner target does not match personalization type" }
    }

    fun activate() {
        active = true
    }

    fun deactivate() {
        active = false
    }
}
