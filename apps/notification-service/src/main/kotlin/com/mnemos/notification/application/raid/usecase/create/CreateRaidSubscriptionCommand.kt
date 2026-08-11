package com.mnemos.notification.application.raid.usecase.create

import com.mnemos.common.core.model.id.UserId

data class CreateRaidSubscriptionCommand(
    val userId: UserId,
    val locations: Set<Int>,
)
