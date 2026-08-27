package com.mnemos.playerdirectory.adapter.out.persistence.activity

import com.mnemos.playerdirectory.adapter.out.persistence.activity.PendingPlayerActivityPersistenceMapper.toDomain
import com.mnemos.playerdirectory.adapter.out.persistence.activity.PendingPlayerActivityPersistenceMapper.toEntity
import com.mnemos.playerdirectory.application.activity.port.out.PendingPlayerActivityRepository
import com.mnemos.playerdirectory.domain.activity.model.PendingPlayerActivity
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class PendingPlayerActivityRepositoryAdapter(
    private val repository: PendingPlayerActivityJpaRepository,
) : PendingPlayerActivityRepository {
    override fun enqueueDrop(
        nickname: PlayerNickname,
        occurredAt: Instant,
        recordedAt: Instant,
    ) {
        repository.enqueueDrop(
            normalizedNickname = nickname.normalized(),
            nickname = nickname.value,
            occurredAt = occurredAt,
            recordedAt = recordedAt,
        )
    }

    override fun enqueueIdentityDemand(
        nickname: PlayerNickname,
        recordedAt: Instant,
    ) {
        repository.enqueueIdentityDemand(
            normalizedNickname = nickname.normalized(),
            nickname = nickname.value,
            recordedAt = recordedAt,
        )
    }

    override fun save(activity: PendingPlayerActivity): PendingPlayerActivity =
        repository.save(activity.toEntity()).toDomain()

    override fun delete(activity: PendingPlayerActivity) {
        repository.delete(activity.toEntity())
    }

    override fun findByNicknameForUpdate(nickname: PlayerNickname): PendingPlayerActivity? =
        repository.findByNormalizedNicknameForUpdate(nickname.normalized())?.toDomain()

    override fun findDueForUpdate(
        now: Instant,
        limit: Int,
    ): List<PendingPlayerActivity> =
        repository
            .findDueForUpdate(
                now = now,
                limit = limit,
            ).map { activity -> activity.toDomain() }
}
