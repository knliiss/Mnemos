package com.mnemos.moderation.application.sanction.usecase.list

import com.mnemos.common.core.model.id.UserId

data class ListSanctionsQuery(
    val userId: UserId,
)
