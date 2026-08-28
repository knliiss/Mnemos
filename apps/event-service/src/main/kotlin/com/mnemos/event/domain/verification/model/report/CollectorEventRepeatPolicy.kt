package com.mnemos.event.domain.verification.model.report

fun CollectorEventReport.allowsRepeatedObservationFromSameCollector(): Boolean =
    this is ItemDropCollectorEventReport
