package com.mnemos.notification.adapter.inbound.http.itemdrop.create

import java.util.UUID

data class CreateItemDropSubscriptionResponse(
    val subscriptionId: UUID,
    val droppedFor: String?,
    val itemName: String?,
)
