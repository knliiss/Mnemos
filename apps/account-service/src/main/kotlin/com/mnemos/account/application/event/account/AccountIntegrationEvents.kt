package com.mnemos.account.application.event.account

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.common.core.model.id.UserId
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.AccountCreatedV1
import com.mnemos.contracts.account.v1.AccountMemberChangedV1
import com.mnemos.contracts.account.v1.AccountOwnershipTransferredV1
import com.mnemos.contracts.account.v1.AccountStatusChangedV1
import com.mnemos.messaging.core.event.IntegrationEvent
import java.time.Instant

object AccountIntegrationEvents {
    fun created(
        account: Account,
        ownerMember: AccountMember,
    ): IntegrationEvent<AccountCreatedV1> =
        IntegrationEvent(
            type = AccountEventType.ACCOUNT_CREATED,
            aggregateId = account.id.id,
            occurredAt = account.createdAt,
            payload =
                AccountCreatedV1(
                    ownerMemberId = ownerMember.id.id,
                    ownerUserId = ownerMember.userId.id,
                    cristalixUuid = account.cristalixUuid.id,
                    cristalixNickname = account.cristalixNickname.value,
                    maxMembers = account.maxMembers,
                ),
        )

    fun activated(
        account: Account,
        recipientUserIds: List<UserId>,
        occurredAt: Instant,
    ): IntegrationEvent<AccountStatusChangedV1> =
        statusChanged(
            type = AccountEventType.ACCOUNT_ACTIVATED,
            account = account,
            recipientUserIds = recipientUserIds,
            occurredAt = occurredAt,
        )

    fun deactivated(
        account: Account,
        recipientUserIds: List<UserId>,
        occurredAt: Instant,
    ): IntegrationEvent<AccountStatusChangedV1> =
        statusChanged(
            type = AccountEventType.ACCOUNT_DEACTIVATED,
            account = account,
            recipientUserIds = recipientUserIds,
            occurredAt = occurredAt,
        )

    fun deleted(
        account: Account,
        recipientUserIds: List<UserId>,
        occurredAt: Instant,
    ): IntegrationEvent<AccountStatusChangedV1> =
        statusChanged(
            type = AccountEventType.ACCOUNT_DELETED,
            account = account,
            recipientUserIds = recipientUserIds,
            occurredAt = occurredAt,
        )

    fun ownershipTransferred(
        account: Account,
        previousOwner: AccountMember,
        newOwner: AccountMember,
        recipientUserIds: List<UserId>,
        occurredAt: Instant,
    ): IntegrationEvent<AccountOwnershipTransferredV1> =
        IntegrationEvent(
            type = AccountEventType.ACCOUNT_OWNER_TRANSFERRED,
            aggregateId = account.id.id,
            occurredAt = occurredAt,
            payload =
                AccountOwnershipTransferredV1(
                    previousOwnerMemberId = previousOwner.id.id,
                    previousOwnerUserId = previousOwner.userId.id,
                    newOwnerMemberId = newOwner.id.id,
                    newOwnerUserId = newOwner.userId.id,
                    recipientUserIds = recipientUserIds.map { userId -> userId.id },
                ),
        )

    fun memberJoined(
        account: Account,
        member: AccountMember,
        recipientUserIds: List<UserId>,
        occurredAt: Instant,
    ): IntegrationEvent<AccountMemberChangedV1> =
        memberChanged(
            type = AccountEventType.ACCOUNT_MEMBER_JOINED,
            account = account,
            member = member,
            recipientUserIds = recipientUserIds,
            occurredAt = occurredAt,
        )

    fun memberLeft(
        account: Account,
        member: AccountMember,
        recipientUserIds: List<UserId>,
        occurredAt: Instant,
    ): IntegrationEvent<AccountMemberChangedV1> =
        memberChanged(
            type = AccountEventType.ACCOUNT_MEMBER_LEFT,
            account = account,
            member = member,
            recipientUserIds = recipientUserIds,
            occurredAt = occurredAt,
        )

    fun memberRemoved(
        account: Account,
        member: AccountMember,
        recipientUserIds: List<UserId>,
        occurredAt: Instant,
    ): IntegrationEvent<AccountMemberChangedV1> =
        memberChanged(
            type = AccountEventType.ACCOUNT_MEMBER_REMOVED,
            account = account,
            member = member,
            recipientUserIds = recipientUserIds,
            occurredAt = occurredAt,
        )

    private fun statusChanged(
        type: String,
        account: Account,
        recipientUserIds: List<UserId>,
        occurredAt: Instant,
    ): IntegrationEvent<AccountStatusChangedV1> =
        IntegrationEvent(
            type = type,
            aggregateId = account.id.id,
            occurredAt = occurredAt,
            payload =
                AccountStatusChangedV1(
                    status = account.status.name,
                    recipientUserIds = recipientUserIds.map { userId -> userId.id },
                ),
        )

    private fun memberChanged(
        type: String,
        account: Account,
        member: AccountMember,
        recipientUserIds: List<UserId>,
        occurredAt: Instant,
    ): IntegrationEvent<AccountMemberChangedV1> =
        IntegrationEvent(
            type = type,
            aggregateId = account.id.id,
            occurredAt = occurredAt,
            payload =
                AccountMemberChangedV1(
                    memberId = member.id.id,
                    userId = member.userId.id,
                    recipientUserIds = recipientUserIds.map { userId -> userId.id },
                ),
        )
}
