package com.mnemos.realtime.application.collector.credential.usecase.disable

import com.mnemos.realtime.application.collector.credential.RequireCollectorCredential
import com.mnemos.realtime.application.collector.credential.mapper.CollectorCredentialViewMapper.toView
import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView
import com.mnemos.realtime.application.collector.port.out.CollectorConnectionTerminator
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import com.mnemos.realtime.domain.collector.repository.CollectorCredentialRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class DisableCollectorCredentialService(
    private val requireCredential: RequireCollectorCredential,
    private val repository: CollectorCredentialRepository,
    private val connectionTerminator: CollectorConnectionTerminator,
    private val clock: Clock,
) : DisableCollectorCredentialUseCase {
    @Transactional
    override fun execute(id: CollectorCredentialId): CollectorCredentialView {
        val credential = requireCredential.find(id)
        credential.disable(clock.instant())

        val saved = repository.save(credential)
        connectionTerminator.disconnect(id)

        return saved.toView()
    }
}
