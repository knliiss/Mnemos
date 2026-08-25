package com.mnemos.account.application.query.identity

data class IdentityDirectoryPage<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
