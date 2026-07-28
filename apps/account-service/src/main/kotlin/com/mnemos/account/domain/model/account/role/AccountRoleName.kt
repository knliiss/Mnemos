package com.mnemos.account.domain.model.account.role

@JvmInline
value class AccountRoleName(
    val value: String,
) {
    init {
        require(value.length in 1..32) { "Account role name must be between 1 and 32" }
    }
}
