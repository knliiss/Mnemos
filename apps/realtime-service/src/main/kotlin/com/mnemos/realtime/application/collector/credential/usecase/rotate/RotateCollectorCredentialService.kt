package com.mnemos.realtime.application.collector.credential.usecase.rotate

import com.mnemos.realtime.application.collector.credential.RequireCollectorCredential
import com.mnemos.realtime.application.collector.credential.mapper.CollectorCredentialViewMapper.toView
import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView
import com.mnemos.realtime.application.collector.port.out.CollectorConnectionTerminator
import com.mnemos.realtime.application.collector.security.CollectorSecretGenerator
import com.mnemos.realtime.application.collector.security.CollectorSecretHasher
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import com.mnemos.realtime.domain.collector.repository.CollectorCredentialRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class RotateCollectorCredentialService(
    private val requireCredential: RequireCollectorCredential,
    private val repository: CollectorCredentialRepository,
    private val secretGenerator: CollectorSecretGenerator,
    private val secretHasher: CollectorSecretHasher,
    private val connectionTerminator: CollectorConnectionTerminator,
    private val clock: Clock,
) : RotateCollectorCredentialUseCase {
    @Transactional
    override fun execute(id: CollectorCredentialId): CollectorCredentialView {
        val credential = requireCredential.find(id)
        val secret = secretGenerator.generate()

        credential.rotate(
            newSecretHash = secretHasher.hash(secret),
            now = clock.instant(),
        )

        val saved = repository.save(credential)
        connectionTerminator.disconnect(id)

        return saved.toView(secret)
    }
}
