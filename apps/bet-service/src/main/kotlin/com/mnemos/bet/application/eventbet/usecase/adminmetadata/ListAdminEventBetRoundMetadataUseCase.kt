package com.mnemos.bet.application.eventbet.usecase.adminmetadata

import com.mnemos.bet.application.eventbet.model.AdminEventBetRoundMetadataView

interface ListAdminEventBetRoundMetadataUseCase {
    fun execute(): List<AdminEventBetRoundMetadataView>
}
