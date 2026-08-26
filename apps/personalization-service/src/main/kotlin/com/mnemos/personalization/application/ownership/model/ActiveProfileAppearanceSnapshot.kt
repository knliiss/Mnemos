package com.mnemos.personalization.application.ownership.model

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.personalization.contracts.shared.ProfileAppearanceSnapshotV1
import java.time.Instant

data class ActiveProfileAppearanceSnapshot(
    val accountId: AccountId,
    val revision: Long,
    val appearance: ProfileAppearanceSnapshotV1,
    val observedAt: Instant,
)
