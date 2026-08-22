package com.mnemos.moderation.adapter.inbound.http.admin.wallet

import com.mnemos.moderation.application.wallet.usecase.query.GetWalletBalanceService
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/moderation/wallet-balances")
class AdminWalletBalanceController(
    private val getWalletBalanceService: GetWalletBalanceService,
) {
    @GetMapping
    fun getBalance(
        @RequestParam targetType: WalletOwnerType,
        @RequestParam targetId: UUID,
    ): WalletBalanceResponse {
        val balance =
            getWalletBalanceService.execute(
                ownerType = targetType,
                ownerId = targetId,
            )

        return WalletBalanceResponse(
            targetType = balance.ownerType,
            targetId = balance.ownerId,
            currency = balance.currency,
            amount = balance.amount,
        )
    }
}
