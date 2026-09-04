package com.mnemos.wallet.adapter.inbound.http.admin

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.adapter.inbound.http.WalletApiMapper.toQuery
import com.mnemos.wallet.adapter.inbound.http.WalletApiMapper.toResponse
import com.mnemos.wallet.adapter.inbound.http.common.PageRequest
import com.mnemos.wallet.adapter.inbound.http.common.PageResponse
import com.mnemos.wallet.adapter.inbound.http.transaction.TransactionResponse
import com.mnemos.wallet.application.transaction.usecase.list.ListAdminAccountTransactionsService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/accounts/{accountId}/transactions")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class AdminAccountTransactionController(
    private val listAdminAccountTransactionsService: ListAdminAccountTransactionsService,
) {
    @GetMapping
    fun transactions(
        @PathVariable accountId: UUID,
        @Valid @ModelAttribute request: PageRequest,
    ): PageResponse<TransactionResponse> =
        listAdminAccountTransactionsService
            .execute(
                accountId = AccountId(accountId),
                page = request.toQuery(),
            ).toResponse()
}
