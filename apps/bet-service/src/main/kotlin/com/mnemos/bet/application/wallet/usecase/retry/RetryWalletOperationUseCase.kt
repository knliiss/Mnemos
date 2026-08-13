package com.mnemos.bet.application.wallet.usecase.retry

interface RetryWalletOperationUseCase {
    fun execute(command: RetryWalletOperationCommand)
}
