package com.mnemos.account.application.query.account

import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import java.time.Instant
import java.util.UUID

data class AccountSummary(
    val id: AccountId,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val currentMemberId: AccountMemberId,
    val memberCount: Int,
    val maxMembers: Int,
    val owner: Boolean,
    val active: Boolean,
)

data class AccountDetails(
    val id: AccountId,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val currentMemberId: AccountMemberId,
    val memberCount: Int,
    val maxMembers: Int,
    val owner: Boolean,
    val active: Boolean,
    val baseRoleId: RoleId?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class AccountContext(
    val availableAccounts: List<AccountSummary>,
    val activeAccount: AccountDetails?,
    val effectivePermissions: Set<RolePermission>,
)
