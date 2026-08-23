package com.mnemos.realtime.application.collector.credential.usecase.list

import com.mnemos.realtime.application.collector.credential.mapper.CollectorCredentialViewMapper.toView
import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView
import com.mnemos.realtime.domain.collector.repository.CollectorCredentialRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListCollectorCredentialsService(
    private val repository: CollectorCredentialRepository,
) : ListCollectorCredentialsUseCase {
    @Transactional(readOnly = true)
    override fun execute(): List<CollectorCredentialView> =
        repository
            .findAll()
            .sortedByDescending { it.createdAt }
            .map { it.toView() }
}
