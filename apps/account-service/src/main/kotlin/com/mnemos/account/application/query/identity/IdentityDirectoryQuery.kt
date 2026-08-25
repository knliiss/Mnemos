package com.mnemos.account.application.query.identity

data class IdentityDirectoryQuery(
    val query: String?,
    val page: Int,
    val size: Int,
    val order: IdentityDirectoryOrder,
)
