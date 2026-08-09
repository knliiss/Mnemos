package com.mnemos.event.domain.verification.model.report

import com.mnemos.event.domain.global.model.DomainGlobalEventType

data class GlobalCollectorEventReport(
    val type: DomainGlobalEventType,
) : CollectorEventReport
