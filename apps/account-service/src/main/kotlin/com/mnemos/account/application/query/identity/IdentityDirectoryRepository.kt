package com.mnemos.account.application.query.identity

interface IdentityDirectoryRepository {
    fun searchUsers(query: IdentityDirectoryQuery): IdentityDirectoryPage<UserIdentityDirectoryEntry>

    fun searchAccounts(query: IdentityDirectoryQuery): IdentityDirectoryPage<AccountIdentityDirectoryEntry>
}
