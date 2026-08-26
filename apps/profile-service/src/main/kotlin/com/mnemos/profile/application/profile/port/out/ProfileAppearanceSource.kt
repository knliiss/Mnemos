package com.mnemos.profile.application.profile.port.out

import com.mnemos.profile.domain.profile.model.ProfileAppearance
import java.util.UUID

fun interface ProfileAppearanceSource {
    fun fetch(accountId: UUID): ProfileAppearance?
}
