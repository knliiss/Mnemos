package com.mnemos.wallet.application.transaction.usecase.list

data class TransactionSort(
    val field: TransactionSortField = TransactionSortField.CREATED_AT,
    val direction: TransactionSortDirection = TransactionSortDirection.DESC,
)

enum class TransactionSortField {
    CREATED_AT,
    AMOUNT,
}

enum class TransactionSortDirection {
    ASC,
    DESC,
}
