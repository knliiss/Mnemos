package com.mnemos.bet.adapter.inbound.http.admin.wallet

import com.mnemos.bet.adapter.inbound.http.admin.wallet.AdminWalletOperationMapper.toResponse
import com.mnemos.bet.application.wallet.usecase.failed.ListFailedWalletOperationsUseCase
import com.mnemos.bet.application.wallet.usecase.retry.RetryWalletOperationCommand
import com.mnemos.bet.application.wallet.usecase.retry.RetryWalletOperationUseCase
import com.mnemos.bet.domain.wallet.model.WalletOperationId
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/bets/admin/wallet-operations")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class AdminWalletOperationController(
    private val listFailedWalletOperationsUseCase: ListFailedWalletOperationsUseCase,
    private val retryWalletOperationUseCase: RetryWalletOperationUseCase,
) {
    @GetMapping("/failed")
    fun failed(): List<FailedWalletOperationResponse> =
        listFailedWalletOperationsUseCase
            .execute()
            .map { operation -> operation.toResponse() }

    @PostMapping("/{operationId}/retry")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun retry(
        @PathVariable
        operationId: UUID,
    ) {
        retryWalletOperationUseCase.execute(
            RetryWalletOperationCommand(
                operationId = WalletOperationId(operationId),
            ),
        )
    }
}
