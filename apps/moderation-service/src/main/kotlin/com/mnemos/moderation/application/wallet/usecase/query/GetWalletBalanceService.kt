package com.mnemos.moderation.application.wallet.usecase.query

import com.mnemos.common.core.model.user.UserRole
import com.mnemos.moderation.application.security.port.out.CurrentModeratorProvider
import com.mnemos.moderation.application.wallet.model.WalletBalance
import com.mnemos.moderation.application.wallet.port.out.ModerationWalletBalanceReader
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetWalletBalanceService(
    private val currentModeratorProvider: CurrentModeratorProvider,
    private val balanceReader: ModerationWalletBalanceReader,
) {
    fun execute(
        ownerType: WalletOwnerType,
        ownerId: UUID,
    ): WalletBalance {
        val actor = currentModeratorProvider.current()

        requirePermission(
            role = actor.role,
            ownerType = ownerType,
        )

        return balanceReader.getBalance(
            ownerType = ownerType,
            ownerId = ownerId,
        )
    }

    private fun requirePermission(
        role: UserRole,
        ownerType: WalletOwnerType,
    ) {
        val allowed =
            when (ownerType) {
                WalletOwnerType.ACCOUNT -> role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN
                WalletOwnerType.USER -> role == UserRole.SUPER_ADMIN
            }

        if (!allowed) {
            throw AccessDeniedException("Role $role cannot view $ownerType wallet balances")
        }
    }
}
