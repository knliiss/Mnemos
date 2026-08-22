package com.mnemos.wallet.adapter.inbound.http.internal

import com.mnemos.wallet.adapter.inbound.http.WalletApiMapper.toResponse
import com.mnemos.wallet.adapter.inbound.http.balance.BalanceResponse
import com.mnemos.wallet.application.balance.usecase.get.GetWalletBalancesService
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/wallets")
class InternalWalletBalanceController(
    private val getWalletBalancesService: GetWalletBalancesService,
) {
    @GetMapping("/{ownerType}/{ownerId}/balances")
    fun balances(
        @PathVariable ownerType: WalletOwnerType,
        @PathVariable ownerId: UUID,
    ): List<BalanceResponse> =
        getWalletBalancesService
            .execute(
                ownerType = ownerType,
                ownerId = ownerId,
            )
            .map { balance -> balance.toResponse() }
}
