package com.mnemos.wallet.adapter.inbound.http.exchange

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.application.exchange.CoinPurchaseCatalog
import com.mnemos.wallet.application.exchange.CoinPurchaseOption
import com.mnemos.wallet.application.exchange.PurchaseCoinService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/coin-purchases")
class CoinPurchaseController(
    private val purchaseCoinService: PurchaseCoinService,
) {
    @GetMapping("/catalog")
    fun catalog(
        @PathVariable accountId: UUID,
    ): CoinPurchaseCatalogResponse =
        purchaseCoinService
            .catalog(AccountId(accountId))
            .toResponse()

    @PostMapping
    fun purchase(
        @PathVariable accountId: UUID,
        @Valid @RequestBody request: CoinPurchaseRequest,
    ): CoinPurchaseOptionResponse =
        purchaseCoinService
            .purchase(
                accountId = AccountId(accountId),
                requestId = request.requestId,
                primeAmount = request.primeAmount,
            ).toResponse()

    private fun CoinPurchaseCatalog.toResponse(): CoinPurchaseCatalogResponse =
        CoinPurchaseCatalogResponse(
            coinPerPrime = coinPerPrime,
            options = options.map { it.toResponse() },
        )

    private fun CoinPurchaseOption.toResponse(): CoinPurchaseOptionResponse =
        CoinPurchaseOptionResponse(
            primeAmount = primeAmount,
            coinAmount = coinAmount,
        )
}
