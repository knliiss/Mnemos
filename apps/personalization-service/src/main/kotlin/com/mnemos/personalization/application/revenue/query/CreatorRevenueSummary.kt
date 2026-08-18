package com.mnemos.personalization.application.revenue.query

data class CreatorRevenueSummary(
    val confirmedPrime: Long,
    val pendingPrime: Long,
    val failedPrime: Long,
    val confirmedOperations: Int,
    val pendingOperations: Int,
    val failedOperations: Int,
)
