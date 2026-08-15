package com.mnemos.personalization.application.common.pagination

data class PageQuery(
    val page: Int,
    val size: Int,
) {
    init {
        require(page >= 0) { "Page must be 0 or greater" }
        require(size in 1..MAX_PAGE_SIZE) { "Size must be between 1 and $MAX_PAGE_SIZE" }
    }

    companion object {
        const val MAX_PAGE_SIZE = 100
    }
}
