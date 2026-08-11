package com.mnemos.notification.application.notification.policy

import com.mnemos.notification.application.notification.model.NotificationType

class NotificationPolicyRegistry(
    private val policies: Map<NotificationType, NotificationTypePolicy>,
) {
    init {
        require(policies.keys == NotificationType.entries.toSet()) {
            "Policy must be configured for every notification type"
        }
    }

    fun get(type: NotificationType): NotificationTypePolicy =
        requireNotNull(policies[type]) {
            "Notification policy is not configured for type $type"
        }
}
