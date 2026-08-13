package com.mnemos.account.domain.model.account

import com.mnemos.account.domain.exception.account.AccountDeletionUnavailableException
import com.mnemos.account.domain.exception.account.member.AccountMemberLimitReachedException
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.common.core.model.id.AccountId
import java.time.Instant

class Account private constructor(
    val id: AccountId,
    ownerMemberId: AccountMemberId,
    val cristalixUuid: CristalixUuid,
    cristalixNickname: CristalixNickname,
    val createdAt: Instant,
    updatedAt: Instant,
    status: AccountStatus,
    deactivatedAt: Instant?,
    maxMembers: Int,
    baseRoleId: RoleId?,
) {
    var ownerMemberId = ownerMemberId
        private set

    var cristalixNickname = cristalixNickname
        private set

    var updatedAt = updatedAt
        private set

    var status = status
        private set

    var deactivatedAt = deactivatedAt
        private set

    var maxMembers = maxMembers
        private set

    var baseRoleId = baseRoleId
        private set

    companion object {
        const val DEFAULT_MAX_MEMBERS = 10

        fun create(
            id: AccountId,
            ownerMemberId: AccountMemberId,
            cristalixUuid: CristalixUuid,
            cristalixNickname: CristalixNickname,
            maxMembers: Int = DEFAULT_MAX_MEMBERS,
            baseRoleId: RoleId? = null,
        ): Account {
            val now = Instant.now()
            require(maxMembers > 0) { "The max members must be > 0" }

            return Account(
                id = id,
                ownerMemberId = ownerMemberId,
                cristalixUuid = cristalixUuid,
                status = AccountStatus.ACTIVE,
                cristalixNickname = cristalixNickname,
                createdAt = now,
                updatedAt = now,
                deactivatedAt = null,
                maxMembers = maxMembers,
                baseRoleId = baseRoleId,
            )
        }

        fun restore(
            id: AccountId,
            ownerMemberId: AccountMemberId,
            cristalixUuid: CristalixUuid,
            cristalixNickname: CristalixNickname,
            status: AccountStatus,
            createdAt: Instant,
            updatedAt: Instant,
            deactivatedAt: Instant?,
            maxMembers: Int,
            baseRoleId: RoleId?,
        ): Account =
            Account(
                id = id,
                ownerMemberId = ownerMemberId,
                cristalixUuid = cristalixUuid,
                status = status,
                cristalixNickname = cristalixNickname,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deactivatedAt = deactivatedAt,
                maxMembers = maxMembers,
                baseRoleId = baseRoleId,
            )
    }

    fun transferOwnership(
        newOwnerMemberId: AccountMemberId,
        now: Instant = Instant.now(),
    ): Boolean {
        if (ownerMemberId == newOwnerMemberId) return false

        ownerMemberId = newOwnerMemberId
        updatedAt = now
        return true
    }

    fun updateCristalixNickname(newCristalixNickname: CristalixNickname) {
        if (newCristalixNickname == cristalixNickname) return

        cristalixNickname = newCristalixNickname
        updatedAt = Instant.now()
    }

    fun deactivate(now: Instant = Instant.now()) {
        if (status != AccountStatus.ACTIVE) {
            throw AccountDeletionUnavailableException(id, status)
        }

        status = AccountStatus.DEACTIVATED
        deactivatedAt = now
        updatedAt = now
    }

    fun reactivate(now: Instant = Instant.now()) {
        if (status != AccountStatus.DEACTIVATED) {
            throw AccountDeletionUnavailableException(id, status)
        }

        status = AccountStatus.ACTIVE
        deactivatedAt = null
        updatedAt = now
    }

    fun isActive(): Boolean = status == AccountStatus.ACTIVE

    fun requireAvailableMemberSlot(currentMemberCount: Int) {
        if (currentMemberCount >= maxMembers) {
            throw AccountMemberLimitReachedException(
                accountId = id,
                maxMembers = maxMembers,
            )
        }
    }

    fun updateMaxMembers(
        maxMembers: Int,
        currentMemberCount: Int,
        now: Instant = Instant.now(),
    ): Boolean {
        require(maxMembers > 0) {
            "Account member limit must be greater than zero"
        }
        require(maxMembers >= currentMemberCount) {
            "Account member limit cannot be lower than current member count"
        }

        if (this.maxMembers == maxMembers) return false

        this.maxMembers = maxMembers
        updatedAt = now
        return true
    }

    fun isOwner(memberId: AccountMemberId): Boolean = memberId == ownerMemberId

    fun changeBaseRole(
        roleId: RoleId?,
        now: Instant = Instant.now(),
    ): Boolean {
        if (baseRoleId == roleId) return false
        baseRoleId = roleId
        updatedAt = now
        return true
    }
}
