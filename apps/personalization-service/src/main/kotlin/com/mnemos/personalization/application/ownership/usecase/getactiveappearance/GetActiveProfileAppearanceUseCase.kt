package com.mnemos.personalization.application.ownership.usecase.getactiveappearance

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.personalization.application.ownership.model.ActiveProfileAppearanceSnapshot

fun interface GetActiveProfileAppearanceUseCase {
    fun execute(accountId: AccountId): ActiveProfileAppearanceSnapshot
}
