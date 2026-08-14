package com.mnemos.wallet.adapter.out.persistence.common

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import java.util.UUID

object WalletOwnerPersistenceMapper {
    fun toDomain(
        ownerType: WalletOwnerType,
        ownerId: UUID,
    ): WalletOwner =
        when (ownerType) {
            WalletOwnerType.ACCOUNT -> WalletOwner.Account(AccountId(ownerId))
            WalletOwnerType.USER -> WalletOwner.User(UserId(ownerId))
        }
}
