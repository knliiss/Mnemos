package com.mnemos.notification.configuration.notification

import com.mnemos.notification.application.notification.policy.NotificationTypePolicy

object NotificationPolicyMapper {
    fun NotificationPolicyProperties.TypePolicy.toPolicy(): NotificationTypePolicy =
        NotificationTypePolicy(
            aggregationEnabled = aggregationEnabled,
            aggregationWindow = aggregationWindow,
            priority = priority,
        )
}
