package com.mnemos.account.application.query.user

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.common.core.model.user.Username
import java.time.Instant

data class CurrentUserView(
    val id: UserId,
    val username: Username,
    val localizationLanguage: LocalizationLanguage,
    val activeAccountId: AccountId?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
