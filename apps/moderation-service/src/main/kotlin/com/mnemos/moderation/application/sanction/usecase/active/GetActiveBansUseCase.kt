package com.mnemos.moderation.application.sanction.usecase.active

interface GetActiveBansUseCase {
    fun execute(): List<ActiveBanView>
}
