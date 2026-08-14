package com.mnemos.wallet.adapter.inbound.http.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.adapter.inbound.http.WalletApiMapper.toQuery
import com.mnemos.wallet.adapter.inbound.http.WalletApiMapper.toResponse
import com.mnemos.wallet.adapter.inbound.http.balance.BalanceResponse
import com.mnemos.wallet.adapter.inbound.http.common.PageRequest
import com.mnemos.wallet.adapter.inbound.http.common.PageResponse
import com.mnemos.wallet.adapter.inbound.http.transaction.TransactionResponse
import com.mnemos.wallet.application.balance.usecase.get.GetAccountBalancesUseCase
import com.mnemos.wallet.application.transaction.usecase.list.ListAccountTransactionsQuery
import com.mnemos.wallet.application.transaction.usecase.list.ListAccountTransactionsUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/accounts/{accountId}")
class AccountWalletController(
    private val getAccountBalancesUseCase: GetAccountBalancesUseCase,
    private val listAccountTransactionsUseCase: ListAccountTransactionsUseCase,
) {
    @GetMapping("/balances")
    fun balances(
        @PathVariable accountId: UUID,
    ): List<BalanceResponse> =
        getAccountBalancesUseCase
            .execute(AccountId(accountId))
            .map { it.toResponse() }

    @GetMapping("/transactions")
    fun transactions(
        @PathVariable accountId: UUID,
        @Valid @ModelAttribute request: PageRequest,
    ): PageResponse<TransactionResponse> {
        val query =
            ListAccountTransactionsQuery(
                accountId = AccountId(accountId),
                page = request.toQuery(),
            )

        return listAccountTransactionsUseCase.execute(query).toResponse()
    }
}
