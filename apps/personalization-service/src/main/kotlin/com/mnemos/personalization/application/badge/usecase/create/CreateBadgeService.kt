package com.mnemos.personalization.application.badge.usecase.create

import com.mnemos.personalization.application.badge.exception.BadgeAlreadyExistsException
import com.mnemos.personalization.application.badge.model.CreateBadgeCommand
import com.mnemos.personalization.application.badge.port.out.BadgeImageStorage
import com.mnemos.personalization.application.badge.support.BadgeAdministrationSupport
import com.mnemos.personalization.application.badge.support.BadgeImageValidator
import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.Clock

@Service
class CreateBadgeService(
    private val support: BadgeAdministrationSupport,
    private val repository: BadgeRepository,
    private val badgeImageStorage: BadgeImageStorage,
    private val badgeImageValidator: BadgeImageValidator,
    private val clock: Clock,
) : CreateBadgeUseCase {
    @Transactional
    override fun execute(command: CreateBadgeCommand): Badge {
        support.requireAdministrator()

        if (repository.findByCode(command.code) != null) {
            throw BadgeAlreadyExistsException()
        }

        val image = badgeImageValidator.validate(command.image)
        val badge =
            Badge.create(
                code = command.code,
                name = command.name,
                color = command.color,
                now = clock.instant(),
            )

        badgeImageStorage.store(
            objectKey = badge.imageObjectKey,
            image = image,
        )

        val rollbackCleanupRegistered = registerRollbackCleanup(badge.imageObjectKey)

        return try {
            repository.save(badge)
        } catch (exception: RuntimeException) {
            if (!rollbackCleanupRegistered) {
                deleteImageBestEffort(badge.imageObjectKey)
            }

            if (exception is DataIntegrityViolationException) {
                throw BadgeAlreadyExistsException()
            }

            throw exception
        }
    }

    private fun registerRollbackCleanup(objectKey: String): Boolean {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return false
        }

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCompletion(status: Int) {
                    if (status != TransactionSynchronization.STATUS_COMMITTED) {
                        deleteImageBestEffort(objectKey)
                    }
                }
            },
        )

        return true
    }

    private fun deleteImageBestEffort(objectKey: String) {
        runCatching {
            badgeImageStorage.delete(objectKey)
        }
    }
}
