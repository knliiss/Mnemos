package com.mnemos.profile.application.profile.usecase.sync

import java.time.Instant
import java.util.UUID

interface SyncPublicProfileUseCase {
    fun execute(
        accountId: UUID,
        cristalixUuid: UUID,
        cristalixNickname: String,
        createdAt: Instant,
    )
}
