package com.mnemos.personalization.application.ownership.event.port.out

import com.mnemos.common.core.model.id.UserId

interface NotificationPackSelectionRevisionStore {
    fun next(userId: UserId): Long
}
