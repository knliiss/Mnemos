package com.mnemos.bet.application.wallet.usecase.process

interface ProcessWalletTransactionCompletedUseCase {
    fun execute(command: ProcessWalletTransactionCompletedCommand)
}
