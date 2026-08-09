package com.mnemos.wallet.api.controller

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.api.dto.TransactionSummaryResponse
import com.mnemos.wallet.application.transaction.TransactionHistoryQueryService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@Controller
@RequestMapping("/api/v1/accounts/{accountId}/transactions")
class AccountTransactionController(
    private val transactionHistoryQueryService: TransactionHistoryQueryService,
) {
    @GetMapping
    fun getTransactionHistory(
        @PathVariable("accountId") accountId: UUID,
        @RequestParam page: Int,
    ): List<TransactionSummaryResponse> = transactionHistoryQueryService.getHistory(AccountId(accountId), page)
}
