package com.mnemos.profile.application.profile.usecase.projectaccount

import java.time.Instant
import java.util.UUID

interface ProjectAccountCreatedUseCase {
    fun execute(
        accountId: UUID,
        cristalixUuid: UUID,
        cristalixNickname: String,
        occurredAt: Instant,
    )
}
