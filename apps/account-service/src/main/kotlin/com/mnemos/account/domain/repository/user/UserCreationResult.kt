package com.mnemos.account.domain.repository.user

import com.mnemos.account.domain.model.user.User

data class UserCreationResult(
    val user: User,
    val created: Boolean,
)
