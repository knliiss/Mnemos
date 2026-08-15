package com.mnemos.realtime.application.collector.credential.usecase.enable

import com.mnemos.realtime.application.collector.credential.RequireCollectorCredential
import com.mnemos.realtime.application.collector.credential.mapper.CollectorCredentialViewMapper.toView
import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import com.mnemos.realtime.domain.collector.repository.CollectorCredentialRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class EnableCollectorCredentialService(
    private val requireCredential: RequireCollectorCredential,
    private val repository: CollectorCredentialRepository,
    private val clock: Clock,
) : EnableCollectorCredentialUseCase {
    @Transactional
    override fun execute(id: CollectorCredentialId): CollectorCredentialView {
        val credential = requireCredential.find(id)
        credential.enable(clock.instant())

        return repository.save(credential).toView()
    }
}
