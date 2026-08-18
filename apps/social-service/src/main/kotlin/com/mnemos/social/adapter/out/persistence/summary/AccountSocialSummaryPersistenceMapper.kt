package com.mnemos.social.adapter.out.persistence.summary

import com.mnemos.social.domain.summary.model.AccountSocialSummary

object AccountSocialSummaryPersistenceMapper {
    fun AccountSocialSummary.toEntity(): AccountSocialSummaryEntity =
        AccountSocialSummaryEntity(
            accountId = accountId,
            likes = likes,
            dislikes = dislikes,
            comments = comments,
            revision = revision,
            updatedAt = updatedAt,
        )

    fun AccountSocialSummaryEntity.toDomain(): AccountSocialSummary =
        AccountSocialSummary(
            accountId = accountId,
            likes = likes,
            dislikes = dislikes,
            comments = comments,
            revision = revision,
            updatedAt = updatedAt,
        )
}
