package com.mnemos.playerdirectory.application.activity.usecase.claim

import com.mnemos.playerdirectory.application.activity.model.PendingPlayerActivityLease

fun interface ClaimPendingPlayerActivityUseCase {
    fun execute(): List<PendingPlayerActivityLease>
}
