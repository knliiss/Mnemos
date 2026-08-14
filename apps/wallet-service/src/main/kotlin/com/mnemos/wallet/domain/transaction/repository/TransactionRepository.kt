package com.mnemos.wallet.domain.transaction.repository

import com.mnemos.wallet.domain.transaction.model.Transaction

interface TransactionRepository {
    fun create(transaction: Transaction)
}
