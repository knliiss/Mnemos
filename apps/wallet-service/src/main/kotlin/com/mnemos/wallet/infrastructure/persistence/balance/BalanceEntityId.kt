package com.mnemos.wallet.infrastructure.persistence.balance

import com.mnemos.wallet.contracts.wallet.shared.Currency
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.io.Serializable
import java.util.UUID

@Embeddable
data class BalanceEntityId(
    @Column(name = "account_id", nullable = false)
    var accountId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 16)
    var currency: Currency,
) : Serializable
