package com.mnemos.personalization.application.ownership.usecase.getactivepack

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.ownership.model.ActiveNotificationPackSelection

interface GetActiveNotificationPackUseCase {
    fun execute(userId: UserId): ActiveNotificationPackSelection?
}
