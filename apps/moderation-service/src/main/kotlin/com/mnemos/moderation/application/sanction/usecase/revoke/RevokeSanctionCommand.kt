package com.mnemos.moderation.application.sanction.usecase.revoke

import com.mnemos.moderation.domain.sanction.model.SanctionId

data class RevokeSanctionCommand(
    val sanctionId: SanctionId,
)
