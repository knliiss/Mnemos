package com.mnemos.event.domain.verification.model.report

data class RaidCollectorEventReport(
    val locations: Set<Int>,
) : CollectorEventReport
