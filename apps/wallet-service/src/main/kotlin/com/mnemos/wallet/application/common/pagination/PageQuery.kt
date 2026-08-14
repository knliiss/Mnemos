package com.mnemos.wallet.application.common.pagination

data class PageQuery(
    val page: Int,
    val size: Int,
) {
    init {
        require(page >= 0) { "Page must be 0 or greater." }
        require(size in 1..100) { "Size must be between 1 and 100." }
    }
}
