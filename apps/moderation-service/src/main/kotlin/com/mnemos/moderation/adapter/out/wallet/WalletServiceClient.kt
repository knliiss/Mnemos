package com.mnemos.moderation.adapter.out.wallet

import com.mnemos.moderation.configuration.wallet.WalletServiceFeignConfiguration
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@FeignClient(
    name = "moderation-wallet-service-client",
    url = "\${mnemos.clients.wallet-service.url}",
    configuration = [WalletServiceFeignConfiguration::class],
)
interface WalletServiceClient {
    @GetMapping("/internal/v1/wallets/{ownerType}/{ownerId}/balances")
    fun getBalances(
        @PathVariable ownerType: WalletOwnerType,
        @PathVariable ownerId: UUID,
    ): List<WalletBalanceResponse>
}
