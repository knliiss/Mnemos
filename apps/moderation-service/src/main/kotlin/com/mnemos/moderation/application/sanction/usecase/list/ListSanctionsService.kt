package com.mnemos.moderation.application.sanction.usecase.list

import com.mnemos.moderation.application.sanction.mapper.SanctionViewMapper.toView
import com.mnemos.moderation.application.sanction.model.SanctionView
import com.mnemos.moderation.domain.sanction.repository.SanctionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListSanctionsService(
    private val sanctionRepository: SanctionRepository,
) : ListSanctionsUseCase {
    override fun execute(query: ListSanctionsQuery): List<SanctionView> =
        sanctionRepository
            .findByUserId(query.userId)
            .map { sanction -> sanction.toView() }
}
