package com.mnemos.profile.application.profile.usecase.reconcile

interface ReconcilePendingSocialSummariesUseCase {
    fun execute(limit: Int)
}
