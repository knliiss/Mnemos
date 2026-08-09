package com.mnemos.wallet.application.transaction

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.wallet.application.port.account.AccountPermissionProvider
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class WalletTransactionAuthorizationService(
    private val accountPermissionProvider: AccountPermissionProvider,
) {
    fun resolveFailureReason(
        request: CreateTransactionRequest,
        actorUserId: UUID?,
    ): WalletTransactionFailureReason? {
        val requiredPermission =
            requiredPermission(request.source) ?: return null

        val userId =
            actorUserId?.let(::UserId)
                ?: return WalletTransactionFailureReason.PERMISSION_DENIED

        val granted =
            accountPermissionProvider.hasPermission(
                accountId = request.accountId,
                userId = userId,
                permission = requiredPermission,
            )

        return if (granted) {
            null
        } else {
            WalletTransactionFailureReason.PERMISSION_DENIED
        }
    }

    private fun requiredPermission(source: TransactionSource): RolePermission? =
        when (source) {
            TransactionSource.BET_PLACE,
            TransactionSource.PERSONALIZATION_PURCHASE,
            ->
                RolePermission.SPEND_WALLET_FUNDS

            else -> null
        }
}
