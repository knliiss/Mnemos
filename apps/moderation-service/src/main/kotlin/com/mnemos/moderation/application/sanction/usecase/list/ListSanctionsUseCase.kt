package com.mnemos.moderation.application.sanction.usecase.list

import com.mnemos.moderation.application.sanction.model.SanctionView

interface ListSanctionsUseCase {
    fun execute(query: ListSanctionsQuery): List<SanctionView>
}
