package com.mnemos.realtime.adapter.out.account

import com.mnemos.realtime.application.delivery.model.WalletRealtimeOwner
import com.mnemos.realtime.application.delivery.model.WalletRealtimeOwnerType
import com.mnemos.realtime.application.delivery.port.out.WalletRealtimeRecipientResolver
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountWalletRealtimeRecipientResolver(
    private val accountServiceClient: AccountServiceClient,
) : WalletRealtimeRecipientResolver {
    override fun resolve(owner: WalletRealtimeOwner): Set<UUID> =
        when (owner.type) {
            WalletRealtimeOwnerType.USER -> setOf(owner.id)
            WalletRealtimeOwnerType.ACCOUNT ->
                accountServiceClient
                    .findMemberUserIds(owner.id)
                    ?.userIds
                    .orEmpty()
                    .toSet()
        }
}
