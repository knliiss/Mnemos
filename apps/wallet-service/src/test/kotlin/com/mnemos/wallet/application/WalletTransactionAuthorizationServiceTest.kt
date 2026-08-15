package com.mnemos.wallet.application

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.wallet.application.account.port.out.AccountPermissionProvider
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionRequest
import com.mnemos.wallet.application.transaction.usecase.create.WalletTransactionAuthorizationService
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.transaction.model.RequestId
import com.mnemos.wallet.domain.transaction.model.TransactionSourceReferenceId
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class WalletTransactionAuthorizationServiceTest {
    private val accountPermissionProvider = FakeAccountPermissionProvider()
    private val service = WalletTransactionAuthorizationService(accountPermissionProvider)

    @Test
    fun `user prime debit requires same actor`() {
        val userId = UserId.generate()
        val request = request(WalletOwner.User(userId), Currency.PRIME, TransactionSource.PERSONALIZATION_PURCHASE)

        assertNull(service.resolveFailureReason(request, userId.id))
        assertEquals(
            WalletTransactionFailureReason.PERMISSION_DENIED,
            service.resolveFailureReason(request, UserId.generate().id),
        )
    }

    @Test
    fun `bet debit requires place bets and spend wallet permissions`() {
        val accountId = AccountId.generate()
        val userId = UserId.generate()
        val request = request(WalletOwner.Account(accountId), Currency.COIN, TransactionSource.BET_PLACE)

        accountPermissionProvider.grantedPermissions =
            setOf(
                RolePermission.PLACE_BETS,
                RolePermission.SPEND_WALLET_FUNDS,
            )

        assertNull(service.resolveFailureReason(request, userId.id))
        assertEquals(
            listOf(
                RolePermission.PLACE_BETS,
                RolePermission.SPEND_WALLET_FUNDS,
            ),
            accountPermissionProvider.checkedPermissions,
        )
    }

    @Test
    fun `bet debit is denied when actor can spend but cannot place bets`() {
        val accountId = AccountId.generate()
        val userId = UserId.generate()
        val request = request(WalletOwner.Account(accountId), Currency.COIN, TransactionSource.BET_PLACE)
        accountPermissionProvider.grantedPermissions = setOf(RolePermission.SPEND_WALLET_FUNDS)

        assertEquals(
            WalletTransactionFailureReason.PERMISSION_DENIED,
            service.resolveFailureReason(request, userId.id),
        )
    }

    private fun request(
        owner: WalletOwner,
        currency: Currency,
        source: TransactionSource,
    ): CreateTransactionRequest =
        CreateTransactionRequest(
            requestId = RequestId.generate(),
            owner = owner,
            currency = currency,
            type = TransactionType.DEBIT,
            source = source,
            sourceReferenceId = TransactionSourceReferenceId(AccountId.generate().id),
            amount = 100,
        )

    private class FakeAccountPermissionProvider : AccountPermissionProvider {
        var grantedPermissions: Set<RolePermission> = emptySet()
        val checkedPermissions = mutableListOf<RolePermission>()

        override fun hasPermission(
            accountId: AccountId,
            userId: UserId,
            permission: RolePermission,
        ): Boolean {
            checkedPermissions += permission

            return permission in grantedPermissions
        }
    }
}
