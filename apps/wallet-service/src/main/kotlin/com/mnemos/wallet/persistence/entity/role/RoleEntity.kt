package com.mnemos.accountservice.persistence.entity.role

import com.mnemos.common.core.model.permission.RolePermission
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "roles")
class RoleEntity(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID,
    @Column(name = "account_id", nullable = false, updatable = false)
    var accountId: UUID,
    @Column(name = "hierarchy_rank", nullable = false)
    var hierarchyRank: Long,
    @Column(name = "role_color", nullable = false)
    var roleColor: String,
    @Column(name = "role_name", nullable = false)
    var roleName: String,
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "role_permissions",
        joinColumns = [
            JoinColumn(name = "role_id"),
        ],
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false)
    var permissions: MutableSet<RolePermission>,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
)
