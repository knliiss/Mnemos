package com.mnemos.playerdirectory.application.tracking.usecase.claim

import com.mnemos.playerdirectory.application.tracking.model.PlayerTrackingLease

fun interface ClaimDuePlayerTrackingUseCase {
    fun execute(): List<PlayerTrackingLease>
}
