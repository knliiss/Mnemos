package com.mnemos.personalization.application.personalization.usecase.createcustompack

interface CreateCustomNotificationPackUseCase {
    fun execute(command: CreateCustomNotificationPackCommand): CreateCustomNotificationPackResult
}
