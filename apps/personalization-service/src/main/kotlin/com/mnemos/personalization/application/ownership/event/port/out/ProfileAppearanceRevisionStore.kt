package com.mnemos.personalization.application.ownership.event.port.out

import com.mnemos.common.core.model.id.AccountId

interface ProfileAppearanceRevisionStore {
    fun next(accountId: AccountId): Long

    fun current(accountId: AccountId): Long
}
