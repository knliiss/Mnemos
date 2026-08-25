package com.mnemos.event.adapter.out.persistence.wipe

import com.mnemos.event.adapter.out.persistence.wipe.WipePersistenceMapper.toDomain
import com.mnemos.event.adapter.out.persistence.wipe.WipePersistenceMapper.toEntity
import com.mnemos.event.domain.wipe.model.Wipe
import com.mnemos.event.domain.wipe.model.WipeId
import com.mnemos.event.domain.wipe.repository.WipeRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class WipeRepositoryAdapter(
    private val repository: WipeJpaRepository,
) : WipeRepository {
    override fun findCurrent(at: Instant): Wipe? = repository.findCurrentAt(at).firstOrNull()?.toDomain()

    override fun findCurrentForUpdate(at: Instant): Wipe? = repository.findCurrentForUpdate(at).firstOrNull()?.toDomain()

    override fun findLatestForUpdate(): Wipe? = repository.findLatestForUpdate().firstOrNull()?.toDomain()

    override fun findById(id: WipeId): Wipe? = repository.findById(id.id).orElse(null)?.toDomain()

    override fun findAll(): List<Wipe> = repository.findAllByOrderByStartedAtDesc().map { entity -> entity.toDomain() }

    override fun save(wipe: Wipe): Wipe = repository.saveAndFlush(wipe.toEntity()).toDomain()
}
