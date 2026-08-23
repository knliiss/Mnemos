package com.mnemos.realtime.application.collector.activation.usecase.create

import com.mnemos.realtime.application.collector.activation.model.CollectorActivationView
import com.mnemos.realtime.application.collector.security.CollectorSecretGenerator
import com.mnemos.realtime.application.collector.security.CollectorSecretHasher
import com.mnemos.realtime.configuration.collector.CollectorConnectionProperties
import com.mnemos.realtime.domain.collector.model.CollectorActivation
import com.mnemos.realtime.domain.collector.repository.CollectorActivationRepository
import com.mnemos.security.CurrentMnemosUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class CreateCollectorActivationService(
    private val repository: CollectorActivationRepository,
    private val secretGenerator: CollectorSecretGenerator,
    private val secretHasher: CollectorSecretHasher,
    private val currentMnemosUser: CurrentMnemosUser,
    private val properties: CollectorConnectionProperties,
    private val clock: Clock,
) : CreateCollectorActivationUseCase {
    @Transactional
    override fun execute(): CollectorActivationView {
        val token = secretGenerator.generate()
        val now = clock.instant()
        val activation =
            CollectorActivation.create(
                userId = currentMnemosUser.requireUserId(),
                tokenHash = secretHasher.hash(token),
                now = now,
                ttl = properties.activationTtl,
            )
        val created = repository.create(activation)

        return CollectorActivationView(
            token = token,
            expiresAt = created.expiresAt,
        )
    }
}
