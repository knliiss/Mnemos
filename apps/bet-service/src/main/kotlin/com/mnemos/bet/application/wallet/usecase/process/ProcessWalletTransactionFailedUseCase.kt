package com.mnemos.bet.application.wallet.usecase.process

interface ProcessWalletTransactionFailedUseCase {
    fun execute(command: ProcessWalletTransactionFailedCommand)
}
