package com.mnemos.notification.application.notification.personalization.usecase

import com.mnemos.notification.application.notification.personalization.port.out.ActiveNotificationPackRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateNotificationPackSelectionService(
    private val repository: ActiveNotificationPackRepository,
) {
    @Transactional
    fun execute(command: UpdateNotificationPackSelectionCommand) {
        val pack = command.pack

        if (pack != null) {
            require(pack.userId == command.userId) {
                "Notification pack user does not match selection user"
            }
        }

        repository.apply(
            userId = command.userId,
            pack = pack,
            selectionRevision = command.selectionRevision,
            eventId = command.eventId,
            occurredAt = command.occurredAt,
        )
    }
}
