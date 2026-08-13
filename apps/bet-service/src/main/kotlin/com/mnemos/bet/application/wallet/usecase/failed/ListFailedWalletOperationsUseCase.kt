package com.mnemos.bet.application.wallet.usecase.failed

interface ListFailedWalletOperationsUseCase {
    fun execute(): List<FailedWalletOperationView>
}
