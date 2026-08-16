package com.mnemos.social.adapter.out.persistence.summary

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "account_social_summaries")
class AccountSocialSummaryEntity(
    @Id
    @Column(name = "account_id", nullable = false)
    var accountId: UUID,
    @Column(name = "likes", nullable = false)
    var likes: Long,
    @Column(name = "dislikes", nullable = false)
    var dislikes: Long,
    @Column(name = "comments", nullable = false)
    var comments: Long,
    @Column(name = "revision", nullable = false)
    var revision: Long,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
