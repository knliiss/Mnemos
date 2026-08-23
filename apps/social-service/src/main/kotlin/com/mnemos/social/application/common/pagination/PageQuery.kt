package com.mnemos.social.application.common.pagination

data class PageQuery(
    val page: Int,
    val size: Int,
) {
    init {
        require(page >= 0) { "Page must be 0 or greater" }
        require(size > 0) { "Size must be greater than 0" }
    }
}
