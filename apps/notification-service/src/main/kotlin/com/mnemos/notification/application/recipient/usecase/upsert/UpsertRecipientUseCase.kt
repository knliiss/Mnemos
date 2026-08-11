package com.mnemos.notification.application.recipient.usecase.upsert

interface UpsertRecipientUseCase {
    fun execute(command: UpsertRecipientCommand)
}
