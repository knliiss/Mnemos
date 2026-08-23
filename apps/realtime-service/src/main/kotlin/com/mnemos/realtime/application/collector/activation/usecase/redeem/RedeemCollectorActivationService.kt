package com.mnemos.realtime.application.collector.activation.usecase.redeem

import com.mnemos.realtime.application.collector.activation.CollectorActivationConflictException
import com.mnemos.realtime.application.collector.activation.CollectorActivationExpiredException
import com.mnemos.realtime.application.collector.activation.CollectorActivationNotFoundException
import com.mnemos.realtime.application.collector.activation.model.RedeemCollectorActivationResult
import com.mnemos.realtime.application.collector.credential.CollectorCredentialNotFoundException
import com.mnemos.realtime.application.collector.security.CollectorSecretHasher
import com.mnemos.realtime.domain.collector.model.CollectorActivation
import com.mnemos.realtime.domain.collector.model.CollectorCredential
import com.mnemos.realtime.domain.collector.repository.CollectorActivationRepository
import com.mnemos.realtime.domain.collector.repository.CollectorCredentialRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class RedeemCollectorActivationService(
    private val activationRepository: CollectorActivationRepository,
    private val credentialRepository: CollectorCredentialRepository,
    private val secretHasher: CollectorSecretHasher,
    private val clock: Clock,
) : RedeemCollectorActivationUseCase {
    @Transactional
    override fun execute(command: RedeemCollectorActivationCommand): RedeemCollectorActivationResult {
        val activation =
            activationRepository.findByTokenHashForUpdate(
                secretHasher.hash(command.token),
            ) ?: throw CollectorActivationNotFoundException()

        val existingCredentialId = activation.credentialId

        if (existingCredentialId != null) {
            val existingCredential =
                credentialRepository.findById(existingCredentialId)
                    ?: throw CollectorCredentialNotFoundException(existingCredentialId)

            if (existingCredential.secretHash != command.secretHash) {
                throw CollectorActivationConflictException()
            }

            return RedeemCollectorActivationResult(
                credentialId = existingCredentialId,
            )
        }

        val now = clock.instant()

        if (activation.isExpired(now)) {
            throw CollectorActivationExpiredException()
        }

        val credential =
            CollectorCredential.create(
                name = credentialName(command.deviceName, activation),
                secretHash = command.secretHash,
                now = now,
                ownerUserId = activation.userId,
            )
        val createdCredential = credentialRepository.create(credential)

        activation.redeem(
            credentialId = createdCredential.id,
            now = now,
        )
        activationRepository.save(activation)

        return RedeemCollectorActivationResult(
            credentialId = createdCredential.id,
        )
    }

    private fun credentialName(
        deviceName: String,
        activation: CollectorActivation,
    ): String {
        val normalizedDeviceName =
            deviceName
                .trim()
                .replace(WHITESPACE_PATTERN, " ")
        val activationSuffix = activation.id.id.toString().take(8)

        return "$normalizedDeviceName-$activationSuffix"
    }

    private companion object {
        val WHITESPACE_PATTERN = Regex("\\s+")
    }
}
