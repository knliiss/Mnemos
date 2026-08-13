package com.mnemos.bet.application.wallet.port.out

import com.mnemos.bet.domain.wallet.model.WalletOperation

interface BetWallet {
    fun request(operation: WalletOperation)
}
