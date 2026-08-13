package com.mnemos.bet.application.itemrace.usecase.create

import com.mnemos.bet.domain.itemrace.model.DomainItemType

data class CreateItemRaceCommand(
    val itemName: String,
    val itemType: DomainItemType,
    val durationHours: Long,
    val entryAmount: Long,
)
