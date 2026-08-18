package com.mnemos.personalization.adapter.out.persistence.selection

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "profile_appearance_revisions")
class ProfileAppearanceRevisionEntity(
    @Id
    @Column(name = "account_id", nullable = false, updatable = false)
    val accountId: UUID,
    @Column(name = "revision", nullable = false)
    var revision: Long,
) {
    fun increment(): Long {
        revision = Math.addExact(revision, 1L)

        return revision
    }
}
