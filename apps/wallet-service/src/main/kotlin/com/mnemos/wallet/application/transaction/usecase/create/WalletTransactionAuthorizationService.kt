package com.mnemos.wallet.application.transaction.usecase.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.wallet.application.account.port.out.AccountPermissionProvider
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class WalletTransactionAuthorizationService(
    private val accountPermissionProvider: AccountPermissionProvider,
) {
    fun resolveFailureReason(
        request: CreateTransactionRequest,
        actorUserId: UUID?,
    ): WalletTransactionFailureReason? =
        when (val owner = request.owner) {
            is WalletOwner.Account -> authorizeAccount(request, owner, actorUserId)
            is WalletOwner.User -> authorizeUser(request, owner, actorUserId)
        }

    private fun authorizeAccount(
        request: CreateTransactionRequest,
        owner: WalletOwner.Account,
        actorUserId: UUID?,
    ): WalletTransactionFailureReason? {
        val requiredPermissions = requiredAccountPermissions(request.source)

        if (requiredPermissions.isEmpty()) {
            return null
        }

        val userId = actorUserId?.let(::UserId) ?: return WalletTransactionFailureReason.PERMISSION_DENIED
        val granted =
            requiredPermissions.all { permission ->
                accountPermissionProvider.hasPermission(
                    accountId = owner.accountId,
                    userId = userId,
                    permission = permission,
                )
            }

        return if (granted) null else WalletTransactionFailureReason.PERMISSION_DENIED
    }

    private fun authorizeUser(
        request: CreateTransactionRequest,
        owner: WalletOwner.User,
        actorUserId: UUID?,
    ): WalletTransactionFailureReason? {
        if (!requiresUserOwnership(request.source)) {
            return null
        }

        return if (actorUserId == owner.userId.id) {
            null
        } else {
            WalletTransactionFailureReason.PERMISSION_DENIED
        }
    }

    private fun requiredAccountPermissions(source: TransactionSource): Set<RolePermission> =
        when (source) {
            TransactionSource.BET_PLACE ->
                setOf(
                    RolePermission.PLACE_BETS,
                    RolePermission.SPEND_WALLET_FUNDS,
                )

            TransactionSource.PERSONALIZATION_PURCHASE ->
                setOf(
                    RolePermission.BUY_PERSONALIZATIONS,
                    RolePermission.SPEND_WALLET_FUNDS,
                )

            else -> emptySet()
        }

    private fun requiresUserOwnership(source: TransactionSource): Boolean =
        source == TransactionSource.PERSONALIZATION_PURCHASE ||
            source == TransactionSource.SPOTLIGHT_PURCHASE
}
