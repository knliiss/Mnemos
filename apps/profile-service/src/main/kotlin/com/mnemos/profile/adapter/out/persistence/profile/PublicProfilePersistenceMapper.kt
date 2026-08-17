package com.mnemos.profile.adapter.out.persistence.profile

import com.mnemos.profile.domain.profile.model.PublicProfile

object PublicProfilePersistenceMapper {
    fun PublicProfile.toEntity(): PublicProfileEntity =
        PublicProfileEntity(
            accountId = accountId,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            likes = likes,
            dislikes = dislikes,
            comments = comments,
            socialRevision = socialRevision,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun PublicProfileEntity.toDomain(): PublicProfile =
        PublicProfile(
            accountId = accountId,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            likes = likes,
            dislikes = dislikes,
            comments = comments,
            socialRevision = socialRevision,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
