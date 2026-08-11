package com.mnemos.notification.application.notification.delivery.model

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.common.core.model.id.UserId
import java.util.UUID

@JvmInline
value class DeliveryCandidateId(
    val id: UUID,
) {
    companion object {
        fun generate(
            userId: UserId,
            sourceIds: Collection<UUID>,
        ): DeliveryCandidateId {
            require(sourceIds.isNotEmpty())

            val sources =
                sourceIds
                    .map(UUID::toString)
                    .sorted()
                    .joinToString(":")

            return DeliveryCandidateId(
                UuidCreator.getNameBasedSha1(
                    "mnemos:notification:delivery:${userId.id}:$sources",
                ),
            )
        }
    }
}
