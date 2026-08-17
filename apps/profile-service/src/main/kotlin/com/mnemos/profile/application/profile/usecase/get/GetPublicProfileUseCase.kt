package com.mnemos.profile.application.profile.usecase.get

import com.mnemos.profile.domain.profile.model.PublicProfile
import java.util.UUID

interface GetPublicProfileUseCase {
    fun execute(accountId: UUID): PublicProfile
}
