package com.mnemos.event.adapter.out.persistence.wipe

import com.mnemos.event.adapter.out.persistence.wipe.WipePersistenceMapper.toDomain
import com.mnemos.event.adapter.out.persistence.wipe.WipePersistenceMapper.toEntity
import com.mnemos.event.domain.wipe.model.Wipe
import com.mnemos.event.domain.wipe.model.WipeId
import com.mnemos.event.domain.wipe.repository.WipeRepository
import org.springframework.stereotype.Repository

@Repository
class WipeRepositoryAdapter(
    private val repository: WipeJpaRepository,
) : WipeRepository {
    override fun findCurrent(): Wipe? = repository.findFirstByEndedAtIsNullOrderByStartedAtDesc()?.toDomain()

    override fun findCurrentForUpdate(): Wipe? = repository.findCurrentForUpdate().firstOrNull()?.toDomain()

    override fun findById(id: WipeId): Wipe? = repository.findById(id.id).orElse(null)?.toDomain()

    override fun findAll(): List<Wipe> = repository.findAllByOrderByStartedAtDesc().map { entity -> entity.toDomain() }

    override fun save(wipe: Wipe): Wipe = repository.saveAndFlush(wipe.toEntity()).toDomain()
}
