package com.mnemos.notification.application.itemdrop.usecase.create

import com.mnemos.common.core.model.id.UserId

data class CreateItemDropSubscriptionCommand(
    val userId: UserId,
    val droppedFor: String?,
    val itemName: String?,
)
