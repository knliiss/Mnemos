package com.mnemos.personalization.application.personalization.usecase.listreviewqueue

import com.mnemos.personalization.domain.personalization.model.Personalization

interface ListPersonalizationReviewQueueUseCase {
    fun execute(): List<Personalization>
}
