package com.mnemos.personalization.contracts.event

import com.mnemos.personalization.contracts.shared.ProfileAppearanceSnapshotV1
import java.time.Instant
import java.util.UUID

data class ProfileAppearanceChangedV1(
    val accountId: UUID,
    val appearance: ProfileAppearanceSnapshotV1,
    val revision: Long,
    val occurredAt: Instant,
)
