package com.mnemos.wallet.domain.wallet.model

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import java.util.UUID

sealed interface WalletOwner {
    val id: UUID
    val type: WalletOwnerType

    data class Account(
        val accountId: AccountId,
    ) : WalletOwner {
        override val id: UUID = accountId.id
        override val type: WalletOwnerType = WalletOwnerType.ACCOUNT
    }

    data class User(
        val userId: UserId,
    ) : WalletOwner {
        override val id: UUID = userId.id
        override val type: WalletOwnerType = WalletOwnerType.USER
    }
}
