package com.mnemos.wallet.domain.wallet

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import com.mnemos.wallet.domain.wallet.policy.WalletCurrencyPolicy
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WalletCurrencyPolicyTest {
    @Test
    fun `account supports coin only`() {
        val owner = WalletOwner.Account(AccountId.generate())

        assertTrue(WalletCurrencyPolicy.supports(owner, Currency.COIN))
        assertFalse(WalletCurrencyPolicy.supports(owner, Currency.PRIME))
    }

    @Test
    fun `user supports prime only`() {
        val owner = WalletOwner.User(UserId.generate())

        assertTrue(WalletCurrencyPolicy.supports(owner, Currency.PRIME))
        assertFalse(WalletCurrencyPolicy.supports(owner, Currency.COIN))
    }

    @Test
    fun `supported currencies follow owner type`() {
        assertTrue(WalletCurrencyPolicy.supportedCurrencies(WalletOwnerType.ACCOUNT) == listOf(Currency.COIN))
        assertTrue(WalletCurrencyPolicy.supportedCurrencies(WalletOwnerType.USER) == listOf(Currency.PRIME))
    }
}
