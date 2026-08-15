package com.mnemos.realtime.application.collector.credential.usecase.create

import com.mnemos.realtime.application.collector.credential.mapper.CollectorCredentialViewMapper.toView
import com.mnemos.realtime.application.collector.credential.model.CollectorCredentialView
import com.mnemos.realtime.application.collector.security.CollectorSecretGenerator
import com.mnemos.realtime.application.collector.security.CollectorSecretHasher
import com.mnemos.realtime.domain.collector.model.CollectorCredential
import com.mnemos.realtime.domain.collector.repository.CollectorCredentialRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class CreateCollectorCredentialService(
    private val repository: CollectorCredentialRepository,
    private val secretGenerator: CollectorSecretGenerator,
    private val secretHasher: CollectorSecretHasher,
    private val clock: Clock,
) : CreateCollectorCredentialUseCase {
    @Transactional
    override fun execute(command: CreateCollectorCredentialCommand): CollectorCredentialView {
        val secret = secretGenerator.generate()
        val credential =
            CollectorCredential.create(
                name = command.name,
                secretHash = secretHasher.hash(secret),
                now = clock.instant(),
            )

        return repository.create(credential).toView(secret)
    }
}
