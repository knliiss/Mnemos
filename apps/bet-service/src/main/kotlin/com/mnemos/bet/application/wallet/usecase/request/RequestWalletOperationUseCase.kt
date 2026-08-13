package com.mnemos.bet.application.wallet.usecase.request

import com.mnemos.bet.domain.wallet.model.WalletOperation

interface RequestWalletOperationUseCase {
    fun requestStake(command: RequestWalletStakeCommand): WalletOperation

    fun requestCredit(command: RequestWalletCreditCommand): WalletOperation
}
