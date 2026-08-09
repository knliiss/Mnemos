package com.mnemos.event.domain.verification.model.report

import com.mnemos.event.domain.booster.model.DomainBoosterType

data class BoosterCollectorEventReport(
    val type: DomainBoosterType,
    val activatedBy: String,
) : CollectorEventReport
