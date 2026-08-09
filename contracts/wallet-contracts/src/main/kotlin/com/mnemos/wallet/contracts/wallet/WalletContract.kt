package com.mnemos.wallet.contracts.wallet

object WalletCommandContract {
    const val TOPIC = "mnemos.wallet.commands.v1"
    const val VERSION = 1
    const val AGGREGATE_TYPE = "WALLET_TRANSACTION_REQUEST"
}

object WalletCommandType {
    const val CREATE_TRANSACTION = "wallet.transaction.create-requested"
}

object WalletEventContract {
    const val TOPIC = "mnemos.wallet.events.v1"
    const val VERSION = 1
    const val AGGREGATE_TYPE = "WALLET_TRANSACTION_REQUEST"
}

object WalletEventType {
    const val TRANSACTION_COMPLETED = "wallet.transaction.completed"
    const val TRANSACTION_FAILED = "wallet.transaction.failed"
}