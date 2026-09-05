package com.mnemos.event.application.collector.service

import com.mnemos.event.application.booster.usecase.record.RecordBoosterEventCommand
import com.mnemos.event.application.booster.usecase.record.RecordBoosterEventUseCase
import com.mnemos.event.application.global.usecase.record.RecordGlobalEventCommand
import com.mnemos.event.application.global.usecase.record.RecordGlobalEventUseCase
import com.mnemos.event.application.itemdrop.usecase.record.RecordItemDropEventCommand
import com.mnemos.event.application.itemdrop.usecase.record.RecordItemDropEventUseCase
import com.mnemos.event.application.raid.usecase.record.RecordRaidEventCommand
import com.mnemos.event.application.raid.usecase.record.RecordRaidEventUseCase
import com.mnemos.event.domain.verification.model.report.BoosterCollectorEventReport
import com.mnemos.event.domain.verification.model.report.CollectorEventReport
import com.mnemos.event.domain.verification.model.report.GlobalCollectorEventReport
import com.mnemos.event.domain.verification.model.report.ItemDropCollectorEventReport
import com.mnemos.event.domain.verification.model.report.RaidCollectorEventReport
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ConfirmedEventRecorder(
    private val recordItemDropEventUseCase: RecordItemDropEventUseCase,
    private val recordBoosterEventUseCase: RecordBoosterEventUseCase,
    private val recordGlobalEventUseCase: RecordGlobalEventUseCase,
    private val recordRaidEventUseCase: RecordRaidEventUseCase,
) {
    fun record(
        report: CollectorEventReport,
        occurredAt: Instant,
    ) {
        when (report) {
            is ItemDropCollectorEventReport ->
                recordItemDropEventUseCase.execute(
                    RecordItemDropEventCommand(
                        itemKey = report.itemKey,
                        itemName = report.itemName,
                        itemType = report.itemType,
                        itemRarity = report.itemRarity,
                        droppedFor = report.droppedFor,
                        occurredAt = occurredAt,
                    ),
                )

            is BoosterCollectorEventReport ->
                recordBoosterEventUseCase.execute(
                    RecordBoosterEventCommand(
                        type = report.type,
                        activatedBy = report.activatedBy,
                        activatedAt = occurredAt,
                    ),
                )

            is GlobalCollectorEventReport ->
                recordGlobalEventUseCase.execute(
                    RecordGlobalEventCommand(
                        type = report.type,
                        startedAt = occurredAt,
                    ),
                )

            is RaidCollectorEventReport ->
                recordRaidEventUseCase.execute(
                    RecordRaidEventCommand(
                        locations = report.locations,
                        startedAt = occurredAt,
                    ),
                )
        }
    }
}
