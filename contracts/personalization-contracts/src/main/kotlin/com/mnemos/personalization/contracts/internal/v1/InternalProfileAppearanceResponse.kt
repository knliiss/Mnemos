package com.mnemos.personalization.contracts.internal.v1

import com.mnemos.personalization.contracts.shared.ProfileAppearanceSnapshotV1
import java.time.Instant
import java.util.UUID

data class InternalProfileAppearanceResponse(
    val accountId: UUID,
    val revision: Long,
    val appearance: ProfileAppearanceSnapshotV1,
    val observedAt: Instant,
)
