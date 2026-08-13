package com.mnemos.moderation.adapter.inbound.http.admin.sanction

import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.application.sanction.model.SanctionView
import com.mnemos.moderation.application.sanction.usecase.create.CreateSanctionCommand
import com.mnemos.moderation.contracts.shared.SanctionStatus
import com.mnemos.moderation.contracts.shared.SanctionType
import com.mnemos.moderation.domain.sanction.model.DomainSanctionStatus
import com.mnemos.moderation.domain.sanction.model.DomainSanctionType
import java.time.Duration

object SanctionApiMapper {
    fun CreateSanctionRequest.toCommand(): CreateSanctionCommand =
        CreateSanctionCommand(
            userId = UserId(userId),
            type = type.toDomain(),
            reason = reason,
            internalNote = internalNote,
            duration = durationHours?.let(Duration::ofHours),
        )

    fun SanctionView.toResponse(): SanctionResponse =
        SanctionResponse(
            id = id.id,
            userId = userId.id,
            type = type.toContract(),
            reason = reason,
            internalNote = internalNote,
            createdByUserId = createdByUserId.id,
            createdAt = createdAt,
            expiresAt = expiresAt,
            status = status.toContract(),
        )

    private fun SanctionType.toDomain(): DomainSanctionType = DomainSanctionType.valueOf(name)

    private fun DomainSanctionType.toContract(): SanctionType = SanctionType.valueOf(name)

    private fun DomainSanctionStatus.toContract(): SanctionStatus = SanctionStatus.valueOf(name)
}
