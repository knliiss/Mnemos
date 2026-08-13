package com.mnemos.moderation.application.sanction.usecase.create

interface CreateSanctionUseCase {
    fun execute(command: CreateSanctionCommand): CreateSanctionResult
}
