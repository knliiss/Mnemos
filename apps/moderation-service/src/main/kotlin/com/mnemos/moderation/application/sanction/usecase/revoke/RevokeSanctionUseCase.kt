package com.mnemos.moderation.application.sanction.usecase.revoke

interface RevokeSanctionUseCase {
    fun execute(command: RevokeSanctionCommand)
}
