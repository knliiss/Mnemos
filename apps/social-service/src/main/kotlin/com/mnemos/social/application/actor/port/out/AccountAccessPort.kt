package com.mnemos.social.application.actor.port.out

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.social.application.actor.model.SocialActor
import java.util.UUID

interface AccountAccessPort {
    fun resolveActiveActor(userId: UUID): SocialActor

    fun hasPermission(
        accountId: UUID,
        userId: UUID,
        permission: RolePermission,
    ): Boolean
}
