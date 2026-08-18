package com.mnemos.social.adapter.out.persistence.reaction

import com.mnemos.social.domain.reaction.model.AccountReactionType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "account_reactions")
@IdClass(AccountReactionEntityId::class)
class AccountReactionEntity(
    @Id
    @Column(name = "actor_account_id", nullable = false)
    var actorAccountId: UUID,
    @Id
    @Column(name = "target_account_id", nullable = false)
    var targetAccountId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 8)
    var type: AccountReactionType,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
