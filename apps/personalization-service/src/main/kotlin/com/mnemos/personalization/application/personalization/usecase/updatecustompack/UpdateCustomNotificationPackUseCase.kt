package com.mnemos.personalization.application.personalization.usecase.updatecustompack

import com.mnemos.personalization.domain.personalization.model.Personalization

interface UpdateCustomNotificationPackUseCase {
    fun execute(command: UpdateCustomNotificationPackCommand): Personalization
}
