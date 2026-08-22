package com.mnemos.realtime.application.delivery.model

import java.util.UUID

data class WalletRealtimeOwner(
    val type: WalletRealtimeOwnerType,
    val id: UUID,
)
