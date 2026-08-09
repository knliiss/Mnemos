package com.mnemos.wallet.api.controller

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.api.dto.BalanceResponse
import com.mnemos.wallet.application.balance.BalanceQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/balances")
class BalanceController(
    private val balanceQueryService: BalanceQueryService,
) {
    @GetMapping
    fun getBalance(
        @PathVariable accountId: AccountId,
    ): List<BalanceResponse> = balanceQueryService.getBalance(accountId)
}
