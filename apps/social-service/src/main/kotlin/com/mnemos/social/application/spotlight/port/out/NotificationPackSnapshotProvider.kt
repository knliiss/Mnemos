package com.mnemos.social.application.spotlight.port.out

import com.mnemos.social.application.spotlight.model.NotificationPackSelectionSnapshot
import java.util.UUID

interface NotificationPackSnapshotProvider {
    fun findActive(userId: UUID): NotificationPackSelectionSnapshot?
}
