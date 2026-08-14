package com.mnemos.wallet.adapter.out.persistence.balance

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.io.Serializable
import java.util.UUID

@Embeddable
data class BalanceEntityId(
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 16)
    var ownerType: WalletOwnerType,
    @Column(name = "owner_id", nullable = false)
    var ownerId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 16)
    var currency: Currency,
) : Serializable
