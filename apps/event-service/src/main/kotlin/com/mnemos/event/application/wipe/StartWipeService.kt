package com.mnemos.event.application.wipe

import com.mnemos.event.domain.wipe.exception.WipeNotFoundException
import com.mnemos.event.domain.wipe.exception.WipeValidationException
import com.mnemos.event.domain.wipe.model.Wipe
import com.mnemos.event.domain.wipe.model.WipeId
import com.mnemos.event.domain.wipe.repository.WipeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class StartWipeService(
    private val repository: WipeRepository,
    private val clock: Clock,
) {
    @Transactional
    fun execute(
        name: String?,
        startedAt: Instant,
        createdBy: String,
    ): Wipe {
        val now = clock.instant()
        val current =
            repository.findCurrentForUpdate(now)
                ?: throw WipeNotFoundException("Active wipe was not found")
        val latest = repository.findLatestForUpdate() ?: current

        if (latest.id != current.id && latest.startedAt.isAfter(now)) {
            throw WipeValidationException("A future wipe is already scheduled")
        }

        if (!startedAt.isAfter(current.startedAt)) {
            throw WipeValidationException("New wipe must start after the current wipe")
        }

        repository.save(current.finish(startedAt))

        val normalizedName = name?.trim()?.takeIf(String::isNotEmpty)
        val wipe =
            Wipe(
                id = WipeId.generate(),
                number = latest.number + 1,
                name = normalizedName,
                startedAt = startedAt,
                endedAt = null,
                createdBy = createdBy,
                createdAt = now,
            )

        return repository.save(wipe)
    }
}
