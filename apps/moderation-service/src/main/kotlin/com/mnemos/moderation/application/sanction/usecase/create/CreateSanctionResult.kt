package com.mnemos.moderation.application.sanction.usecase.create

import com.mnemos.moderation.application.sanction.model.SanctionView

data class CreateSanctionResult(
    val sanction: SanctionView,
)
