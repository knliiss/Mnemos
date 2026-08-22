package com.mnemos.realtime.application.delivery.port.out

import com.mnemos.realtime.application.delivery.model.WalletRealtimeOwner
import java.util.UUID

interface WalletRealtimeRecipientResolver {
    fun resolve(owner: WalletRealtimeOwner): Set<UUID>
}
