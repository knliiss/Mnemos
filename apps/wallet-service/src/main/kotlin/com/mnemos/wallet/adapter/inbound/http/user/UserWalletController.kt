package com.mnemos.wallet.adapter.inbound.http.user

import com.mnemos.wallet.adapter.inbound.http.WalletApiMapper.toQuery
import com.mnemos.wallet.adapter.inbound.http.WalletApiMapper.toResponse
import com.mnemos.wallet.adapter.inbound.http.WalletApiMapper.toSort
import com.mnemos.wallet.adapter.inbound.http.balance.BalanceResponse
import com.mnemos.wallet.adapter.inbound.http.common.PageRequest
import com.mnemos.wallet.adapter.inbound.http.common.PageResponse
import com.mnemos.wallet.adapter.inbound.http.transaction.TransactionResponse
import com.mnemos.wallet.application.balance.usecase.get.GetCurrentUserBalancesUseCase
import com.mnemos.wallet.application.transaction.usecase.list.ListCurrentUserTransactionsQuery
import com.mnemos.wallet.application.transaction.usecase.list.ListCurrentUserTransactionsUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users/me")
class UserWalletController(
    private val getCurrentUserBalancesUseCase: GetCurrentUserBalancesUseCase,
    private val listCurrentUserTransactionsUseCase: ListCurrentUserTransactionsUseCase,
) {
    @GetMapping("/balances")
    fun balances(): List<BalanceResponse> = getCurrentUserBalancesUseCase.execute().map { it.toResponse() }

    @GetMapping("/transactions")
    fun transactions(
        @Valid @ModelAttribute request: PageRequest,
    ): PageResponse<TransactionResponse> {
        val query =
            ListCurrentUserTransactionsQuery(
                page = request.toQuery(),
                sort = request.toSort(),
            )

        return listCurrentUserTransactionsUseCase.execute(query).toResponse()
    }
}
