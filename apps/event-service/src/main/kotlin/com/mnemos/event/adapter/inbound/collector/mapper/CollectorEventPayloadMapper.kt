package com.mnemos.event.adapter.inbound.collector.mapper

import com.mnemos.event.adapter.inbound.collector.dto.report.BoosterCollectorEventPayload
import com.mnemos.event.adapter.inbound.collector.dto.report.CollectorEventPayload
import com.mnemos.event.adapter.inbound.collector.dto.report.GlobalCollectorEventPayload
import com.mnemos.event.adapter.inbound.collector.dto.report.ItemDropCollectorEventPayload
import com.mnemos.event.adapter.inbound.collector.dto.report.RaidCollectorEventPayload
import com.mnemos.event.domain.verification.model.report.BoosterCollectorEventReport
import com.mnemos.event.domain.verification.model.report.CollectorEventReport
import com.mnemos.event.domain.verification.model.report.GlobalCollectorEventReport
import com.mnemos.event.domain.verification.model.report.ItemDropCollectorEventReport
import com.mnemos.event.domain.verification.model.report.RaidCollectorEventReport

object CollectorEventPayloadMapper {
    fun CollectorEventPayload.toReport(): CollectorEventReport =
        when (this) {
            is ItemDropCollectorEventPayload ->
                ItemDropCollectorEventReport(
                    itemKey = itemKey,
                    itemName = itemName,
                    itemType = itemType,
                    itemRarity = itemRarity,
                    droppedFor = droppedFor,
                )

            is BoosterCollectorEventPayload ->
                BoosterCollectorEventReport(
                    type = boosterType,
                    activatedBy = activatedBy,
                )

            is GlobalCollectorEventPayload ->
                GlobalCollectorEventReport(
                    type = eventType,
                )

            is RaidCollectorEventPayload ->
                RaidCollectorEventReport(
                    locations = locations,
                )
        }
}
