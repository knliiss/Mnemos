package com.mnemos.moderation.application.sanction.usecase.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.domain.sanction.model.DomainSanctionType
import java.time.Duration

data class CreateSanctionCommand(
    val userId: UserId,
    val type: DomainSanctionType,
    val reason: String,
    val internalNote: String?,
    val duration: Duration?,
)
