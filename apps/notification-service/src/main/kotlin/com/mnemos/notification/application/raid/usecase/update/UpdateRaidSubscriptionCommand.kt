package com.mnemos.notification.application.raid.usecase.update

import com.mnemos.common.core.model.id.UserId

data class UpdateRaidSubscriptionCommand(
    val userId: UserId,
    val locations: Set<Int>,
)
